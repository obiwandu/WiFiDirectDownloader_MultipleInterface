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
    File recvFile;
    InetSocketAddress slaveSockAddr;
    InetSocketAddress masterSockAddr;
    ResultReceiver masterResult;
    TcpConnector conn;
    long chunkSize;
    long minChunkSize;
    Statistic stat;
    int slaveIndex;

    public SlaveTaskThread(Statistic s, int si, TaskScheduler ts, File f, TcpConnector c, long ckSize, long minCkSize) {
        stat = s;
        slaveIndex = si;
        taskScheduler = ts;
        recvFile = f;
        conn = c;
        chunkSize = ckSize;
        minChunkSize = minCkSize;
    }

    public void run() {
        long dataCount = 0;
        long slaveBw = 0;
        RandomAccessFile tempRecvFile;
        boolean isDone;
        Log log = new Log("slave_" + Integer.valueOf(slaveIndex) + "_log");
        stat.addThread("slave_" + Integer.valueOf(slaveIndex) + "_stat");
//        stat = new Statistic("slave_" + Integer.valueOf(slaveIndex) +"_stat");


        while (true) {
            try {
                /* Judge whether all tasks have been done */
                isDone = taskScheduler.isTaskDone();
                if (isDone) {
                    log.record("slave " + Integer.valueOf(slaveIndex) + " task done");
                    /* Stop statistics timer */
//                    try {
//                        stat.stopTimer();
//                    } catch (Exception e) {
//                        conn.backendService.signalActivity("Exception in stopping timer:" + e.toString());
//                    }
                    taskScheduler.semaphoreSlaveDone.release();
                    break;
                }
            } catch (Exception e) {
                conn.backendService.signalActivity("Exception during terminal condition fetching:" + e.toString());
            }

            /* Schedule task */
            DownloadTask sTask;
            DownloadTask retTasks;
            try {
                retTasks = taskScheduler.scheduleTask(chunkSize, minChunkSize, false);
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
                    tempRecvFile = new RandomAccessFile(recvFile, "rwd");
                    tempRecvFile.seek(sTask.start);
                    slaveBw = MasterOperation.remoteDownload(sTask, tempRecvFile, conn, stat);
                    tempRecvFile.close();
                    dataCount += sTask.end - sTask.start + 1;
                    /*submit tasks*/
                    taskScheduler.updateSlaveBw(slaveBw);
                }
            } catch (Exception e) {
                conn.backendService.signalActivity("Exception during remote downloading:" + e.toString());
                return;
            }

            float totalLen = (float)taskScheduler.leftTask.totalLen;
            float alreadyLen = (float)taskScheduler.leftTask.start;
            float progress = (alreadyLen * (float)100)/totalLen;
            float slavePer = (float)(100*dataCount)/(float)taskScheduler.leftTask.totalLen;
            conn.backendService.signalActivityProgress("Slave " + Integer.valueOf(slaveIndex) + " Data Per:"
                                                        + slavePer + "% | mBw:" + taskScheduler.mBw + "KB/s, sBw:"
                                                        + taskScheduler.sBw + "KB/s | Progress:" + progress + "% | Task left:"
                                                        + Long.toString(taskScheduler.leftTask.end - taskScheduler.leftTask.start));
        }
    }
}
