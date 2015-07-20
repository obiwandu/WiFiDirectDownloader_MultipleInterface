package edu.pdx.cs410.wifi.direct.file.transfer.master;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;

/**
 * Created by User on 7/11/2015.
 */
public class MasterOperation  {
    public MasterOperation(){

    }

    static public void remoteDownload(DownloadTask task, File recvFile,
                                         InetSocketAddress remoteAddr, InetSocketAddress localAddr,
                                         MasterService masterService) throws Exception {
        MasterProxy.remoteDownload(task, recvFile, remoteAddr, localAddr, masterService);
    }
}
