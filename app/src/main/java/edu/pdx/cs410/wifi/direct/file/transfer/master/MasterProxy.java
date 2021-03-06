package edu.pdx.cs410.wifi.direct.file.transfer.master;

import java.io.RandomAccessFile;
import java.net.InetSocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.BackendService;
import edu.pdx.cs410.wifi.direct.file.transfer.ThreadStatistics;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpConnector;

/**
 * Created by User on 7/11/2015.
 */
public class MasterProxy {
//    static public long remoteDownload(DownloadTask task, File recvFile,
//                                         InetSocketAddress remoteAddr, InetSocketAddress localAddr,
//                                     BackendService masterService) throws Exception {
//        long bw = MasterInvoker.remoteDownload(task, recvFile, remoteAddr, localAddr, masterService);
//        return bw;
//    }

//    static public int remoteDownload(DownloadTask task, RandomAccessFile recvFile,
//                                     InetSocketAddress remoteAddr, InetSocketAddress localAddr,
//                                     MasterService masterService) throws Exception {
//        int bw;
//        bw = MasterInvoker.remoteDownload(task, recvFile, remoteAddr, localAddr, masterService);
//        return bw;
//    }

    /* Short connection */
    static public long remoteDownload(DownloadTask task, RandomAccessFile recvFile,
                                     InetSocketAddress remoteAddr, InetSocketAddress localAddr,
                                     BackendService masterService) throws Exception {
        long bw;
        bw = MasterInvoker.remoteDownload(task, recvFile, remoteAddr, localAddr, masterService);
        return bw;
    }

    /* Long connection version */
    static public long remoteDownload(DownloadTask task, RandomAccessFile recvFile,
                                     TcpConnector conn, ThreadStatistics stat) throws Exception {
        long bw;
        bw = MasterInvoker.remoteDownload(task, recvFile, conn, stat);
        return bw;
    }
}
