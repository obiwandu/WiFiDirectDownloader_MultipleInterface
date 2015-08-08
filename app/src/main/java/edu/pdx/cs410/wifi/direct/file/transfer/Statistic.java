package edu.pdx.cs410.wifi.direct.file.transfer;

/**
 * Created by User on 8/7/2015.
 */
public class Statistic {
    public long alreadyBytes = 0;
    public Log statLog;
    public Statistic(String logName) {
        statLog = new Log(logName);

    }

    public void stat(long transBytes) throws Exception{
        alreadyBytes += transBytes;
        statLog.stat(Long.toString(alreadyBytes));
        return;
    }
}
