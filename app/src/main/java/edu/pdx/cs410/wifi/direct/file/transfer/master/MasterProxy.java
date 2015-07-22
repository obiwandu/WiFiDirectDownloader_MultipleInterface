package edu.pdx.cs410.wifi.direct.file.transfer.master;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;

/**
 * Created by User on 7/11/2015.
 */
public class MasterProxy {
    static public int remoteDownload(DownloadTask task, File recvFile,
                                         InetSocketAddress remoteAddr, InetSocketAddress localAddr,
                                         MasterService masterService) throws Exception {
        int bw;
        bw = MasterInvoker.remoteDownload(task, recvFile, remoteAddr, localAddr, masterService);
        return bw;
    }

    static public int remoteDownload(DownloadTask task, RandomAccessFile recvFile,
                                     InetSocketAddress remoteAddr, InetSocketAddress localAddr,
                                     MasterService masterService) throws Exception {
        int bw;
        bw = MasterInvoker.remoteDownload(task, recvFile, remoteAddr, localAddr, masterService);
        return bw;
    }
}
