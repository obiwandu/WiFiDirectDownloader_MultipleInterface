package edu.pdx.cs410.wifi.direct.file.transfer.master;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;

import edu.pdx.cs410.wifi.direct.file.transfer.TaskScheduler;
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
            signalActivity("Exception during getting http length:" + e.toString());
        }

        /* set recv file path */
        String recvFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/A-NRS-Recv";
        String recvFileName = "recvFile";
        File recvFile = new File(recvFilePath, recvFileName);
        RandomAccessFile tempRecvFile;
        try {
            tempRecvFile = new RandomAccessFile(recvFile, "rwd");
        } catch (Exception e) {
            signalActivity("Exception during creating RA file:" + e.toString());
            return;
        }
        /*initialize TaskScheduler*/
        TaskScheduler taskScheduler = new TaskScheduler(totalLen, url);
        /*start master thread*/
        while (!taskScheduler.isTaskDone()) {
//        while (!isDone || !slaveDone || !masterDone) {
            /*schedule task*/
            DownloadTask mTask;
            DownloadTask sTask;
            DownloadTask retTasks[];
            try {
                retTasks = taskScheduler.scheduleTask(4 * 1024);
                mTask = retTasks[0];
                sTask = retTasks[1];
            } catch (Exception e) {
                signalActivity("Exception during task scheduling:" + e.toString());
                return;
            }
//            masterLen = totalLen / 2;
//            slaveLen = totalLen - masterLen;
//            DownloadTask mTask = new DownloadTask(0, masterLen - 1, true, url);
//            DownloadTask sTask = new DownloadTask(masterLen, totalLen - 1, true, url);

            /*set storage path*/
//            String recvFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/A-NRS-Recv";
//            String recvFileName = "slave-recv.temp";
//            File slaveTempRecv = new File(recvFilePath, recvFileName);
//            recvFileName = "master-recv.temp";
//            File masterTempRecv = new File(recvFilePath, recvFileName);

            try {
                if (sTask != null) {
                    tempRecvFile.seek(sTask.start);
                /*download on slave*/
                    slaveBw = MasterOperation.remoteDownload(sTask, tempRecvFile, slaveSockAddr, masterSockAddr, this);
                    slaveDone = true;
                /*submit task*/
                }
            } catch (Exception e) {
                signalActivity("Exception during remote downloading:" + e.toString());
            }
            /*execute downloading*/
            try {
                if (mTask != null) {
                    tempRecvFile.seek(mTask.start);
                /*execute downloading*/
                    masterBw = MasterOperation.httpDownload(mTask, tempRecvFile, this);
                    masterDone = true;
                }
            } catch (Exception e) {
                signalActivity("Exception during remote downloading:" + e.toString());
            }
            /*submit tasks*/
            taskScheduler.submitTask(masterBw, slaveBw);
        }
    }

    public void signalActivity(String msg) {
        Bundle b = new Bundle();
        b.putString("message", msg);
        masterResult.send(nrsPort, b);
    }
}
