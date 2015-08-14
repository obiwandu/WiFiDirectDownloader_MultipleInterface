package edu.pdx.cs410.wifi.direct.file.transfer.master;

import java.io.RandomAccessFile;
import java.net.InetSocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.BackendService;
import edu.pdx.cs410.wifi.direct.file.transfer.ProtocolHeader;
import edu.pdx.cs410.wifi.direct.file.transfer.ThreadStatistics;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpConnector;

/**
 * Created by User on 7/11/2015.
 */
public class MasterInvoker {
    static private String encapCmd(DownloadTask task) {
        String command = "taskstart:" + Long.toString(task.start) + "\n" + "taskend:" + Long.toString(task.end) + "\n"
                + "totallen:" + Long.toString(task.totalLen) + "\n" + "url:" + task.url + "\n";
        return command;
    }

    static private String encapCmd() {
        String command = "stop";

        return command;
    }

//    static public long remoteDownload(DownloadTask task, File recvFile,
//                                       InetSocketAddress remoteAddr, InetSocketAddress localAddr,
//                                     BackendService masterService) throws Exception {
//        long bw;
//        String command = encapCmd(task);
//        masterService.signalActivity("Ready to send command to slave");
//        bw = MasterConnector.remoteDownload(command, recvFile, remoteAddr, localAddr, masterService);
//        return bw;
//    }

    /* Short connection */
    static public long remoteDownload(DownloadTask task, RandomAccessFile recvFile,
                                     InetSocketAddress remoteAddr, InetSocketAddress localAddr,
                                     BackendService masterService) throws Exception {
        ProtocolHeader header = new ProtocolHeader(task);
        masterService.signalActivity("Ready to send command to slave");
        long bw = MasterConnector.remoteDownload(header, task.url, recvFile, remoteAddr, localAddr, masterService);
        return bw;
    }

    /* Long connection version */
    static public long remoteDownload(DownloadTask task, RandomAccessFile recvFile,
                                     TcpConnector conn, ThreadStatistics stat) throws Exception {
        ProtocolHeader header = new ProtocolHeader(task);
        conn.backendService.signalActivity("Ready to send command to slave");
        long bw = MasterConnector.remoteDownload(header, task.url, recvFile, conn, stat);
        return bw;
    }

    /* Short connection */
    static public void remoteStop(InetSocketAddress remoteAddr, InetSocketAddress localAddr, BackendService masterService) throws Exception{
        ProtocolHeader header = new ProtocolHeader(3333);
        masterService.signalActivity("Ready to send command to slave");
        MasterConnector.remoteStop(remoteAddr, localAddr, header, masterService);
    }

    /* Long connection */
    static public void remoteStop(TcpConnector conn) throws Exception{
        ProtocolHeader header = new ProtocolHeader(3333);
        conn.backendService.signalActivity("Ready to send command to slave");
        MasterConnector.remoteStop(header, conn);
        conn.backendService.signalActivity("Stop command is sent successfully, slave will be shut down");
    }
}
