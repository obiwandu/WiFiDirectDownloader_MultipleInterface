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
    protected String url;
    //    private String slaveIp;
//    private String masterIp;
//    private int transPort;
    protected InetAddress masterIp;
    protected InetAddress slaveIp;
    protected ResultReceiver masterResult;
    protected int nrsPort;

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
        String originalFileName = "unnamed";

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
            originalFileName = "weixin622android580.apk";
        } catch (Exception e) {
            signalActivity("Exception during getting http length:" + e.toString());
        }

        /* set recv file path */
        String recvFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/A-NRS-Recv";
//        String recvFileName = "recvFile";
        String recvFileName = originalFileName;
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
        while (true) {
            try {
                isDone = taskScheduler.isTaskDone();
                if (isDone) {
                    break;
                }
            } catch (Exception e) {
                signalActivity("Exception during terminal condition fetching:" + e.toString());
            }
//        while (!isDone || !slaveDone || !masterDone) {
            /*schedule task*/
            DownloadTask mTask;
            DownloadTask sTask;
            DownloadTask retTasks[];
            try {
//                retTasks = taskScheduler.scheduleTask(4 * 1024);
                retTasks = taskScheduler.scheduleTask(100 * 1024);
                mTask = retTasks[0];
                sTask = retTasks[1];
            } catch (Exception e) {
                signalActivity("Exception during task scheduling:" + e.toString());
                return;
            }

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
            signalActivityProgress("Task left:" + Integer.toString(taskScheduler.leftTask.end - taskScheduler.leftTask.start));
        }
        /* when transmission is done, close file and stop slave*/
        try {
            tempRecvFile.close();
            /* no need to stop slave */
        } catch (Exception e) {
            signalActivity("Exception during closing file:" + e.toString());
        }
    }

    public void signalActivity(String msg) {
        Bundle b = new Bundle();
        b.putString("message", msg);
        masterResult.send(nrsPort, b);
    }

    public void signalActivityProgress(String msg) {
        Bundle b = new Bundle();
        b.putString("progress", msg);
        masterResult.send(1, b);
    }
}
