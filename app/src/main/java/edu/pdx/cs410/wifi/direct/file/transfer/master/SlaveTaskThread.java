package edu.pdx.cs410.wifi.direct.file.transfer.master;

import android.os.ResultReceiver;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.Log;
import edu.pdx.cs410.wifi.direct.file.transfer.Statistic;
import edu.pdx.cs410.wifi.direct.file.transfer.TaskScheduler;
import edu.pdx.cs410.wifi.direct.file.transfer.TimeMetric;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpConnector;

/**
 * Created by User on 7/24/2015.
 */
public class SlaveTaskThread extends Thread {
    TaskScheduler taskScheduler;
//    RandomAccessFile tempRecvFile;
    File recvFile;
    InetSocketAddress slaveSockAddr;
    InetSocketAddress masterSockAddr;
//    MultithreadMasterService masterService;
    ResultReceiver masterResult;
    TcpConnector conn;
//    TimeMetric time;
    long chunkSize;
    long minChunkSize;
    Statistic stat;

    public SlaveTaskThread(TaskScheduler ts, File f, TcpConnector c, long ckSize, long minCkSize) {
        taskScheduler = ts;
        recvFile = f;
//        slaveSockAddr = ssa;
//        masterSockAddr = msa;
//        masterService = ms;
        conn = c;
//        time = tm;
        chunkSize = ckSize;
        minChunkSize = minCkSize;

//        try {
//            conn = new TcpConnector(ssa, msa, ms, 0);
//        } catch (Exception e) {
//            masterService.signalActivity("Exception during setting up data connection:" + e.toString());
//        }
    }

    public void run() {
        long dataCount = 0;
        long slaveBw = 0;
        RandomAccessFile tempRecvFile;
        boolean isDone;
        Log log = new Log("slave_log");
        stat = new Statistic("slave_stat");

//        try {
//            taskScheduler.semaphoreSlaveDone.acquire();
//        } catch (Exception e) {
//            conn.backendService.signalActivity("Exception during accquring slave lock:" + e.toString());
//        }
        while (true) {
            try {
                isDone = taskScheduler.isTaskDone();
                if (isDone) {
                    log.record("slave task done");
                    taskScheduler.semaphoreSlaveDone.release();
                    break;
                }
            } catch (Exception e) {
                conn.backendService.signalActivity("Exception during terminal condition fetching:" + e.toString());
            }

            /*schedule task*/
            DownloadTask sTask;
            DownloadTask retTasks;
            try {
                retTasks = taskScheduler.scheduleTask(chunkSize, minChunkSize, false);
//                retTasks = taskScheduler.scheduleTask(taskScheduler.leftTask.end - taskScheduler.leftTask.start + 1, true);
                sTask = retTasks;
                if (sTask != null) {
                    log.record("cur start:" + Long.toString(sTask.start) + "|cur end:" + Long.toString(sTask.end)
                            + "|cur left start:" + Long.toString(taskScheduler.leftTask.start)
                            + "|cur left end:" + Long.toString(taskScheduler.leftTask.end));
                } else {
                    log.record("cur task is null|cur left start:" + Long.toString(taskScheduler.leftTask.start)
                            + "|cur left end:" + Long.toString(taskScheduler.leftTask.end));
                }
            } catch (Exception e) {
                conn.backendService.signalActivity("Exception during task scheduling:" + e.toString());
                return;
            }

            /*execute downloading*/
            try {
                if (sTask != null) {

                    /*execute downloading*/
//                    taskScheduler.semaphore.acquire();
                    tempRecvFile = new RandomAccessFile(recvFile, "rwd");
                    tempRecvFile.seek(sTask.start);
                    slaveBw = MasterOperation.remoteDownload(sTask, tempRecvFile, conn, stat);
                    tempRecvFile.close();
//                    taskScheduler.semaphore.release();
                    dataCount += sTask.end - sTask.start + 1;
                    /*submit tasks*/
                    taskScheduler.updateSlaveBw(slaveBw);
                }
            } catch (Exception e) {
                conn.backendService.signalActivity("Exception during remote downloading:" + e.toString());
                return;
            }

//            masterService.signalActivityProgress("Task left:" + Integer.toString(taskScheduler.leftTask.end - taskScheduler.leftTask.start));
            float totalLen = (float)taskScheduler.leftTask.totalLen;
            float alreadyLen = (float)taskScheduler.leftTask.start;
            float progress = (alreadyLen * (float)100)/totalLen;
            float slavePer = (float)(100*dataCount)/(float)taskScheduler.leftTask.totalLen;
            conn.backendService.signalActivityProgress("Slave Data Per:" + slavePer + "% | mBw:" + taskScheduler.mBw + "KB/s, sBw:" + taskScheduler.sBw + "KB/s | Progress:" + progress + "% | Task left:" + Long.toString(taskScheduler.leftTask.end - taskScheduler.leftTask.start));
        }

        /* When transmission is done, close file and stop slave*/
//        try {
//            taskScheduler.semaphoreMasterDone.acquire();
//            taskScheduler.semaphoreSlaveDone.acquire();
//            long totalTime = time.getTimeLapse();
//            int avgBw = (int)((float)1000 * ((float)taskScheduler.leftTask.totalLen/(float)((int)totalTime * 1024)));
//            tempRecvFile.close();
//            MasterOperation.remoteStop(conn);
//            taskScheduler.semaphoreMasterDone.release();
//            taskScheduler.semaphoreSlaveDone.release();
//            conn.backendService.signalActivity("All tasks have been done, downloading complete! Time consume: " + Long.toString(totalTime/(long)1000) + " (s) | Avg bw: " + Integer.toString(avgBw) + "KB/s");
//        } catch (Exception e) {
//            conn.backendService.signalActivity("Exception during closing file:" + e.toString());
//        }
    }
}
