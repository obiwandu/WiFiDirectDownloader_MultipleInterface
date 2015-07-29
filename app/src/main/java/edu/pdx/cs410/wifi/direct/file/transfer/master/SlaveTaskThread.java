package edu.pdx.cs410.wifi.direct.file.transfer.master;

import android.os.ResultReceiver;

import java.io.RandomAccessFile;
import java.net.InetSocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.TaskScheduler;
import edu.pdx.cs410.wifi.direct.file.transfer.TimeMetric;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpConnector;

/**
 * Created by User on 7/24/2015.
 */
public class SlaveTaskThread extends Thread {
    TaskScheduler taskScheduler;
    RandomAccessFile tempRecvFile;
    InetSocketAddress slaveSockAddr;
    InetSocketAddress masterSockAddr;
    MultithreadMasterService masterService;
    ResultReceiver masterResult;
    TcpConnector conn;
    TimeMetric time;
    long chunkSize;
    long minChunkSize;

    public SlaveTaskThread(TaskScheduler ts, RandomAccessFile raf, InetSocketAddress ssa, InetSocketAddress msa, MultithreadMasterService ms, TimeMetric tm, long ckSize, long minCkSize) {
        taskScheduler = ts;
        tempRecvFile = raf;
        slaveSockAddr = ssa;
        masterSockAddr = msa;
        masterService = ms;
        time = tm;
        chunkSize = ckSize;
        minChunkSize = minCkSize;

        try {
            conn = new TcpConnector(ssa, msa, ms, 0);
        } catch (Exception e) {
            masterService.signalActivity("Exception during setting up data connection:" + e.toString());
        }
    }

    public void run() {
        long dataCount = 0;
        long slaveBw = 0;
        boolean isDone;

        try {
            taskScheduler.semaphoreSlaveDone.acquire();
        } catch (Exception e) {
            masterService.signalActivity("Exception during accquring slave lock:" + e.toString());
        }
        while (true) {
            try {
                isDone = taskScheduler.isTaskDone();
                if (isDone) {
                    taskScheduler.semaphoreSlaveDone.release();
                    break;
                }
            } catch (Exception e) {
                masterService.signalActivity("Exception during terminal condition fetching:" + e.toString());
            }

            /*schedule task*/
            DownloadTask sTask;
            DownloadTask retTasks;
            try {
//                retTasks = taskScheduler.scheduleTask(4 * 1024);
//                retTasks = taskScheduler.scheduleTask(100 * 1024, true);
                retTasks = taskScheduler.scheduleTask(chunkSize, minChunkSize, false);
//                retTasks = taskScheduler.scheduleTask(taskScheduler.leftTask.end - taskScheduler.leftTask.start + 1, true);
                sTask = retTasks;
            } catch (Exception e) {
                masterService.signalActivity("Exception during task scheduling:" + e.toString());
                return;
            }

            /*execute downloading*/
            try {
                if (sTask != null) {
                    tempRecvFile.seek(sTask.start);
                    /*execute downloading*/
//                    slaveBw = MasterOperation.remoteDownload(sTask, tempRecvFile, slaveSockAddr, masterSockAddr, masterService);
                    slaveBw = MasterOperation.remoteDownload(sTask, tempRecvFile, conn);
                    dataCount += sTask.end - sTask.start + 1;
                    /*submit tasks*/
                    taskScheduler.updateSlaveBw(slaveBw);
                }
            } catch (Exception e) {
                masterService.signalActivity("Exception during remote downloading:" + e.toString());
                return;
            }

//            masterService.signalActivityProgress("Task left:" + Integer.toString(taskScheduler.leftTask.end - taskScheduler.leftTask.start));
            float totalLen = (float)taskScheduler.leftTask.totalLen;
            float alreadyLen = (float)taskScheduler.leftTask.start;
            float progress = (alreadyLen * (float)100)/totalLen;
            float slavePer = (float)(100*dataCount)/(float)taskScheduler.leftTask.totalLen;
            masterService.signalActivityProgress("Slave Data Per:" + slavePer + "% | mBw:" + taskScheduler.mBw + "KB/s, sBw:" + taskScheduler.sBw + "KB/s | Progress:" + progress + "% | Task left:" + Long.toString(taskScheduler.leftTask.end - taskScheduler.leftTask.start));
        }

        /* When transmission is done, close file and stop slave*/
        try {
            taskScheduler.semaphoreMasterDone.acquire();
            taskScheduler.semaphoreSlaveDone.acquire();
            long totalTime = time.getTimeLapse();
            int avgBw = (int)((float)1000 * ((float)taskScheduler.leftTask.totalLen/(float)((int)totalTime * 1024)));
            tempRecvFile.close();
            MasterOperation.remoteStop(conn);
            taskScheduler.semaphoreMasterDone.release();
            taskScheduler.semaphoreSlaveDone.release();
            masterService.signalActivity("All tasks have been done, downloading complete! Time consume: " + Long.toString(totalTime/(long)1000) + " (s) | Avg bw: " + Integer.toString(avgBw) + "KB/s");
        } catch (Exception e) {
            masterService.signalActivity("Exception during closing file:" + e.toString());
        }
    }
}
