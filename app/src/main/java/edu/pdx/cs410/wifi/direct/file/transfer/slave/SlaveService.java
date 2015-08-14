package edu.pdx.cs410.wifi.direct.file.transfer.slave;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.BackendService;
import edu.pdx.cs410.wifi.direct.file.transfer.Statistic;
import edu.pdx.cs410.wifi.direct.file.transfer.ThreadStatistics;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpConnectorLong;

/**
 * Created by User on 7/9/2015.
 */
public class SlaveService extends BackendService {
//    private int nrsPort;
//    private ResultReceiver slaveResult;
//    private InetAddress masterIp;
//    private InetAddress slaveIp;
//    private TcpConnectorLong conn;

    public SlaveService() {
        super();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        nrsPort = ((Integer) intent.getExtras().get("port")).intValue();
        masterIp = (InetAddress) intent.getExtras().get("masterIp");
        slaveIp = (InetAddress) intent.getExtras().get("slaveIp");
        resultReceiver = (ResultReceiver) intent.getExtras().get("slaveResult");
        stat = new Statistic(this);

        try {
            InetSocketAddress localSockAddr = new InetSocketAddress(slaveIp, nrsPort);
            InetSocketAddress remoteSockAddr = new InetSocketAddress(masterIp, nrsPort);
//            conn = new TcpConnectorLong(remoteSockAddr, localSockAddr, this, 1);
            SlaveAcceptor.listen(remoteSockAddr, localSockAddr, this, stat);
        } catch (Exception e) {
            signalActivityException("Exception in downloading:" + e.toString());
        }
    }

//    public void signalActivity(String msg) {
//        Bundle b = new Bundle();
//        b.putString("message", msg);
//        slaveResult.send(nrsPort, b);
//    }
}
