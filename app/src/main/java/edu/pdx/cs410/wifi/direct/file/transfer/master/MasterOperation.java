package edu.pdx.cs410.wifi.direct.file.transfer.master;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.BackendService;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.HttpDownload;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpConnector;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpConnectorLong;

/**
 * Created by User on 7/11/2015.
 */
public class MasterOperation  {
    static public int remoteDownload(DownloadTask task, File recvFile,
                                         InetSocketAddress remoteAddr, InetSocketAddress localAddr,
                                         BackendService masterService) throws Exception {
        int bw;
        bw = MasterProxy.remoteDownload(task, recvFile, remoteAddr, localAddr, masterService);

        return bw;
    }

//    static public int remoteDownload(DownloadTask task, RandomAccessFile recvFile,
//                                     InetSocketAddress remoteAddr, InetSocketAddress localAddr,
//                                     BackendService masterService) throws Exception {
//        int bw;
//        bw = MasterProxy.remoteDownload(task, recvFile, remoteAddr, localAddr, masterService);
//
//        return bw;
//    }

    static public int remoteDownload(DownloadTask task, RandomAccessFile recvFile,
                                     InetSocketAddress remoteAddr, InetSocketAddress localAddr,
                                     BackendService masterService) throws Exception {
        int bw;
        bw = MasterProxy.remoteDownload(task, recvFile, remoteAddr, localAddr, masterService);

        return bw;
    }

    static public int remoteDownload(DownloadTask task, RandomAccessFile recvFile,
                                     TcpConnector conn) throws Exception {
        int bw;
        bw = MasterProxy.remoteDownload(task, recvFile, conn);

        return bw;
    }

    static public void remoteStop(InetSocketAddress remoteAddr, InetSocketAddress localAddr, BackendService masterService) throws Exception {
        MasterInvoker.remoteStop(remoteAddr, localAddr, masterService);
    }

    static public void remoteStop(TcpConnector conn) throws Exception {
        MasterInvoker.remoteStop(conn);
    }

    static public int httpDownload(DownloadTask task, File recvFile) throws Exception {
        if (task.isPartial){
            HttpDownload.partialDownload(task.url, recvFile, task);
        } else {
            HttpDownload.download(task.url, recvFile);
        }
        int bw = 0;
        return bw;
    }

    static public int httpDownload(DownloadTask task, File recvFile, BackendService masterService) throws Exception {
        int bw;
        if (task.isPartial){
            bw = HttpDownload.partialDownload(task.url, recvFile, task, masterService);
        } else {
            bw = HttpDownload.download(task.url, recvFile, masterService);
        }

        return bw;
    }

    static public int httpDownload(DownloadTask task, RandomAccessFile recvFile, BackendService masterService) throws Exception {
        int bw;
        if (task.isPartial){
            bw = HttpDownload.partialDownload(task.url, recvFile, task, masterService);
        } else {
            bw = HttpDownload.download(task.url, recvFile, masterService);
        }

        return bw;
    }
}
