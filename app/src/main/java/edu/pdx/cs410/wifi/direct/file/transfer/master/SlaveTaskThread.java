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

    public SlaveTaskThread(TaskScheduler ts, RandomAccessFile raf, InetSocketAddress ssa, InetSocketAddress msa, MultithreadMasterService ms, TimeMetric tm) {
        taskScheduler = ts;
        tempRecvFile = raf;
        slaveSockAddr = ssa;
        masterSockAddr = msa;
        masterService = ms;
        time = tm;

        try {
            conn = new TcpConnector(ssa, msa, ms, 0);
        } catch (Exception e) {
            masterService.signalActivity("Exception during setting up data connection:" + e.toString());
        }
    }

    public void run() {
        int slaveBw;
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
            DownloadTask sTask;
            DownloadTask retTasks;
            try {
//                retTasks = taskScheduler.scheduleTask(4 * 1024);
                retTasks = taskScheduler.scheduleTask(100 * 1024, true);
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
                    /*submit tasks*/
                    taskScheduler.updateMasterBw(slaveBw);
                }
            } catch (Exception e) {
                masterService.signalActivity("Exception during remote downloading:" + e.toString());
                return;
            }

//            masterService.signalActivityProgress("Task left:" + Integer.toString(taskScheduler.leftTask.end - taskScheduler.leftTask.start));
            float totalLen = (float)taskScheduler.leftTask.totalLen;
            float alreadyLen = (float)taskScheduler.leftTask.start;
            float progress = (alreadyLen * (float)100)/totalLen;
            masterService.signalActivityProgress("Progress:" + progress + "% | Task left:" + Integer.toString(taskScheduler.leftTask.end - taskScheduler.leftTask.start));
        }

        /* when transmission is done, close file and stop slave*/
        try {
            long totalTime = time.getTimeLapse();
            int avgBw = (int)((float)1000 * ((float)taskScheduler.leftTask.totalLen/(float)((int)totalTime * 1024)));
            tempRecvFile.close();
            MasterOperation.remoteStop(conn);
            masterService.signalActivity("All tasks have been done, downloading complete! Time consume: " + Long.toString(totalTime/(long)1000) + " (s) | Avg bw: " + Integer.toString(avgBw) + "KB/s");
            /* no need to stop slave */
        } catch (Exception e) {
            masterService.signalActivity("Exception during closing file:" + e.toString());
        }
    }
}
