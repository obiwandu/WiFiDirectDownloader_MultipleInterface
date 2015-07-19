package edu.pdx.cs410.wifi.direct.file.transfer.oldClass;

import android.os.Handler;
import android.os.Message;

import java.io.File;

/**
 * Created by User on 7/3/2015.
 */
public class SlaveDlThread extends Thread {
    String url;
    Handler handler;
    File tempStorage;
    public SlaveDlThread(String cmd, Handler hdl){
        /*parse cmd and execute downloading*/
        url = "test";
        handler = hdl;
    }

    @Override
    public void run(){
        Message completeMsg = handler.obtainMessage(2, tempStorage);
        completeMsg.sendToTarget();
    }
}
