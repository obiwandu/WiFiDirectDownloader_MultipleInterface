package edu.pdx.cs410.wifi.direct.file.transfer.master;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.BackendService;
import edu.pdx.cs410.wifi.direct.file.transfer.ProtocolHeader;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpConnector;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpConnectorLong;

/**
 * Created by User on 7/11/2015.
 */
public class MasterInvoker {
    static private String encapCmd(DownloadTask task) {
        String command = "taskstart:" + Integer.toString(task.start) + "\n" + "taskend:" + Integer.toString(task.end) + "\n"
                + "totallen:" + task.totalLen + "\n" + "url:" + task.url + "\n";
        return command;
    }

    static private String encapCmd() {
        String command = "stop";

        return command;
    }

    static public int remoteDownload(DownloadTask task, File recvFile,
                                       InetSocketAddress remoteAddr, InetSocketAddress localAddr,
                                     BackendService masterService) throws Exception {
        int bw;
        String command = encapCmd(task);
        masterService.signalActivity("Ready to send command to slave");
        bw = MasterConnector.remoteDownload(command, recvFile, remoteAddr, localAddr, masterService);
        return bw;
    }

    static public int remoteDownload(DownloadTask task, RandomAccessFile recvFile,
                                     InetSocketAddress remoteAddr, InetSocketAddress localAddr,
                                     BackendService masterService) throws Exception {
        int bw;
//        String command = encapCmd(task);
        ProtocolHeader header = new ProtocolHeader();
        header.encapPro(task, 0);
        masterService.signalActivity("Ready to send command to slave");
        bw = MasterConnector.remoteDownload(header, task.url, recvFile, remoteAddr, localAddr, masterService);
        return bw;
    }

    static public int remoteDownload(DownloadTask task, RandomAccessFile recvFile,
                                     TcpConnector conn) throws Exception {
        int bw;
//        String command = encapCmd(task);
        ProtocolHeader header = new ProtocolHeader();
        header.encapPro(task, 0);
        conn.backendService.signalActivity("Ready to send command to slave");
        bw = MasterConnector.remoteDownload(header, task.url, recvFile, conn);
        return bw;
    }

    static public void remoteStop(InetSocketAddress remoteAddr, InetSocketAddress localAddr, BackendService masterService) throws Exception{
        ProtocolHeader header = new ProtocolHeader();
        header.encapPro(new DownloadTask(0, 0, 0, ""), 2222);
        masterService.signalActivity("Ready to send command to slave");
        MasterConnector.remoteStop(remoteAddr, localAddr, header, masterService);
    }

    static public void remoteStop(TcpConnector conn) throws Exception{
        ProtocolHeader header = new ProtocolHeader();
        header.encapPro(new DownloadTask(0, 0, 0, ""), 2222);
        conn.backendService.signalActivity("Ready to send command to slave");
        MasterConnector.remoteStop(header, conn);
    }
}
