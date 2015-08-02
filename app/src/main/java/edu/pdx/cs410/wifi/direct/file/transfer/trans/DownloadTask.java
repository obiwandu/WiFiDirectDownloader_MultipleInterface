package edu.pdx.cs410.wifi.direct.file.transfer.trans;

/**
 * Created by User on 7/5/2015.
 */
public class DownloadTask {
    public long start;
    public long end;
    public long totalLen;
    public boolean isPartial;
    public boolean isDone;
    public String url;

    public DownloadTask(long s, long e, long tl, String l) {
        start = s;
        end = e;
        totalLen = tl;
        url = l;

        if (start == 0 && totalLen == end - start + 1) {
            isPartial = false;
        } else {
            isPartial = true;
        }

        if (start <= end) {
            isDone = false;
        } else {
            isDone = true;
        }
    }

    public DownloadTask schedule(long taskLen) {
        long actTaskLen;
        long leftLen = end - start + 1;
        long newStart, newEnd;

        if (taskLen == 0) {
            return null;
        } else {
//            if (start + taskLen <= end) {
            if (taskLen >= leftLen) {
                /* task len is greater than or equal to left len, actual task len is only left len */
                actTaskLen = leftLen;
            } else {
                /* task len is less than left len, actual task len is task len */
//                actTaskLen = end - start + 1;
                actTaskLen = taskLen;
            }

            newStart = start;
            newEnd = start + actTaskLen - 1;
            DownloadTask newTask = new DownloadTask(newStart, newEnd, totalLen, url);

            start = newEnd + 1;

            if (start > end) {
                isDone = true;
            }

            return newTask;
        }
    }
}
