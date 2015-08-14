package edu.pdx.cs410.wifi.direct.file.transfer.master;

import android.content.Intent;
import android.os.Environment;
import android.os.ResultReceiver;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;

import edu.pdx.cs410.wifi.direct.file.transfer.BackendService;
import edu.pdx.cs410.wifi.direct.file.transfer.Statistic;
import edu.pdx.cs410.wifi.direct.file.transfer.TaskScheduler;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpConnector;

/**
 * Created by User on 7/9/2015.
 */
public class MultithreadMasterService extends BackendService {
    private String url;
    private ArrayList<InetAddress> slaveList;
    private int slaveNum;

    public MultithreadMasterService() {
        super();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        totalLen = 0;
        int masterLen;
        int slaveLen;
        int alreadyMasterLen;
        int alreadySlaveLen;
        boolean isDone = false;
        boolean slaveDone = false;
        boolean masterDone = false;
        int masterBw = 0;
        int slaveBw = 0;
//        long chunkSize = 50 * 1024;
        long chunkSize = 5120 * 1024;
//        long minChunkSize = 10 * 1024;
        long minChunkSize = 500 * 1024;
        String originalFileName = "unnamed";
//        TimeMetric tm = new TimeMetric();
        stat = new Statistic(this);

        url = (String) intent.getExtras().get("url");
        nrsPort = (Integer) intent.getExtras().get("port");
        masterIp = (InetAddress) intent.getExtras().get("masterIp");
        slaveIp = (InetAddress) intent.getExtras().get("slaveIp");
        slaveList = (ArrayList<InetAddress>) intent.getExtras().get("slaveList");
        chunkSize = (Long) intent.getExtras().get("chunkSize");
        minChunkSize = (Long) intent.getExtras().get("minChunkSize");
        resultReceiver = (ResultReceiver) intent.getExtras().get("masterResult");

        InetSocketAddress masterSockAddr = new InetSocketAddress(masterIp, nrsPort);
        InetSocketAddress slaveSockAddr = new InetSocketAddress(slaveIp, nrsPort);
        ArrayList<InetSocketAddress> slaveSockAddrdList = new ArrayList<InetSocketAddress>();
        slaveNum = slaveList.size();
        for (InetAddress temp : slaveList) {
            slaveSockAddrdList.add(new InetSocketAddress(temp, nrsPort));
        }

        /* Get data length */
        try {
            URL urlObj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            totalLen = conn.getContentLength();
        } catch (Exception e) {
            signalActivityException("Exception during getting http length:" + e.toString());
        }

        /* Set recv file path */
        String recvFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/A-NRS-Recv";
        originalFileName = "qzone_5.5.1.192_android_r98080_20150616175052_release_QZGW_D.apk";
        String recvFileName = originalFileName;
        File recvFile = new File(recvFilePath, recvFileName);

        /*initialize TaskScheduler*/
        TaskScheduler taskScheduler = new TaskScheduler(totalLen, url, slaveNum);

//        /* Start timer */
//        tm.startTimer();
        /* Start statistics timer */
        try {
            stat.startTimer();
        } catch (Exception e) {
            signalActivityException("Exception in starting timer:" + e.toString());
        }

        /*start master thread*/
        Thread masterThd = new Thread(new MasterTaskThread(stat, 0, taskScheduler, recvFile, this, chunkSize, minChunkSize));
        try {
            taskScheduler.semaphoreMasterDone.acquire();
        } catch (Exception e) {
            signalActivityException("Exception during accquring master lock:" + e.toString());
        }
        masterThd.start();

        /*start slave thread for all slaves*/
        ArrayList<TcpConnector> connList = new ArrayList<TcpConnector>();
        for (int i = 0; i < slaveNum; i++) {
            try {
                TcpConnector tempConn = new TcpConnector(slaveSockAddrdList.get(i), masterSockAddr, this, 0);
                connList.add(tempConn);
                Thread slaveThd = new Thread(new SlaveTaskThread(stat, i, taskScheduler, recvFile, tempConn, chunkSize, minChunkSize));
                taskScheduler.semaphoreSlaveDone.acquire();
                slaveThd.start();
            } catch (Exception e) {
                signalActivityException("Exception during slave transmission:" + e.toString());
            }
        }

        /* When transmission is done, close file and stop slave*/
        try {
            taskScheduler.semaphoreMasterDone.acquire();
            for (int i = 0; i < slaveNum; i++) {
                taskScheduler.semaphoreSlaveDone.acquire();
            }

            /* Stop statistics timer */
            try {
                stat.stopTimer();
            } catch (Exception e) {
                signalActivityException("Exception in stopping timer:" + e.toString());
            }
//            long totalTime = tm.getTimeLapse();
//            int avgBw = (int) ((float) 1000 * ((float) taskScheduler.leftTask.totalLen / (float) ((int) totalTime * 1024)));

            for (int i = 0; i < slaveNum; i++) {
                MasterOperation.remoteStop(connList.get(i));
                connList.get(i).close();
            }

            taskScheduler.semaphoreMasterDone.release();
            for (int i = 0; i < slaveNum; i++) {
                taskScheduler.semaphoreSlaveDone.release();
            }
            signalActivityComplete();
            signalActivity("All tasks have been done, downloading complete!");
        } catch (Exception e) {
            signalActivityException("Exception during closing file:" + e.toString());
        }
    }

}
