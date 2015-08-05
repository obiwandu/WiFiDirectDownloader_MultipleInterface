package edu.pdx.cs410.wifi.direct.file.transfer.slave;

import android.os.Bundle;
import android.os.ResultReceiver;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.BackendService;
import edu.pdx.cs410.wifi.direct.file.transfer.ProtocolHeader;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpConnector;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpConnectorLong;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpTrans;

/**
 * Created by User on 7/12/2015.
 */
public class SlaveAcceptor {
    static public void listen(InetSocketAddress remoteAddr, InetSocketAddress localAddr, BackendService slaveService) throws Exception {
        byte[] recvHeader = new byte[ProtocolHeader.HEADER_LEN];
        byte[] urlBuf = new byte[1024];
//        ProtocolHeader header = new ProtocolHeader();

        TcpConnector conn = new TcpConnector(remoteAddr, localAddr, slaveService, 1);
        while (true) {
            slaveService.signalActivity("Start listening for command");
//            InetSocketAddress[] sockAddr = TcpTrans.recv(localAddr, recvBuf);

            String url;

            conn.recv(recvHeader, ProtocolHeader.HEADER_LEN);
//            header.decapPro(recvHeader);
            ProtocolHeader header = new ProtocolHeader(recvHeader);
            if (header.stop == true) {
                conn.close();
                slaveService.signalActivityComplete();
                slaveService.signalActivity("All tasks are complete, slave stops");
                break;
            }
            conn.recv(urlBuf, (int)header.urlLen);
            url = new String(urlBuf);
            slaveService.signalActivity("Command got, start downloading");
//            SlaveProcessor.processRequest(recvBuf, sockAddr, slaveService);
            SlaveProcessor.processRequest(header, url, conn);
            slaveService.signalActivity("Data sent back succefully");
        }
    }
}
