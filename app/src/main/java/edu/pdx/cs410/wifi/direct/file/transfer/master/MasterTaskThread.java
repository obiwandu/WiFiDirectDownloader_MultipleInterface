package edu.pdx.cs410.wifi.direct.file.transfer.master;

import android.os.ResultReceiver;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.BackendService;
import edu.pdx.cs410.wifi.direct.file.transfer.Log;
import edu.pdx.cs410.wifi.direct.file.transfer.TaskScheduler;
import edu.pdx.cs410.wifi.direct.file.transfer.TimeMetric;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;

/**
 * Created by User on 7/24/2015.
 */
public class MasterTaskThread extends Thread {
    TaskScheduler taskScheduler;
    //    RandomAccessFile tempRecvFile;
    File recvFile;
    InetSocketAddress slaveSockAddr;
    InetSocketAddress masterSockAddr;
    //MultithreadMasterService masterService;
    BackendService masterService;
    long chunkSize;
    long minChunkSize;
    ResultReceiver masterResult;
    TimeMetric time;
//    TcpConnectorLong conn;

    public MasterTaskThread(TaskScheduler ts, File f, BackendService ms, TimeMetric tm, long ckSize, long minCkSize) {
        taskScheduler = ts;
        recvFile = f;
//        slaveSockAddr = ssa;
//        masterSockAddr = msa;
        masterService = ms;
        time = tm;
        chunkSize = ckSize;
        minChunkSize = minCkSize;
    }

    public void run() {
        int dataCount = 0;
        long masterBw = 0;
        boolean isDone;
        RandomAccessFile tempRecvFile;
        Log log = new Log("master1_log");

//        try {
//            taskScheduler.semaphoreMasterDone.acquire();
//        } catch (Exception e) {
//            masterService.signalActivity("Exception during accquring master lock:" + e.toString());
//        }
        while (true) {
            try {
                isDone = taskScheduler.isTaskDone();
                if (isDone) {
                    log.record("master task 1 done");
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
                retTasks = taskScheduler.scheduleTask(chunkSize, minChunkSize, true);
                mTask = retTasks;
                log.record("cur start:" + Long.toString(mTask.start) + "|cur end:" + Long.toString(mTask.end)
                        + "|cur left start:" + Long.toString(taskScheduler.leftTask.start)
                        + "|cur left end:" + Long.toString(taskScheduler.leftTask.end));
            } catch (Exception e) {
                masterService.signalActivity("Exception during task scheduling:" + e.toString());
                return;
            }

            /*execute downloading*/
            try {
                if (mTask != null) {
                    /*execute downloading*/
//                    taskScheduler.semaphore.acquire();
                    tempRecvFile = new RandomAccessFile(recvFile, "rwd");
                    tempRecvFile.seek(mTask.start);
                    masterBw = MasterOperation.httpDownload(mTask, tempRecvFile, masterService);
                    tempRecvFile.close();
//                    taskScheduler.semaphore.release();
                    dataCount += mTask.end - mTask.start + 1;
                    /*submit tasks*/
                    taskScheduler.updateMasterBw(masterBw);
                }
            } catch (Exception e) {
                masterService.signalActivity("Exception during local downloading:" + e.toString());
                return;
            }

            float totalLen = (float) taskScheduler.leftTask.totalLen;
            float alreadyLen = (float) taskScheduler.leftTask.start;
            float progress = (alreadyLen * (float) 100) / totalLen;
            float masterPer = (float) (100 * dataCount) / (float) taskScheduler.leftTask.totalLen;
            masterService.signalActivityProgress("Master Data Per:" + masterPer + "% | mBw:" + taskScheduler.mBw + "KB/s, sBw:" + taskScheduler.sBw + "KB/s | Progress:" + progress + "% | Task left:" + Long.toString(taskScheduler.leftTask.end - taskScheduler.leftTask.start));
        }

        /* When transmission is done, close file and stop slave*/
//        try {
////            taskScheduler.semaphoreMasterDone.acquire();
////            taskScheduler.semaphoreSlaveDone.acquire();
//            long totalTime = time.getTimeLapse();
//            int avgBw = (int)((float)1000 * ((float)taskScheduler.leftTask.totalLen/(float)((int)totalTime * 1024)));
//            tempRecvFile.close();
////            MasterOperation.remoteStop(conn);
////            taskScheduler.semaphoreMasterDone.release();
////            taskScheduler.semaphoreSlaveDone.release();
//            masterService.signalActivity("All tasks have been done, downloading complete! Time consume: " + Long.toString(totalTime/(long)1000) + " (s) | Avg bw: " + Integer.toString(avgBw) + "KB/s");
//        } catch (Exception e) {
//            masterService.signalActivity("Exception during closing file:" + e.toString());
//        }
    }
}
