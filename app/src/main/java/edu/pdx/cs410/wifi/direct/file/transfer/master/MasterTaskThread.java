package edu.pdx.cs410.wifi.direct.file.transfer.master;

import android.os.ResultReceiver;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.BackendService;
import edu.pdx.cs410.wifi.direct.file.transfer.Log;
import edu.pdx.cs410.wifi.direct.file.transfer.Statistic;
import edu.pdx.cs410.wifi.direct.file.transfer.TaskScheduler;
import edu.pdx.cs410.wifi.direct.file.transfer.TimeMetric;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;

/**
 * Created by User on 7/24/2015.
 */
public class MasterTaskThread extends Thread {
    TaskScheduler taskScheduler;
    File recvFile;
    InetSocketAddress slaveSockAddr;
    InetSocketAddress masterSockAddr;
    BackendService masterService;
    long chunkSize;
    long minChunkSize;
    ResultReceiver masterResult;
    TimeMetric time;
    Statistic stat;
    int threadIndex;

    public MasterTaskThread(int ti, TaskScheduler ts, File f, BackendService ms, TimeMetric tm, long ckSize, long minCkSize) {
        threadIndex = ti;
        taskScheduler = ts;
        recvFile = f;
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
        Log log = new Log("master_" + Integer.valueOf(threadIndex) + "_log");
        stat = new Statistic("master_" + Integer.valueOf(threadIndex) + "_stat");

        while (true) {
            try {
                isDone = taskScheduler.isTaskDone();
                if (isDone) {
                    log.record("master task " + Integer.valueOf(threadIndex) + " done");
                    taskScheduler.semaphoreMasterDone.release();
//                    taskScheduler.semaphoreSlaveDone.release();
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
                if (mTask != null) {
                    log.record("cur start:" + Long.toString(mTask.start) + "|cur end:" + Long.toString(mTask.end)
                            + "|cur left start:" + Long.toString(taskScheduler.leftTask.start)
                            + "|cur left end:" + Long.toString(taskScheduler.leftTask.end));
                } else {
                    log.record("cur task is null|cur left start:" + Long.toString(taskScheduler.leftTask.start)
                            + "|cur left end:" + Long.toString(taskScheduler.leftTask.end));
                }
            } catch (Exception e) {
                masterService.signalActivity("Exception during task scheduling:" + e.toString());
                return;
            }

            /*execute downloading*/
            try {
                if (mTask != null) {
                    /*execute downloading*/
                    tempRecvFile = new RandomAccessFile(recvFile, "rwd");
                    tempRecvFile.seek(mTask.start);
                    masterBw = MasterOperation.httpDownload(mTask, tempRecvFile, masterService, stat);
                    tempRecvFile.close();

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
            masterService.signalActivityProgress("Master " + Integer.valueOf(threadIndex) + " Data Per:"
                                                + masterPer + "% | mBw:" + taskScheduler.mBw + "KB/s, sBw:"
                                                + taskScheduler.sBw + "KB/s | Progress:" + progress + "% | Task left:"
                                                + Long.toString(taskScheduler.leftTask.end - taskScheduler.leftTask.start));
        }
    }
}
