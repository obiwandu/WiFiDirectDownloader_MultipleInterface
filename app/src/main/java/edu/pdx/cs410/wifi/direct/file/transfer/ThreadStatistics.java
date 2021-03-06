package edu.pdx.cs410.wifi.direct.file.transfer;

/**
 * Created by User on 8/13/2015.
 */
public class ThreadStatistics {
    public Log statLog;
    public String threadName;
    public long curTotalBytes;
    public long curAlreadyBytes;
    //        public long curAlreadyTime;
    public long alreadyBytes;
    public long lastBytes;
    public long bw;
    public long slaveBw;
    public long avgBw;
    boolean isLan;
    //        public long lastTime;
//        public long alreadyTime;
    public ThreadStatistics(String name, boolean isL) {
        threadName = name;
        statLog = new Log("stat_" + threadName);
        alreadyBytes = 0;
//            alreadyTime = 0;
//            lastTime = 0;
        bw = 0;
        isLan = isL;
    }

    public void updateBw(long clock, boolean isHarmonic) {
        long curBw;
        if (!isLan) {
            avgBw = (long) ((float) alreadyBytes / (float) ((clock + 1) * 1024));
            curBw = (long) ((float) (alreadyBytes - lastBytes) / (float) 1024);
            lastBytes = alreadyBytes;
        } else {
            avgBw = (long) ((float) alreadyBytes / (float) ((clock + 1) * 1024));
            curBw = slaveBw;
        }

        calBw(curBw, clock, isHarmonic);
    }

    private void calBw(long curBw, long clock, boolean isHarmonic) {
//        avgBw = (long)((float)alreadyBytes/(float)((clock + 1)*1024));
//        long curBw = (long)((float)(alreadyBytes - lastBytes)/(float)1024);
//        lastBytes = alreadyBytes;

        if (!isHarmonic) {
            /*EWMA*/
            bw = (long)(0.9 * (float)bw + 0.1 * (float)curBw);
        } else {
            /*Harmonic*/
            if (curBw != 0) {
                if (bw != 0) {
                    bw = (long) ((float) (clock + 1) / (((float) clock / (float) bw) + ((float) 1 / (float) curBw)));
                } else {
                    bw = (long) ((float) (clock + 1) / ((float) 1 / (float) curBw));
                }
            } else {
                if (bw != 0) {
                    bw = (long) ((float) (clock + 1) / ((float) clock / (float) bw));
                } else {
                    bw = 0;
                }
            }
        }
    }

    public void stat() throws Exception {
        statLog.stat(Long.toString(alreadyBytes));
    }

    public void startMetric(long totalLen) throws Exception {
//        curStat.lastTime = System.currentTimeMillis();
        curTotalBytes = totalLen;
        curAlreadyBytes = 0;
//        curStat.curAlreadyTime = 0;
    }

    public void updateSlaveBw(long curBw) throws Exception {
        slaveBw = curBw;
    }

    public void updateBytes(long transBytes) throws Exception {
//        long currentTime = System.currentTimeMillis();
//        long timeSpan = currentTime - curStat.lastTime;
//        curStat.lastTime = currentTime;
//        long tempAlreadyTime = curStat.alreadyTime;
//        curStat.curAlreadyTime += timeSpan;
//        curStat.alreadyTime += timeSpan;

        alreadyBytes += transBytes;
        curAlreadyBytes += transBytes;

//        long curBw;
//        if (curStat.curAlreadyTime > 0) {
//            curBw = (long) ((float) 1000 * ((float) curStat.curAlreadyBytes / (float) (curStat.curAlreadyTime * 1024)));
//        } else {
//            curBw = 0;
//        }

//        if (!isHarmonic) {
//            /*EMBA*/
//            curStat.bw = (long)(0.9 * (float)curStat.bw + 0.1 * (float)curBw);
//        } else {
//            /*Harmonic*/
//            if (curBw != 0) {
//                if (curStat.bw != 0) {
//                    curStat.bw = (long) ((float) curStat.alreadyTime / (((float) tempAlreadyTime / (float) curStat.bw) + ((float) timeSpan / (float) curBw)));
//                } else {
//                    curStat.bw = (long) ((float) curStat.alreadyTime / ((float) timeSpan / (float) curBw));
//                }
//            } else {
//                if (curStat.bw != 0) {
//                    curStat.bw = (long) ((float) curStat.alreadyTime / ((float) tempAlreadyTime / (float) curStat.bw));
//                } else {
//                    curStat.bw = 0;
//                }
//            }
//        }

//        float dataPer = (float)(100*curAlreadyBytes)/(float)curTotalBytes;

//        if (service != null) {
//            if (isLAN) {
//                service.signalActivity("LAN transmission is on going: Percentage:" + dataPer + "% | Bw:" + Long.toString(bw) + " KB/s");
//            } else {
//                service.signalActivity("HTTP transmission is on going: Percentage:" + dataPer + "% | Bw:" + Long.toString(bw) + " KB/s");
//            }
//        }
    }
};
