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
    public boolean isHarmonic;
    Timer timer;
    TimerTask timerTask;
    public HashMap<Long, ThreadStatistics> statMap;
    long curClock;
    BackendService service;
    int threadNum;

    public Statistic(BackendService s) {
        service = s;
        statMap = new HashMap<Long, ThreadStatistics>();
        curClock = 0;
        isHarmonic = false;
        threadNum = 0;
    }

    public void addThread(String threadName, boolean isLan) {
        threadNum ++;
        long threadId = Thread.currentThread().getId();
        statMap.put(threadId, new ThreadStatistics(threadName, isLan));

    }

    public ThreadStatistics getThreadStat(long threadId) {
        return statMap.get(threadId);
    }

    public long[] toBwArray() {
        long[] bwArray = new long[threadNum];
        int index = 0;
        Iterator it = statMap.keySet().iterator();
        while (it.hasNext()) {
            bwArray[index] = statMap.get(it.next()).bw;
            index++;
        }
        return bwArray;
    }

    public void startTimer() throws Exception {
        timer = new Timer();
        initTimer();
        timer.schedule(timerTask, 0, 1000);
    }

    public void stopTimer() throws Exception {
        if (timer != null) {
            timerTask.run();
            timer.cancel();
        }
    }

    public void initTimer() throws Exception {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    Iterator it = statMap.keySet().iterator();
                    while(it.hasNext()) {
                        ThreadStatistics curStat = statMap.get(it.next());
                        curStat.updateBw(curClock, isHarmonic);
                        /* Record bytes transfered */
                        curStat.stat();
                        service.signalActivityStat();
                    }
                    curClock ++;
                } catch (Exception e) {
                    /* Exception in logging */
                }
            }
        };
    }
}
