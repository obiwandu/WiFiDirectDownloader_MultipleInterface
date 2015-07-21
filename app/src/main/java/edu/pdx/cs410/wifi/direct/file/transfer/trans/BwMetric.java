package edu.pdx.cs410.wifi.direct.file.transfer.trans;

import edu.pdx.cs410.wifi.direct.file.transfer.master.MasterService;
import edu.pdx.cs410.wifi.direct.file.transfer.slave.SlaveService;

/**
 * Created by User on 7/21/2015.
 */
public class BwMetric {
    /* used for bw calculation */
    private MasterService masterService;
    private SlaveService slaveService;
    private int alreadyBytes = 0;
    private int alreadyTime = 0;
    private long lastTime = System.currentTimeMillis();
    private long currenntTime;
    private int timeSpan;
    public int bw = 0;

    public BwMetric (MasterService service) {
        /* used for bw calculation */
        alreadyBytes = 0;
        alreadyTime = 0;
        lastTime = System.currentTimeMillis();
        currenntTime = 0;
        timeSpan = 0;
        bw = 0;
        masterService = service;
        slaveService = null;
    }

    public BwMetric (SlaveService service) {
        /* used for bw calculation */
        alreadyBytes = 0;
        alreadyTime = 0;
        lastTime = System.currentTimeMillis();
        currenntTime = 0;
        timeSpan = 0;
        bw = 0;
        masterService = null;
        slaveService = service;
    }

    public int bwMetric(int bytesRead) {
        /* update bw */
        alreadyBytes += bytesRead;
        currenntTime = System.currentTimeMillis();
        timeSpan = (int) (currenntTime - lastTime);
        lastTime = currenntTime;
        alreadyTime += timeSpan;
        if (alreadyTime > 0) {
            bw = alreadyBytes / alreadyTime;
        } else {
            bw = 0;
        }

        if (masterService != null) {
            masterService.signalActivity("Transmission is on going: " + Integer.toString(bw) + " B/s");
        }
        else if (slaveService != null) {
            slaveService.signalActivity("Transmission is on going: " + Integer.toString(bw) + " B/s");
        }

        return bw;
    }
}
