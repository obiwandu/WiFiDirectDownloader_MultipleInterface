package edu.pdx.cs410.wifi.direct.file.transfer.master;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpTrans;

/**
 * Created by User on 7/11/2015.
 */
public class MasterConnector {
    static public void remoteDownload (String command, File recvFile,
                                         InetSocketAddress remoteAddr, InetSocketAddress localAddr,
                                         MasterService masterService) throws Exception {
        byte[] sendBuf = command.getBytes();
        TcpTrans.send(remoteAddr, localAddr, sendBuf);
        masterService.signalActivity("Command is sent successfully, start waiting for data sent back");
        TcpTrans.recv(localAddr, recvFile);
        masterService.signalActivity("Final data got successfully, task complete");
    }
}
