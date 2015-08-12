package edu.pdx.cs410.wifi.direct.file.transfer.master;

import android.content.Intent;
import android.os.Environment;
import android.os.ResultReceiver;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import edu.pdx.cs410.wifi.direct.file.transfer.BackendService;
import edu.pdx.cs410.wifi.direct.file.transfer.TaskScheduler;
import edu.pdx.cs410.wifi.direct.file.transfer.TimeMetric;
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
        long totalLen = 0;
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
        TimeMetric tm = new TimeMetric();

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
        for(InetAddress temp : slaveList) {
            slaveSockAddrdList.add(new InetSocketAddress(temp, nrsPort));
        }

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
        originalFileName = "qzone_5.5.1.192_android_r98080_20150616175052_release_QZGW_D.apk";
//        originalFileName = "weixin622android580.apk";
//        String recvFileName = "recvFile";
        String recvFileName = originalFileName;
        File recvFile = new File(recvFilePath, recvFileName);
//        RandomAccessFile tempRecvFile;
//        try {
//            tempRecvFile = new RandomAccessFile(recvFile, "rwd");
//        } catch (Exception e) {
//            signalActivity("Exception during creating RA file:" + e.toString());
//            return;
//        }

        /*initialize TaskScheduler*/
        TaskScheduler taskScheduler = new TaskScheduler(totalLen, url, slaveNum);

        tm.startTimer();
        /*start master thread*/
        Thread masterThd = new Thread(new MasterTaskThread(0, taskScheduler, recvFile, this, tm, chunkSize, minChunkSize));
        try {
            taskScheduler.semaphoreMasterDone.acquire();
//            taskScheduler.semaphoreSlaveDone.acquire();
        } catch (Exception e) {
            signalActivity("Exception during accquring master lock:" + e.toString());
        }
        masterThd.start();

        /*start slave thread*/
//        TcpConnector conn = null;
//        try {
//            conn = new TcpConnector(slaveSockAddr, masterSockAddr, this, 0);
//            Thread slaveThd = new Thread(new SlaveTaskThread(taskScheduler, recvFile, conn, chunkSize, minChunkSize));
//            taskScheduler.semaphoreSlaveDone.acquire();
//            slaveThd.start();
//        } catch (Exception e) {
//            this.signalActivity("Exception during slave transmission:" + e.toString());
//        }

        /*start slave thread for all slaves*/
        ArrayList<TcpConnector> connList = new ArrayList<TcpConnector>();
        for (int i = 0; i < slaveNum; i ++) {
            try {
                TcpConnector tempConn = new TcpConnector(slaveSockAddrdList.get(i), masterSockAddr, this, 0);
                connList.add(tempConn);
                Thread slaveThd = new Thread(new SlaveTaskThread(i, taskScheduler, recvFile, tempConn, chunkSize, minChunkSize));
                taskScheduler.semaphoreSlaveDone.acquire();
                slaveThd.start();
            } catch (Exception e) {
                this.signalActivity("Exception during slave transmission:" + e.toString());
            }
        }

        /* When transmission is done, close file and stop slave*/
        try {
//            Thread.sleep(1000);
            taskScheduler.semaphoreMasterDone.acquire();
//            taskScheduler.semaphoreSlaveDone.acquire();
            for (int i = 0; i < slaveNum; i ++) {
                taskScheduler.semaphoreSlaveDone.acquire();
            }

            long totalTime = tm.getTimeLapse();
            int avgBw = (int)((float)1000 * ((float)taskScheduler.leftTask.totalLen/(float)((int)totalTime * 1024)));
            for (int i = 0; i < slaveNum; i ++) {
                MasterOperation.remoteStop(connList.get(i));
                connList.get(i).close();
            }

//            tempRecvFile.close();
            taskScheduler.semaphoreMasterDone.release();
//            taskScheduler.semaphoreSlaveDone.release();
            for (int i = 0; i < slaveNum; i ++) {
                taskScheduler.semaphoreSlaveDone.release();
            }
            this.signalActivityComplete();
            this.signalActivity("All tasks have been done, downloading complete! Time consume: " + Long.toString(totalTime / (long) 1000) + " (s) | Avg bw: " + Integer.toString(avgBw) + "KB/s");
        } catch (Exception e){
            this.signalActivity("Exception during closing file:" + e.toString());
        }
    }

}
