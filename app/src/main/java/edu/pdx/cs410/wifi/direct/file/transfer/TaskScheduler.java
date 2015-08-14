package edu.pdx.cs410.wifi.direct.file.transfer;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;

/**
 * Created by User on 7/21/2015.
 */
public class TaskScheduler {
    private long totalLen;
    //    private ArrayList<DownloadTask> leftTaskList;
    public DownloadTask leftTask;
    private DownloadTask[] taskList;
    //    public long mBw;
//    public long sBw;
//    public long mAlreadyBytes;
//    public long sAlreadyBytes;
//    public TimeMetric mTm;
//    public TimeMetric sTm;
    private String url;
    public static Semaphore semaphore;
    public static Semaphore semaphoreMasterDone;
    //    public static Semaphore semaphoreMasterDone2;
    public static Semaphore semaphoreSlaveDone;
    public ArrayList<DownloadTask> alreadyTaskList;
    public Statistic stat;

    public TaskScheduler(long tl, String l, int n, Statistic s) {
//        mAlreadyBytes = 0;
//        sAlreadyBytes = 0;
//        mTm = new TimeMetric();
//        sTm = new TimeMetric();
        semaphore = new Semaphore(1);
        semaphoreMasterDone = new Semaphore(n);
        semaphoreSlaveDone = new Semaphore(n);

        stat = s;
        totalLen = tl;
//        leftTaskList.add(new DownloadTask(0, totalLen - 1, true, url));
        leftTask = new DownloadTask(0, totalLen - 1, totalLen, l);
        url = l;
    }

    public boolean isTaskDone() throws Exception {
        boolean isDone;

        semaphore.acquire();
        isDone = leftTask.isDone;
        semaphore.release();

        return isDone;
    }

    public DownloadTask scheduleTask(long threadId, long baseLen, long minBaseLen, boolean isMaster) throws Exception {
        if (!leftTask.isDone) {
            DownloadTask retTask;
            long leftLen = leftTask.end - leftTask.start + 1;
            long curBw = stat.getThreadStat(threadId).bw;
            int threadNum = stat.threadNum;
            long[] bwArray = stat.toBwArray();

            if (leftLen <= baseLen) {
                /* Last chunk scheduling, only choose one end with highest bw to handle the last chunk */
                boolean isHighest = true;
                for (long temp : bwArray) {
                    if (temp > curBw) {
                        isHighest = false;
                    }
                }
                if (isHighest) {
                    semaphore.acquire();
                    retTask = leftTask.schedule(leftLen);
                    semaphore.release();
                } else {
                    retTask = null;
                }

                return retTask;
            } else {
                /* General scheduling */
                long curTotalTaskLen;
                long nextTaskLen;

                /* Count number of threads with BW 0 and sum of BW on all threads */
                long sumBw = 0;
                int zeroBwNum = 0;
                for (long temp : bwArray) {
                    sumBw += temp;
                    if (temp == 0) {
                        zeroBwNum ++;
                    }
                }
                long threadsholdLen = (threadNum - zeroBwNum) * baseLen + zeroBwNum * minBaseLen;

                if (leftLen <= threadsholdLen && leftLen > baseLen) {
                    /* Tasks are going to be finished */
                    curTotalTaskLen = leftLen;
                } else {
                    /* General scheduling */
                    curTotalTaskLen = threadsholdLen;
                }

                /* calculate tasks according to bandwidth */
                if (zeroBwNum == 0) {
                    /* All threads have non 0 BW */
                    nextTaskLen = (long) ((float) curTotalTaskLen * ((float) curBw / (float) sumBw));
                } else if(zeroBwNum == threadNum) {
                    /* All threads have 0 BW, initial state */
                    nextTaskLen = baseLen;
                } else{
                    if (curBw == 0) {
                        nextTaskLen = minBaseLen;
                    } else {
                        nextTaskLen = (long)((float)(curTotalTaskLen - zeroBwNum * minBaseLen) *  ((float) curBw / (float) sumBw));
                    }
                }

                /*schedule new tasks*/
                semaphore.acquire();
                retTask = leftTask.schedule(nextTaskLen);
                semaphore.release();

                return retTask;
            }
        } else {
            /* All tasks have been done */
            Exception e = new Exception("All tasks have been done");
            throw e;
        }
    }

//    public DownloadTask[] scheduleSingleTask(int baseLen) throws Exception {
////        semaphore.acquire();
//        if (!leftTask.isDone) {
//            DownloadTask[] retTask = new DownloadTask[2];
//            long leftLen = leftTask.end - leftTask.start + 1;
//
//            if (leftLen <= baseLen) {
//                /* last scheduling, only a small part of task left */
//                if (mBw >= sBw) {
//                    retTask[0] = leftTask.schedule(leftLen);
//                    retTask[1] = null;
//                } else {
//                    retTask[0] = null;
//                    retTask[1] = leftTask.schedule(leftLen);
//                }
//
////                semaphore.release();
//                return retTask;
//            } else {
//                /* general scheduling */
//                long mLen;
//                long sLen;
//                long curTaskLen;
//
//                if (leftLen <= 2 * baseLen && leftLen > baseLen) {
//                    /* last scheduling */
//                    curTaskLen = leftLen;
//                } else {
//                    /* general scheduling */
//                    curTaskLen = 2 * baseLen;
//                }
//
//                /* calculate tasks according to bandwidth */
//                if (mBw != 0 && sBw != 0) {
//                    mLen = curTaskLen * mBw / (mBw + sBw);
//                    sLen = curTaskLen - mLen;
//                } else {
//                    if (mBw == 0 && sBw == 0) {
//                        mLen = curTaskLen / 2;
//                        sLen = curTaskLen - mLen;
//                    } else {
//                        if (mBw == 0) {
//                            mLen = 0;
//                            sLen = curTaskLen;
//                        } else {
//                            mLen = curTaskLen;
//                            sLen = 0;
//                        }
//                    }
//                }
//
//                /*schedule new tasks for master and slave*/
//                retTask[0] = leftTask.schedule(mLen);
//                retTask[1] = leftTask.schedule(sLen);
//
////                semaphore.release();
//                return retTask;
//            }
//        } else {
////            semaphore.release();
//            Exception e = new Exception("All tasks have been done");
//            throw e;
//        }
//    }

//    public void submitTask(long m, long s) {
//        mBw = m;
//        sBw = s;
////        semaphore.release();
//        return;
//    }

//    public void updateMasterBw(long m) throws Exception {
//        semaphore.acquire();
//        mBw = m;
//        semaphore.release();
//        return;
//    }
//
//    public void updateSlaveBw(long s) throws Exception {
//        semaphore.acquire();
//        sBw = s;
//        semaphore.release();
//        return;
//    }
}
