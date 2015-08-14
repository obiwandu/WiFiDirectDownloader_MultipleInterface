package edu.pdx.cs410.wifi.direct.file.transfer.oldClass;

import edu.pdx.cs410.wifi.direct.file.transfer.BackendService;
import edu.pdx.cs410.wifi.direct.file.transfer.master.MasterService;
import edu.pdx.cs410.wifi.direct.file.transfer.slave.SlaveService;

/**
 * Created by User on 7/21/2015.
 */
public class BwMetric {
    /* used for bw calculation */
    private BackendService masterService;
    private BackendService slaveService;
    private BackendService backendService;
    private int alreadyBytes = 0;
    private int alreadyTime = 0;
    private long totalLen = 0;
    private long lastTime = System.currentTimeMillis();
    private long currenntTime;
    private int timeSpan;
    public long bw = 0;

    public BwMetric (BackendService service, long tl) {
        /* used for bw calculation */
        totalLen = tl;
        alreadyBytes = 0;
        alreadyTime = 0;
        lastTime = System.currentTimeMillis();
        currenntTime = 0;
        timeSpan = 0;
        bw = 0;
        backendService = service;
//        masterService = service;
//        slaveService = null;
    }

//    public BwMetric (BackendService service) {
//        /* used for bw calculation */
//        alreadyBytes = 0;
//        alreadyTime = 0;
//        lastTime = System.currentTimeMillis();
//        currenntTime = 0;
//        timeSpan = 0;
//        bw = 0;
//        masterService = null;
//        slaveService = service;
//    }

    public long bwMetric(int bytesRead, boolean isLAN) {
        /* update bw */
        alreadyBytes += bytesRead;
        currenntTime = System.currentTimeMillis();
        timeSpan = (int) (currenntTime - lastTime);
        lastTime = currenntTime;
        alreadyTime += timeSpan;
        if (alreadyTime > 0) {
            bw = (long)((float)1000 * ((float)alreadyBytes / (float)(alreadyTime * 1024)));
        } else {
            bw = 0;
        }

        float dataPer = (float)(100*alreadyBytes)/(float)totalLen;

//        if (backendService != null) {
//            if (isLAN) {
//                backendService.signalActivity("LAN transmission is on going: Percentage:" + dataPer + "% | Bw:" + Long.toString(bw) + " KB/s");
//            } else {
//                backendService.signalActivity("HTTP transmission is on going: Percentage:" + dataPer + "% | Bw:" + Long.toString(bw) + " KB/s");
//            }
//        }
//        else if (slaveService != null) {
//            slaveService.signalActivity("Transmission is on going: " + Integer.toString(bw) + " B/s");
//        }

        return bw;
    }
}
