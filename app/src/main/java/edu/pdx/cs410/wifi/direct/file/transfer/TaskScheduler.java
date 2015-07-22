package edu.pdx.cs410.wifi.direct.file.transfer;

import java.util.ArrayList;

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

    public TaskScheduler(int tl, String l) {
        totalLen = tl;
//        leftTaskList.add(new DownloadTask(0, totalLen - 1, true, url));
        leftTask = new DownloadTask(0, totalLen - 1, totalLen, l);
        url = l;
    }

    public boolean isTaskDone() {
        return leftTask.isDone;
    }

    public DownloadTask[] scheduleTask(int baseLen) throws Exception {
        if (!leftTask.isDone) {
            DownloadTask[] retTask = new DownloadTask[2];
            int leftLen = leftTask.end - leftTask.start + 1;

            if (leftLen <= baseLen) {
                /* last scheduling, only a small part of task left */
                if (mBw >= sBw) {
//                    retTask[0] = new DownloadTask(leftTask.start, leftTask.end, totalLen, url);
                    retTask[0] = leftTask.schedule(leftLen);
                    retTask[1] = null;
                } else {
                    retTask[0] = null;
                    retTask[1] = leftTask.schedule(leftLen);
//                    retTask[1] = new DownloadTask(leftTask.start, leftTask.end, totalLen, url);
                }

//                leftTask.schedule(leftLen);
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
//                curStart = leftTask.start;
//                curEnd = leftTask.start + mLen - 1;
//                retTask[0] = new DownloadTask(curStart, curEnd, true, leftTask.url);
                retTask[0] = leftTask.schedule(mLen);
//                curStart = leftTask.start + mLen;
//                curEnd = leftTask.start + mLen + sLen - 1;
//                retTask[1] = new DownloadTask(curStart, curEnd, true, leftTask.url);
                retTask[1] = leftTask.schedule(sLen);
                //            curStart = leftTask.start;
                //            curEnd = (leftTask.start + mLen - 1 > leftTask.end) ? leftTask.start + mLen - 1 : leftTask.end;
                //            retTask[0] = new DownloadTask(curStart, curEnd, true, leftTask.url);
                //            curStart = leftTask.start + mLen;
                //            curEnd = (leftTask.start + mLen + sLen - 1 > leftTask.end) ? leftTask.start + mLen - 1 : leftTask.end;
                //            retTask[1] = new DownloadTask(leftTask.start + mLen, leftTask.start + mLen + sLen - 1, false, leftTask.url);

                /*re-calculate the left task*/
//                leftTask.start += curTaskLen;
//                leftTask.isPartial = true;

                /*all tasks have been done*/
//                if (leftTask.start == leftTask.end) {

                return retTask;
            }
        } else {
            Exception e = new Exception("All tasks have been done");
            throw e;
        }
    }

    public void submitTask(int m, int s) {
        mBw = m;
        sBw = s;

        return;
    }
}
