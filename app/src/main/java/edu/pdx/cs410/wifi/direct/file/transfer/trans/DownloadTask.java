package edu.pdx.cs410.wifi.direct.file.transfer.trans;

/**
 * Created by User on 7/5/2015.
 */
public class DownloadTask {
    public int start;
    public int end;
    public boolean isPartial;
    public String url;

    public DownloadTask(int s, int e, boolean p, String l){
        start = s;
        end = e;
        isPartial = p;
        url = l;
    }
}
