package edu.pdx.cs410.wifi.direct.file.transfer.trans;

/**
 * Created by User on 7/5/2015.
 */
public class DownloadTask {
    public int start;
    public int end;
    public int totalLen;
    public boolean isPartial;
    public boolean isDone;
    public String url;

    public DownloadTask(int s, int e, int tl, String l) {
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

    public DownloadTask schedule(int taskLen) {
        int actTaskLen;
        int newStart, newEnd;

        if (taskLen == 0) {
            return null;
        } else {
            if (start + taskLen <= end) {
                actTaskLen = taskLen;
            } else {
                actTaskLen = end - start + 1;
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
