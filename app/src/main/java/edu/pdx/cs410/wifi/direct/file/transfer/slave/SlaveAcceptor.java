package edu.pdx.cs410.wifi.direct.file.transfer.slave;

import android.os.Bundle;
import android.os.ResultReceiver;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpTrans;

/**
 * Created by User on 7/12/2015.
 */
public class SlaveAcceptor {
    static public void listen(InetSocketAddress localAddr, SlaveService slaveService) throws Exception {
        byte[] recvBuf = new byte[1024];
        slaveService.signalActivity("Start listening for command");
        InetSocketAddress[] sockAddr = TcpTrans.recv(localAddr, recvBuf);
        slaveService.signalActivity("Command got, start downloading");
        SlaveProcessor.processRequest(recvBuf, sockAddr, slaveService);
    }

}
