package edu.pdx.cs410.wifi.direct.file.transfer.slave;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by User on 7/9/2015.
 */
public class SlaveService extends IntentService {
    private int nrsPort;
    private ResultReceiver slaveResult;
    private InetAddress masterIp;
    private InetAddress slaveIp;

    public SlaveService() {
        super("SlaveService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        nrsPort = ((Integer) intent.getExtras().get("port")).intValue();
        masterIp = (InetAddress) intent.getExtras().get("masterIp");
        slaveIp = (InetAddress) intent.getExtras().get("slaveIp");
        slaveResult = (ResultReceiver) intent.getExtras().get("slaveResult");

        try {
            InetSocketAddress localSockAddr = new InetSocketAddress(slaveIp, nrsPort);
            SlaveAcceptor.listen(localSockAddr, this);
        } catch (Exception e) {
            signalActivity("Failure in downloading:" + e.toString());
        }
    }

    public void signalActivity(String msg) {
        Bundle b = new Bundle();
        b.putString("message", msg);
        slaveResult.send(nrsPort, b);
    }
}
