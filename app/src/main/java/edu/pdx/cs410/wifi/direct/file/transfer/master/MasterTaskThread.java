package edu.pdx.cs410.wifi.direct.file.transfer.master;

import android.os.Bundle;
import android.os.ResultReceiver;

import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.util.concurrent.Semaphore;

import edu.pdx.cs410.wifi.direct.file.transfer.TaskScheduler;
import edu.pdx.cs410.wifi.direct.file.transfer.TimeMetric;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpConnectorLong;

/**
 * Created by User on 7/24/2015.
 */
public class MasterTaskThread extends Thread{
    TaskScheduler taskScheduler;
    RandomAccessFile tempRecvFile;
    InetSocketAddress slaveSockAddr;
    InetSocketAddress masterSockAddr;
    MultithreadMasterService masterService;
    ResultReceiver masterResult;
    TimeMetric time;
//    TcpConnectorLong conn;

    public MasterTaskThread(TaskScheduler ts, RandomAccessFile raf, InetSocketAddress ssa, InetSocketAddress msa, MultithreadMasterService ms, TimeMetric tm){
        taskScheduler = ts;
        tempRecvFile = raf;
        slaveSockAddr = ssa;
        masterSockAddr = msa;
        masterService = ms;
        time = tm;
    }

    public void run() {
        int masterBw;
        boolean isDone;
        while (true) {
            try {
                isDone = taskScheduler.isTaskDone();
                if (isDone) {
                    break;
                }
            } catch (Exception e) {
                masterService.signalActivity("Exception during terminal condition fetching:" + e.toString());
            }

            /*schedule task*/
            DownloadTask mTask;
            DownloadTask retTasks;
            try {
//                retTasks = taskScheduler.scheduleTask(4 * 1024);
                retTasks = taskScheduler.scheduleTask(100 * 1024, true);
                mTask = retTasks;
            } catch (Exception e) {
                masterService.signalActivity("Exception during task scheduling:" + e.toString());
                return;
            }

            /*execute downloading*/
            try {
                if (mTask != null) {
                    tempRecvFile.seek(mTask.start);
                    /*execute downloading*/
                    masterBw = MasterOperation.httpDownload(mTask, tempRecvFile, masterService);
                    /*submit tasks*/
                    taskScheduler.updateMasterBw(masterBw);
                }
            } catch (Exception e) {
                masterService.signalActivity("Exception during local downloading:" + e.toString());
                return;
            }

            float totalLen = (float)taskScheduler.leftTask.totalLen;
            float alreadyLen = (float)taskScheduler.leftTask.start;
            float progress = (alreadyLen * (float)100)/totalLen;
            masterService.signalActivityProgress("Progress:" + progress + "% | Task left:" + Integer.toString(taskScheduler.leftTask.end - taskScheduler.leftTask.start));
        }

        /* when transmission is done, close file and stop slave*/
//        try {
//            tempRecvFile.close();
//            /* no need to stop slave */
////            MasterOperation.remoteStop(conn, masterService);
//        } catch (Exception e) {
//            masterService.signalActivity("Exception during closing file:" + e.toString());
//        }
    }
}
