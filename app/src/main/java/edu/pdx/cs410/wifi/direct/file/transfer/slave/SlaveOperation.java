package edu.pdx.cs410.wifi.direct.file.transfer.slave;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpTrans;

/**
 * Created by User on 7/12/2015.
 */
public class SlaveOperation {
    static public File httpDownload(DownloadTask task){
        File dlFile = null;
        return dlFile;
    }

    static public void transBack(File file, InetSocketAddress[] sockAddr){
        TcpTrans.send(sockAddr[0], sockAddr[1], file);
    }
}
