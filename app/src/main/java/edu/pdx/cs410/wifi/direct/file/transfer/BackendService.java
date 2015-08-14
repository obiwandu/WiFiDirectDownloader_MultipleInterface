package edu.pdx.cs410.wifi.direct.file.transfer;

import android.app.IntentService;
import android.os.Bundle;
import android.os.ResultReceiver;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by User on 7/25/2015.
 */
public abstract class BackendService extends IntentService {
    protected ResultReceiver resultReceiver;
    protected int nrsPort;
    protected InetAddress masterIp;
    protected InetAddress slaveIp;
    protected long totalLen;
    protected Statistic stat;

    public BackendService() {
        super("BackendService");
    }

    public void signalActivity(String msg) {
        Bundle b = new Bundle();
        b.putString("message", msg);
        resultReceiver.send(1, b);
    }

    public void signalActivityException(String exception) {
        Bundle b = new Bundle();
        b.putString("exception", exception);
        resultReceiver.send(4, b);
    }

    public void signalActivityComplete() {
        resultReceiver.send(0, null);
    }

    public void signalActivityProgress(String msg) {
        Bundle b = new Bundle();
        b.putString("progress", msg);
        resultReceiver.send(2, b);
    }

    public void signalActivityStat() {
        HashMap<Long, ThreadStatistics> statMap = stat.statMap;
        int thdNum = statMap.size();
        String[] threadNameArray = new String[thdNum];
        float[] progressArray = new float[thdNum];
        long[] bwArray = new long[thdNum];
        long[] avgBwArray = new long[thdNum];
        long[] alBytesArray = new long[thdNum];
        Bundle b = new Bundle();
        long totalAlreadyBytes = 0;

        Iterator it = statMap.keySet().iterator();
        int index = 0;
        while (it.hasNext()) {
            ThreadStatistics curStat = statMap.get(it.next());
            /* Thread name */
            threadNameArray[index] = curStat.threadName;

            /* Progress */
            if (!curStat.isLan) {
                if (curStat.curTotalBytes == 0){
                    progressArray[index] = 0;
                }else {
                    progressArray[index] = (float) 100 * ((float) curStat.curAlreadyBytes / (float) curStat.curTotalBytes);
                }
            } else {
                progressArray[index] = 0;
            }

            /* Current BW */
            bwArray[index] = curStat.bw;

            /* Avg BW */
            avgBwArray[index] = curStat.avgBw;

            /* alreadyBytes */
            alBytesArray[index] = curStat.alreadyBytes;
            totalAlreadyBytes += curStat.alreadyBytes;

            index++;
        }

        b.putStringArray("nameStat", threadNameArray);
        b.putFloatArray("proStat", progressArray);
        b.putLongArray("bwStat", bwArray);
        b.putLongArray("avgBwStat", avgBwArray);
        b.putLongArray("alBytesStat", alBytesArray);

        b.putString("totalTime", Long.toString(stat.curClock));
        b.putString("totalPro", Float.toString((float) 100 * ((float) totalAlreadyBytes / (float) totalLen)));
        b.putString("totalAvgBw", Long.toString((long) (((float) totalAlreadyBytes / (float) 1024) / (float) stat.curClock)));
        resultReceiver.send(3, b);
    }
}
