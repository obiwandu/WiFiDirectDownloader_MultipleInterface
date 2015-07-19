package edu.pdx.cs410.wifi.direct.file.transfer.slave;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpTrans;

/**
 * Created by User on 7/12/2015.
 */
public class SlaveAcceptor {
    static public void listen(InetSocketAddress localAddr){
        byte[] recvBuf = new byte[1024];
        InetSocketAddress[] sockAddr = TcpTrans.recv(localAddr, recvBuf);

        SlaveProcessor.processRequest(recvBuf, sockAddr);
    }
}
