package edu.pdx.cs410.wifi.direct.file.transfer.master;

import android.os.Bundle;
import android.os.ResultReceiver;

import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.util.concurrent.Semaphore;

import edu.pdx.cs410.wifi.direct.file.transfer.TaskScheduler;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;

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

    public MasterTaskThread(TaskScheduler ts, RandomAccessFile raf, InetSocketAddress ssa, InetSocketAddress msa, MultithreadMasterService ms){
        taskScheduler = ts;
        tempRecvFile = raf;
        slaveSockAddr = ssa;
        masterSockAddr = msa;
        masterService = ms;
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
            DownloadTask retTasks[];
            try {
//                retTasks = taskScheduler.scheduleTask(4 * 1024);
                retTasks = taskScheduler.scheduleTask(100 * 1024);
                mTask = retTasks[0];
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
                    taskScheduler.updateMasterBw(masterBw);
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
            /* no need to stop slave */
        } catch (Exception e) {
            masterService.signalActivity("Exception during closing file:" + e.toString());
        }
    }
}
