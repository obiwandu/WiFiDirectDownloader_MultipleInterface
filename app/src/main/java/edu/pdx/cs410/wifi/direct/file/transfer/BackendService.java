package edu.pdx.cs410.wifi.direct.file.transfer;

import android.app.IntentService;
import android.os.Bundle;
import android.os.ResultReceiver;

import java.net.InetAddress;

/**
 * Created by User on 7/25/2015.
 */
public abstract class BackendService extends IntentService {
    protected ResultReceiver resultReceiver;
    protected int nrsPort;
    protected InetAddress masterIp;
    protected InetAddress slaveIp;

    public BackendService() {
        super("BackendService");
    }

    public void signalActivity(String msg) {
        Bundle b = new Bundle();
        b.putString("message", msg);
        resultReceiver.send(1, b);
    }

    public void signalActivityComplete() {
        resultReceiver.send(0, null);
    }

    public void signalActivityProgress(String msg) {
        Bundle b = new Bundle();
        b.putString("progress", msg);
        resultReceiver.send(2, b);
    }
}
