package edu.pdx.cs410.wifi.direct.file.transfer.slave;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.BackendService;
import edu.pdx.cs410.wifi.direct.file.transfer.ProtocolHeader;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpConnectorLong;

/**
 * Created by User on 7/12/2015.
 */
public class SlaveInvoker {
    static public void downLoad(DownloadTask task, InetSocketAddress[] sockAddr, BackendService slaveService) throws Exception {
        File tempFile = null;

        /*initialize storage directory*/
        String recvPath;
        String recvFileName;
        recvPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        recvPath += "/A-NRS-Temp";
        recvFileName = "Recved";
        File dir = new File(recvPath);
        dir.mkdirs();
        /*write something to the file*/
        tempFile = new File(recvPath, recvFileName);

        tempFile = SlaveOperation.httpDownload(task, tempFile, slaveService);
        slaveService.signalActivity("Download complete, ready to send back");
        SlaveOperation.transBack(tempFile, sockAddr);
        slaveService.signalActivity("Send back succefully, task complete");
    }

    static public void downLoad(TcpConnectorLong conn, DownloadTask task,BackendService slaveService) throws Exception {
        File tempFile = null;

        /*initialize storage directory*/
        String recvPath;
        String recvFileName;
        recvPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        recvPath += "/A-NRS-Temp";
        recvFileName = "Recved";
        File dir = new File(recvPath);
        dir.mkdirs();
        /*write something to the file*/
        tempFile = new File(recvPath, recvFileName);

        tempFile = SlaveOperation.httpDownload(task, tempFile, slaveService);
        slaveService.signalActivity("Download complete, ready to send back");
        ProtocolHeader header = new ProtocolHeader();
        header.encapPro(task, 1111);
        conn.send(header.header);
        conn.send(tempFile);
//        SlaveOperation.transBack(tempFile, sockAddr);
        slaveService.signalActivity("Send back succefully, task complete");
    }
}
