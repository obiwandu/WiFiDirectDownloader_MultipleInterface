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

import edu.pdx.cs410.wifi.direct.file.transfer.BackendService;
import edu.pdx.cs410.wifi.direct.file.transfer.Statistic;
import edu.pdx.cs410.wifi.direct.file.transfer.TaskScheduler;
import edu.pdx.cs410.wifi.direct.file.transfer.TimeMetric;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.HttpDownload;

/**
 * Created by User on 7/9/2015.
 */
public class MasterService extends BackendService {
    protected String url;
    //    private String slaveIp;
//    private String masterIp;
//    private int transPort;

    public MasterService() {
        super();
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
        long masterBw = 0;
        long slaveBw = 0;
        String originalFileName = "unnamed";
        TimeMetric tm = new TimeMetric();
        Statistic stat = new Statistic();
        long chunkSize = 5120 * 1024;
        long minChunkSize = 500 * 1024;
        int threadNum = 2;

        url = (String) intent.getExtras().get("url");
        nrsPort = (Integer) intent.getExtras().get("port");
        resultReceiver = (ResultReceiver) intent.getExtras().get("masterResult");
        chunkSize = (Long) intent.getExtras().get("chunkSize");
        minChunkSize = (Long) intent.getExtras().get("minChunkSize");

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
        String recvFileName = originalFileName;
        File recvFile = new File(recvFilePath, recvFileName);

        /*initialize TaskScheduler*/
        TaskScheduler taskScheduler = new TaskScheduler(totalLen, url, threadNum);

        tm.startTimer();
        /* Start statistics timer */
        try {
            stat.startTimer();
        } catch (Exception e) {
            signalActivity("Exception in starting timer:" + e.toString());
        }

        for (int i = 0; i < threadNum; i ++) {
            /*start master thread*/
            Thread masterThd = new Thread(new MasterTaskThread(stat, i, taskScheduler, recvFile, this, chunkSize, minChunkSize));
            try {
                taskScheduler.semaphoreMasterDone.acquire();
            } catch (Exception e) {
                signalActivity("Exception during accquring master lock:" + e.toString());
            }
            masterThd.start();
        }

        /* When transmission is done, close file and stop slave*/
        try {
            for (int i = 0; i < threadNum; i ++) {
                taskScheduler.semaphoreMasterDone.acquire();
            }

            /* Stop statistics timer */
            try {
                stat.stopTimer();
            } catch (Exception e) {
                signalActivity("Exception in stopping timer:" + e.toString());
            }
            long totalTime = tm.getTimeLapse();
            int avgBw = (int)((float)1000 * ((float)taskScheduler.leftTask.totalLen/(float)((int)totalTime * 1024)));

            for (int i = 0; i < threadNum; i ++) {
                taskScheduler.semaphoreMasterDone.release();
            }
            this.signalActivityComplete();
            this.signalActivity("All tasks have been done, downloading complete! Time consume: " + Long.toString(totalTime / (long) 1000) + " (s) | Avg bw: " + Integer.toString(avgBw) + "KB/s");
        } catch (Exception e) {
            this.signalActivity("Exception during closing file:" + e.toString());
        }
    }
}
