package edu.pdx.cs410.wifi.direct.file.transfer;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;

/**
 * Created by User on 7/21/2015.
 */
public class TaskScheduler {
    private int totalLen;
    //    private ArrayList<DownloadTask> leftTaskList;
    public DownloadTask leftTask;
    private DownloadTask[] taskList;
    private int mBw;
    private int sBw;
    private String url;
    static Semaphore semaphore;

    public TaskScheduler(int tl, String l) {
        semaphore = new Semaphore(1);
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

    public DownloadTask scheduleTask(int baseLen, boolean isMaster) throws Exception {
        semaphore.acquire();
        if (!leftTask.isDone) {
            DownloadTask retTask;
            int leftLen = leftTask.end - leftTask.start + 1;

            if (leftLen <= baseLen) {
                /* last scheduling, only a small part of task left */
                if (mBw >= sBw) {
//                    retTask[0] = new DownloadTask(leftTask.start, leftTask.end, totalLen, url);
                    if (isMaster) {
                        retTask = leftTask.schedule(leftLen);
                    } else {
                        retTask = null;
                    }
                } else {
                    if (isMaster) {
                        retTask = null;
                    } else {
                        retTask = leftTask.schedule(leftLen);
                    }
//                    retTask[1] = new DownloadTask(leftTask.start, leftTask.end, totalLen, url);
                }

//                leftTask.schedule(leftLen);
                semaphore.release();
                return retTask;
            } else {
                /* general scheduling */
                int mLen;
                int sLen;
//                int curStart;
//                int curEnd;
                int curTaskLen;

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
                if (isMaster) {
                    retTask = leftTask.schedule(mLen);
                } else {
                    retTask = leftTask.schedule(sLen);
                }
//                retTask[0] = leftTask.schedule(mLen);
//                retTask[1] = leftTask.schedule(sLen);

                /*all tasks have been done*/
                semaphore.release();
                return retTask;
            }
        } else {
            semaphore.release();
            Exception e = new Exception("All tasks have been done");
            throw e;
        }
    }

    public DownloadTask[] scheduleSingleTask(int baseLen) throws Exception {
        semaphore.acquire();
        if (!leftTask.isDone) {
            DownloadTask[] retTask = new DownloadTask[2];
            int leftLen = leftTask.end - leftTask.start + 1;

            if (leftLen <= baseLen) {
                /* last scheduling, only a small part of task left */
                if (mBw >= sBw) {
                    retTask[0] = leftTask.schedule(leftLen);
                    retTask[1] = null;
                } else {
                    retTask[0] = null;
                    retTask[1] = leftTask.schedule(leftLen);
                }

                semaphore.release();
                return retTask;
            } else {
                /* general scheduling */
                int mLen;
                int sLen;
                int curTaskLen;

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

                semaphore.release();
                return retTask;
            }
        } else {
            semaphore.release();
            Exception e = new Exception("All tasks have been done");
            throw e;
        }
    }

    public void submitTask(int m, int s) {
        mBw = m;
        sBw = s;

        return;
    }

    public void updateMasterBw(int m) throws Exception {
        semaphore.acquire();
        mBw = m;
        semaphore.release();
        return;
    }

    public void updateSlaveBw(int s) throws Exception {
        semaphore.acquire();
        sBw = s;
        semaphore.release();
        return;
    }
}
