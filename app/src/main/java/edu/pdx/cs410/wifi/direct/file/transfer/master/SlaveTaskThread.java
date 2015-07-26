package edu.pdx.cs410.wifi.direct.file.transfer.master;

import android.os.ResultReceiver;

import java.io.RandomAccessFile;
import java.net.InetSocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.TaskScheduler;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpConnectorLong;

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
    TcpConnectorLong conn;

    public SlaveTaskThread(TaskScheduler ts, RandomAccessFile raf, InetSocketAddress ssa, InetSocketAddress msa, MultithreadMasterService ms) {
        taskScheduler = ts;
        tempRecvFile = raf;
        slaveSockAddr = ssa;
        masterSockAddr = msa;
        masterService = ms;

        try {
            conn = new TcpConnectorLong(ssa, msa, ms, 0);
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
//                retTasks = taskScheduler.scheduleTask(100 * 1024, true);
                retTasks = taskScheduler.scheduleTask(taskScheduler.leftTask.end - taskScheduler.leftTask.start + 1, true);
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
                    slaveBw = MasterOperation.remoteDownload(conn, sTask, tempRecvFile, slaveSockAddr, masterSockAddr, masterService);
                    taskScheduler.updateMasterBw(slaveBw);
                }
            } catch (Exception e) {
                masterService.signalActivity("Exception during remote downloading:" + e.toString());
                return;
            }

            /*submit tasks*/
            masterService.signalActivityProgress("Task left:" + Integer.toString(taskScheduler.leftTask.end - taskScheduler.leftTask.start));
        }

        /* when transmission is done, close file and stop slave*/
        try {
            tempRecvFile.close();
            MasterOperation.remoteStop(conn, masterService);
            /* no need to stop slave */
        } catch (Exception e) {
            masterService.signalActivity("Exception during closing file:" + e.toString());
        }
    }
}
