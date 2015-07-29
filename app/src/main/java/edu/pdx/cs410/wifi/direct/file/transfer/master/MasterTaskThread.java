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
        int dataCount = 0;
        long masterBw = 0;
        boolean isDone;

        try {
            taskScheduler.semaphoreMasterDone.acquire();
        } catch (Exception e) {
            masterService.signalActivity("Exception during accquring master lock:" + e.toString());
        }
        while (true) {
            try {
                isDone = taskScheduler.isTaskDone();
                if (isDone) {
                    taskScheduler.semaphoreMasterDone.release();
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
                    dataCount += mTask.end - mTask.start + 1;
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
            float masterPer = (float)(100*dataCount)/(float)taskScheduler.leftTask.totalLen;
            masterService.signalActivityProgress("Master Data Per:" + masterPer + "% | mBw:" + taskScheduler.mBw + "KB/s, sBw:" + taskScheduler.sBw + "KB/s | Progress:" + progress + "% | Task left:" + Long.toString(taskScheduler.leftTask.end - taskScheduler.leftTask.start));
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
