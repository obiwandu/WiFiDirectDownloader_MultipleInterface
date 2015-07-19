package edu.pdx.cs410.wifi.direct.file.transfer.slave;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;

/**
 * Created by User on 7/12/2015.
 */
public class SlaveInvoker {
    static public void downLoad(DownloadTask task, InetSocketAddress[] sockAddr){
        File tempFile;
        tempFile = SlaveOperation.httpDownload(task);
        SlaveOperation.transBack(tempFile, sockAddr);
    }
}
