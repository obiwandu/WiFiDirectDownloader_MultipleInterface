package edu.pdx.cs410.wifi.direct.file.transfer.master;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.BackendService;
import edu.pdx.cs410.wifi.direct.file.transfer.Statistic;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.HttpDownload;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpConnector;
import edu.pdx.cs410.wifi.direct.file.transfer.ThreadStatistics;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpConnectorLong;

/**
 * Created by User on 7/11/2015.
 */
public class MasterOperation  {
//    static public long remoteDownload(DownloadTask task, File recvFile,
//                                         InetSocketAddress remoteAddr, InetSocketAddress localAddr,
//                                         BackendService masterService) throws Exception {
//        long bw = MasterProxy.remoteDownload(task, recvFile, remoteAddr, localAddr, masterService);
//
//        return bw;
//    }

    /* Short connection */
    static public long remoteDownload(DownloadTask task, RandomAccessFile recvFile,
                                     InetSocketAddress remoteAddr, InetSocketAddress localAddr,
                                     BackendService masterService) throws Exception {
        long bw;
        bw = MasterProxy.remoteDownload(task, recvFile, remoteAddr, localAddr, masterService);

        return bw;
    }

    /* Long connection version */
    static public long remoteDownload(DownloadTask task, RandomAccessFile recvFile,
                                     TcpConnector conn, ThreadStatistics stat) throws Exception {
        long bw;
        bw = MasterProxy.remoteDownload(task, recvFile, conn, stat);

        return bw;
    }

    /* Short connection */
    static public void remoteStop(InetSocketAddress remoteAddr, InetSocketAddress localAddr, BackendService masterService) throws Exception {
        MasterInvoker.remoteStop(remoteAddr, localAddr, masterService);
    }

    /* Long connection */
    static public void remoteStop(TcpConnector conn) throws Exception {
        MasterInvoker.remoteStop(conn);
    }

    static public long httpDownload(DownloadTask task, File recvFile, BackendService masterService) throws Exception {
        long bw;
        if (task.isPartial){
            bw = HttpDownload.partialDownload(task.url, recvFile, task, masterService);
        } else {
            bw = HttpDownload.download(task.url, recvFile, masterService);
        }

        return bw;
    }

    static public long httpDownload(DownloadTask task, RandomAccessFile recvFile, BackendService masterService, ThreadStatistics stat) throws Exception {
        long bw;
        if (task.isPartial){
            bw = HttpDownload.partialDownload(task.url, recvFile, task, masterService, stat);
        } else {
            bw = HttpDownload.download(task.url, recvFile, masterService, stat);
        }

        return bw;
    }
}
