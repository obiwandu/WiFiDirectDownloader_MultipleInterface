package edu.pdx.cs410.wifi.direct.file.transfer.oldClass;

import android.os.Handler;

/**
 * Created by User on 7/3/2015.
 */
public class SlaveSendThread extends Thread {
    private int port;
    private String ip;
    private Handler handler;

    public SlaveSendThread(int masterPort, String masterIp, Handler hdl){
        port = masterPort;
        ip = masterIp;
        handler = hdl;
    }

    @Override
    public void run(){

    }
}
