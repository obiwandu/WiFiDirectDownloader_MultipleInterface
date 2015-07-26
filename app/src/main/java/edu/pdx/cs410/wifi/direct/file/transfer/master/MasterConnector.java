package edu.pdx.cs410.wifi.direct.file.transfer.master;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.BackendService;
import edu.pdx.cs410.wifi.direct.file.transfer.ProtocolHeader;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpConnectorLong;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpTrans;

/**
 * Created by User on 7/11/2015.
 */
public class MasterConnector {
    static public int remoteDownload (String command, File recvFile,
                                         InetSocketAddress remoteAddr, InetSocketAddress localAddr,
                                      BackendService masterService) throws Exception {
        int bw;
        byte[] sendBuf = command.getBytes();
        TcpTrans.send(remoteAddr, localAddr, sendBuf);
        masterService.signalActivity("Command is sent successfully, start waiting for data sent back");
        bw = TcpTrans.recv(localAddr, recvFile, masterService);
        masterService.signalActivity("Final data got successfully, task complete");

        return bw;
    }

    static public int remoteDownload (TcpConnectorLong conn, ProtocolHeader header, String url, RandomAccessFile recvFile,
                                      InetSocketAddress remoteAddr, InetSocketAddress localAddr,
                                      BackendService masterService) throws Exception {
        int bw;
//        byte[] sendBuf = command.getBytes();
        conn.send(header.header);
        byte[] sendBuf = url.getBytes();
        conn.send(sendBuf);
//        TcpTrans.send(remoteAddr, localAddr, header);
        masterService.signalActivity("Task command is sent successfully, start waiting for data sent back");
        byte[] recvBuf = new byte[16];
//        TcpTrans.recv(localAddr, recvBuf, masterService);
        conn.recv(recvBuf);
        ProtocolHeader recvHeader = new ProtocolHeader();
        recvHeader.decapPro(recvBuf);
        masterService.signalActivity("Data header received, start receiving data from slave");
        bw = conn.recv(recvFile, recvHeader.dataLen);
//        bw = TcpTrans.recv(localAddr, recvFile, masterService);
        masterService.signalActivity("Data got successfully");

        return bw;
    }

    static public void remoteStop (ProtocolHeader header, TcpConnectorLong conn, BackendService masterService) throws Exception {
        conn.send(header.header);
        masterService.signalActivity("Stop command is sent successfully, slave will be shut down");
    }
}
