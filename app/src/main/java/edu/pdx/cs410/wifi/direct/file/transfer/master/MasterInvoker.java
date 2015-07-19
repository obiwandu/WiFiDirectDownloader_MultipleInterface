package edu.pdx.cs410.wifi.direct.file.transfer.master;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;

/**
 * Created by User on 7/11/2015.
 */
public class MasterInvoker {
    static private String encapCmd(DownloadTask task){
        String command = "taskstart:" + Integer.toString(task.start) + "\n" + "taskend:" + Integer.toString(task.end) + "\n"
                         + "partial:" + task.isPartial + "\n"+ "url:" + task.url + "\n";
        return command;
    }

    static public void remoteDownload(DownloadTask task, File recvFile, InetSocketAddress remoteAddr, InetSocketAddress localAddr){
        String command = encapCmd(task);
        MasterConnector.remoteDownload(command, recvFile, remoteAddr, localAddr);
    }
}
