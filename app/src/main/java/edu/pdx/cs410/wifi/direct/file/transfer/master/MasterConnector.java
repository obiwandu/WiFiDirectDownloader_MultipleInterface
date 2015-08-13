package edu.pdx.cs410.wifi.direct.file.transfer.master;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.BackendService;
import edu.pdx.cs410.wifi.direct.file.transfer.ProtocolHeader;
import edu.pdx.cs410.wifi.direct.file.transfer.Statistic;
import edu.pdx.cs410.wifi.direct.file.transfer.ThreadStatistics;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpConnector;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpConnectorLong;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpTrans;

/**
 * Created by User on 7/11/2015.
 */
public class MasterConnector {
//    static public long remoteDownload (String command, File recvFile,
//                                         InetSocketAddress remoteAddr, InetSocketAddress localAddr,
//                                      BackendService masterService) throws Exception {
//        long bw;
//        byte[] sendBuf = command.getBytes();
//        TcpTrans.send(remoteAddr, localAddr, sendBuf);
//        masterService.signalActivity("Command is sent successfully, start waiting for data sent back");
//        bw = TcpTrans.recv(localAddr, recvFile, masterService);
//        masterService.signalActivity("Final data got successfully, task complete");
//
//        return bw;
//    }

    /* Short connection */
    static public long remoteDownload (ProtocolHeader header, String url, RandomAccessFile recvFile,
                                      InetSocketAddress remoteAddr, InetSocketAddress localAddr,
                                      BackendService masterService) throws Exception {
        long bw;
        TcpConnector conn = new TcpConnector(remoteAddr, localAddr, masterService, 0);
        conn.send(header.header, ProtocolHeader.HEADER_LEN);
        byte[] sendBuf = url.getBytes();
        conn.send(sendBuf, url.length());
        conn.close();
        masterService.signalActivity("Task command is sent successfully, start waiting for data sent back");

        conn = new TcpConnector(remoteAddr, localAddr, masterService, 1);
        byte[] recvBuf = new byte[ProtocolHeader.HEADER_LEN];
        conn.recv(recvBuf, ProtocolHeader.HEADER_LEN);
        ProtocolHeader recvHeader = new ProtocolHeader(recvBuf);
        masterService.signalActivity("Data header received, start receiving data from slave");
        conn.recv(recvFile, (int)(recvHeader.end - recvHeader.start + 1));
        bw = recvHeader.bw;
        conn.close();
        masterService.signalActivity("Data got successfully");

        return bw;
    }

    /* Long connection version */
    static public long remoteDownload (ProtocolHeader header, String url, RandomAccessFile recvFile,
                                      TcpConnector conn, ThreadStatistics stat) throws Exception {
        byte[] sendBuf = url.getBytes();
        byte[] recvBuf = new byte[ProtocolHeader.HEADER_LEN];

        /* Send task to slave */
        conn.send(header.header, ProtocolHeader.HEADER_LEN);
        conn.send(sendBuf, url.length());
        conn.backendService.signalActivity("Task command is sent successfully, start waiting for data sent back");

        /* Wait for data back */
        conn.recv(recvBuf, ProtocolHeader.HEADER_LEN);
        ProtocolHeader recvHeader = new ProtocolHeader(recvBuf);
        conn.backendService.signalActivity("Data header received, start receiving data from slave");
        conn.recv(recvFile, (int)(recvHeader.end - recvHeader.start + 1), stat);
        long bw = recvHeader.bw;
        conn.backendService.signalActivity("Data got successfully");

        return bw;
    }

    /* Short connection */
    static public void remoteStop (InetSocketAddress remoteAddr, InetSocketAddress localAddr,
                                      ProtocolHeader header, BackendService masterService) throws Exception {
        TcpConnector conn = new TcpConnector(remoteAddr, localAddr, masterService, 0);
        conn.send(header.header, ProtocolHeader.HEADER_LEN);
        conn.close();
        masterService.signalActivity("Stop command is sent successfully, slave will be shut down");
    }

    /* Long connection */
    static public void remoteStop (ProtocolHeader header, TcpConnector conn) throws Exception {
        conn.send(header.header, ProtocolHeader.HEADER_LEN);
    }
}
