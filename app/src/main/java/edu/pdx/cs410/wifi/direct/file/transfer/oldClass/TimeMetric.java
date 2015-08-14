package edu.pdx.cs410.wifi.direct.file.transfer.oldClass;


/**
 * Created by User on 7/27/2015.
 */
public class TimeMetric {
    public long alreadyTime;
    private long startTime;
    public TimeMetric() {
        alreadyTime = 0;
        startTime = 0;
    }

    public void startTimer() {
        startTime = System.currentTimeMillis();
    }

    public long getTimeLapse() {
        alreadyTime = System.currentTimeMillis() - startTime;
        return alreadyTime;
    }
}
