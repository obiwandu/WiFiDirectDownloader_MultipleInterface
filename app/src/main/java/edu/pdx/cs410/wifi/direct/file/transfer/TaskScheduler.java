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
    public long mBw;
    public long sBw;
    public long mAlreadyBytes;
    public long sAlreadyBytes;
    public TimeMetric mTm;
    public TimeMetric sTm;
    private String url;
    public static Semaphore semaphore;
    public static Semaphore semaphoreMasterDone;
    public static Semaphore semaphoreMasterDone2;
    public static Semaphore semaphoreSlaveDone;
    public ArrayList<DownloadTask> alreadyTaskList;

    public TaskScheduler(long tl, String l, int n) {
        mAlreadyBytes = 0;
        sAlreadyBytes = 0;
        mTm = new TimeMetric();
        sTm = new TimeMetric();
        semaphore = new Semaphore(1);
        semaphoreMasterDone = new Semaphore(n);
        semaphoreSlaveDone = new Semaphore(n);

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

    public DownloadTask scheduleTask(long baseLen, long minBaseLen, boolean isMaster) throws Exception {
        if (!leftTask.isDone) {
            DownloadTask retTask;
            long leftLen = leftTask.end - leftTask.start + 1;

            if (leftLen <= baseLen) {
                /* last chunk scheduling, only choose one end to handle the last chunk */
                if (mBw >= sBw) {
                    if (isMaster) {
                        semaphore.acquire();
                        retTask = leftTask.schedule(leftLen);
                        semaphore.release();
                    } else {
                        retTask = null;
                    }
                } else {
                    if (isMaster) {
                        retTask = null;
                    } else {
                        semaphore.acquire();
                        retTask = leftTask.schedule(leftLen);
                        semaphore.release();
                    }
                }

                return retTask;
            } else {
                /* general scheduling */
                long mLen;
                long sLen;
                long curTaskLen;

                if (leftLen <= 2 * baseLen && leftLen > baseLen) {
                    /* tasks are going to be finished */
                    curTaskLen = leftLen;
                } else {
                    /* general scheduling */
                    curTaskLen = 2 * baseLen;
                }

                /* calculate tasks according to bandwidth */
                if (mBw != 0 && sBw != 0) {
                    mLen = (long) ((float)curTaskLen * ((float)mBw / (float)(mBw + sBw)));
                    sLen = curTaskLen - mLen;
                } else {
                    if (mBw == 0 && sBw == 0) {
                        mLen = curTaskLen / 2;
                        sLen = curTaskLen - mLen;
                    } else {
                        if (mBw == 0) {
                            mLen = minBaseLen;
                            sLen = curTaskLen - minBaseLen;
                        } else {
                            mLen = curTaskLen - minBaseLen;
                            sLen = minBaseLen;
                        }
                    }
                }

                /*schedule new tasks for master and slave*/
                if (isMaster) {
                    semaphore.acquire();
                    retTask = leftTask.schedule(mLen);
                    semaphore.release();
                } else {
                    semaphore.acquire();
                    retTask = leftTask.schedule(sLen);
                    semaphore.release();
                }

                return retTask;
            }
        } else {
            /* All tasks have been done */
            Exception e = new Exception("All tasks have been done");
            throw e;
        }
    }

    public DownloadTask[] scheduleSingleTask(int baseLen) throws Exception {
//        semaphore.acquire();
        if (!leftTask.isDone) {
            DownloadTask[] retTask = new DownloadTask[2];
            long leftLen = leftTask.end - leftTask.start + 1;

            if (leftLen <= baseLen) {
                /* last scheduling, only a small part of task left */
                if (mBw >= sBw) {
                    retTask[0] = leftTask.schedule(leftLen);
                    retTask[1] = null;
                } else {
                    retTask[0] = null;
                    retTask[1] = leftTask.schedule(leftLen);
                }

//                semaphore.release();
                return retTask;
            } else {
                /* general scheduling */
                long mLen;
                long sLen;
                long curTaskLen;

                if (leftLen <= 2 * baseLen && leftLen > baseLen) {
                    /* last scheduling */
                    curTaskLen = leftLen;
                } else {
                    /* general scheduling */
                    curTaskLen = 2 * baseLen;
                }

                /* calculate tasks according to bandwidth */
                if (mBw != 0 && sBw != 0) {
                    mLen = curTaskLen * mBw / (mBw + sBw);
                    sLen = curTaskLen - mLen;
                } else {
                    if (mBw == 0 && sBw == 0) {
                        mLen = curTaskLen / 2;
                        sLen = curTaskLen - mLen;
                    } else {
                        if (mBw == 0) {
                            mLen = 0;
                            sLen = curTaskLen;
                        } else {
                            mLen = curTaskLen;
                            sLen = 0;
                        }
                    }
                }

                /*schedule new tasks for master and slave*/
                retTask[0] = leftTask.schedule(mLen);
                retTask[1] = leftTask.schedule(sLen);

//                semaphore.release();
                return retTask;
            }
        } else {
//            semaphore.release();
            Exception e = new Exception("All tasks have been done");
            throw e;
        }
    }

    public void submitTask(long m, long s) {
        mBw = m;
        sBw = s;
//        semaphore.release();
        return;
    }

    public void updateMasterBw(long m) throws Exception {
        semaphore.acquire();
        mBw = m;
        semaphore.release();
        return;
    }

    public void updateSlaveBw(long s) throws Exception {
        semaphore.acquire();
        sBw = s;
        semaphore.release();
        return;
    }
}
