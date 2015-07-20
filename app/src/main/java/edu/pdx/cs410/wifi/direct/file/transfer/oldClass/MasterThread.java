package edu.pdx.cs410.wifi.direct.file.transfer.oldClass;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;
import edu.pdx.cs410.wifi.direct.file.transfer.oldClass.RecvThread;
import edu.pdx.cs410.wifi.direct.file.transfer.oldClass.SendThread;

/**
 * Created by User on 7/5/2015.
 */
public class MasterThread extends Thread {
    public boolean isDone;
    public boolean slaveDone;
    public boolean masterDone;
    public String url;
    public String slaveIp;
    public int transPort;
    private int totalLen;
    private int masterLen;
    private int slaveLen;
    private int alreadyMasterLen;
    private int alreadySlaveLen;

    public MasterThread(String l, String sIp, int port) {
        url = l;
        slaveIp = sIp;
        transPort = port;

        isDone = false;
        slaveDone = false;
        masterDone = false;
    }

    @Override
    public void run() {
        Handler taskHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                            /*master has done its task*/
                        masterDone = true;
                    case 2:
                            /*slave has done its task*/
                        slaveDone = true;
                    default:
                        super.handleMessage(msg);
                }
            }
        };

		/*get data length*/
        try {
            URL urlObj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            totalLen = conn.getContentLength();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        /*start master thread*/
        while (!isDone || !slaveDone || !masterDone) {
            /*schedule task*/
            masterLen = totalLen/2;
            slaveLen = totalLen - masterLen;
            DownloadTask mTask = new DownloadTask(0, masterLen - 1, false, url);
            DownloadTask sTask = new DownloadTask(masterLen, totalLen - 1, false, url);
            /*build command*/
            String command = "taskstart:" + Integer.toString(masterLen) + "\n" + "taskend:" + Integer.toString(totalLen - 1) + "\n" + "url:" + url + "\n";
            /*set storage path*/
            String recvFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() +  "/A-NRS-Send/recv1.temp";
            File tempRecv = new File(recvFilePath);

            /*send task to slave, then wait for slave to send data back*/
            try {
                Thread sendThread = new Thread(new SendThread(transPort, InetAddress.getByName(slaveIp), taskHandler, command));
                Thread recvThread = new Thread(new RecvThread(transPort, taskHandler, tempRecv));
            } catch (Exception e) {
                e.printStackTrace();
            }
            /*execute downloading*/
//            Thread dlThread = new Thread(new HttpDownload(url, mTask));
        }
    }
}
