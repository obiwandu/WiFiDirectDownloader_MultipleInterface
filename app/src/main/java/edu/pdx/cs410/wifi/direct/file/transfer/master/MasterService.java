package edu.pdx.cs410.wifi.direct.file.transfer.master;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;

import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.HttpDownload;

/**
 * Created by User on 7/9/2015.
 */
public class MasterService extends IntentService {
    private String url;
    //    private String slaveIp;
//    private String masterIp;
//    private int transPort;
    private InetAddress masterIp;
    private InetAddress slaveIp;
    private ResultReceiver masterResult;
    private int nrsPort;

    public MasterService() {
        super("MasterService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int totalLen = 0;
        int masterLen;
        int slaveLen;
        int alreadyMasterLen;
        int alreadySlaveLen;
        boolean isDone = false;
        boolean slaveDone = false;
        boolean masterDone = false;
        int masterBw = 0;
        int slaveBw = 0;

        url = (String) intent.getExtras().get("url");
        nrsPort = (Integer) intent.getExtras().get("port");
        masterIp = (InetAddress) intent.getExtras().get("masterIp");
        slaveIp = (InetAddress) intent.getExtras().get("slaveIp");
        masterResult = (ResultReceiver) intent.getExtras().get("masterResult");

        InetSocketAddress masterSockAddr = new InetSocketAddress(masterIp, nrsPort);
        InetSocketAddress slaveSockAddr = new InetSocketAddress(slaveIp, nrsPort);

        /*get data length*/
        try {
            URL urlObj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            totalLen = conn.getContentLength();
        } catch (Exception e) {
            signalActivity("Fail to get http length:" + e.toString());
        }

        /*start master thread*/
        while (!isDone || !slaveDone || !masterDone) {
            /*schedule task*/
            masterLen = totalLen / 2;
            slaveLen = totalLen - masterLen;
            DownloadTask mTask = new DownloadTask(0, masterLen - 1, false, url);
            DownloadTask sTask = new DownloadTask(masterLen, totalLen - 1, false, url);

            /*set storage path*/
            String recvFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/A-NRS-Send/recv1.temp";
            File tempRecv = new File(recvFilePath);

            try {
                /*download on slave*/
                MasterOperation.remoteDownload(sTask, tempRecv, slaveSockAddr, masterSockAddr, this);

                /**/
            } catch (Exception e) {
                signalActivity("Failure in remote downloading:" + e.toString());
            }
            /*execute downloading*/
        }
    }

    public void signalActivity(String msg) {
        Bundle b = new Bundle();
        b.putString("message", msg);
        masterResult.send(nrsPort, b);
    }
}
