package edu.pdx.cs410.wifi.direct.file.transfer.slave;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.BackendService;
import edu.pdx.cs410.wifi.direct.file.transfer.ProtocolHeader;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpConnector;
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

        long bw = SlaveOperation.httpDownload(task, tempFile, slaveService);
        slaveService.signalActivity("Download complete, ready to send back");
        SlaveOperation.transBack(tempFile, sockAddr);
        slaveService.signalActivity("Send back succefully, task complete");
    }

    static public void downLoad(InetSocketAddress remoteAddr, InetSocketAddress localAddr, DownloadTask task,BackendService slaveService) throws Exception {
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
        long bw = SlaveOperation.httpDownload(task, tempFile, slaveService);
        slaveService.signalActivity("Download complete, ready to send back");
//        ProtocolHeader header = new ProtocolHeader();
//        header.encapPro(task, 1111);
        ProtocolHeader header = new ProtocolHeader(task, bw);
        TcpConnector conn = new TcpConnector(remoteAddr, localAddr, slaveService, 0);
        slaveService.signalActivity("Sending data back to master");
        conn.send(header.header, ProtocolHeader.HEADER_LEN);
        conn.send(tempFile, (int)(task.end - task.start + 1));
        conn.close();
    }

    /* Long connection version */
    static public void downLoad(DownloadTask task, TcpConnector conn) throws Exception {
        /* Initialize storage directory */
        String recvPath;
        String recvFileName;
        recvPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        recvPath += "/A-NRS-Temp";
        recvFileName = "Recved";
        File dir = new File(recvPath);
        dir.mkdirs();
        File tempFile = new File(recvPath, recvFileName);
        long bw = SlaveOperation.httpDownload(task, tempFile, conn.backendService);
        conn.backendService.signalActivity("Download complete, ready to send back");

        /* Capsulate header and send data back to master */
        ProtocolHeader header = new ProtocolHeader(task, bw);
//        header.encapPro(task, 1111);
//        ProtocolHeader header = new ProtocolHeader(task)
        conn.backendService.signalActivity("Sending data back to master");
        SlaveOperation.transBack(conn, header, tempFile, (int)(task.end - task.start + 1));
//        conn.send(header.header, ProtocolHeader.HEADER_LEN);
//        conn.send(tempFile, (int)(task.end - task.start + 1));
    }
}
