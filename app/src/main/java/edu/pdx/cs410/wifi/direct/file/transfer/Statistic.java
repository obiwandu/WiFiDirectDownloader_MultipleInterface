package edu.pdx.cs410.wifi.direct.file.transfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by User on 8/7/2015.
 */
public class Statistic {
//    public long alreadyBytes = 0;
    public Log statLog;
    Timer timer;
    TimerTask timerTask;
    HashMap<Long, Long> alreadyBytesMap;
    HashMap<Long, Log> statLogMap;

//    public Statistic(String logName) {
//        statLog = new Log(logName);
//    }

    public Statistic() {
        statLogMap = new HashMap<Long, Log>();
        alreadyBytesMap = new HashMap<Long, Long>();
    }

    public void addThread(String threadName) {
        long threadId = Thread.currentThread().getId();
        statLogMap.put(threadId, new Log(threadName));
        alreadyBytesMap.put(threadId, (long)0);
    }

//    public void stat(long transBytes) throws Exception{
//        alreadyBytes += transBytes;
//        statLog.stat(Long.toString(alreadyBytes));
//        return;
//    }

    public void update(long threadId, long transBytes) throws Exception {
//        long alreadyBytes = alreadyBytesList.get(statIndex) + transBytes;
//        alreadyBytesList.set(statIndex, alreadyBytes);
        long alreadyBytes = alreadyBytesMap.get(threadId);
        alreadyBytes += transBytes;
        alreadyBytesMap.put(threadId, alreadyBytes);
    }

    public void startTimer() throws Exception {
        timer = new Timer();
        initTimer();
        timer.schedule(timerTask, 0, 1000);
    }

    public void stopTimer() throws Exception {
        if (timer != null) {
            timer.cancel();
        }
    }

    public void initTimer() throws Exception {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
//                    statLog.stat(Long.toString(alreadyBytes));
                    Iterator it = statLogMap.keySet().iterator();
                    while(it.hasNext()) {
                        long threadId = (Long)it.next();
                        Log temp = statLogMap.get(threadId);
                        Long alreadyBytes = alreadyBytesMap.get(threadId);
                        temp.stat(alreadyBytes.toString());
                    }
                } catch (Exception e) {
                    /* Exception in logging */
                }
            }
        };
    }
}
