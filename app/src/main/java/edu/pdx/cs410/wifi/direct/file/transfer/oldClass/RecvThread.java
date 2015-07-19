package edu.pdx.cs410.wifi.direct.file.transfer.oldClass;

import android.os.Handler;
import android.os.Message;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by User on 7/4/2015.
 */
public class RecvThread extends Thread {

    private boolean serviceEnabled;
    private int localPort;
    private InetAddress localIp;
    private InetAddress remoteIp;
    private File fileSent;
    private String strSent;
    private Handler handler;
    private String masterIp;
    private int dataType;
    private byte[] recvBuf;
    public boolean isSent;
//    private ResultReceiver serverResult;

    public RecvThread(int port, Handler hdl, File file) {
        serviceEnabled = true;
        localPort = port;
        handler = hdl;
        dataType = 1;
        fileSent = file;
    }

    public RecvThread(int port, Handler hdl, byte[] buf) {
        serviceEnabled = true;
        localPort = port;
        handler = hdl;
        dataType = 2;
        recvBuf = buf;
    }

    @Override
    public void run() {
        String fileName = "";
        isSent = true;
        try {
//            Message msg2 = handler.obtainMessage(2, "start handshake");
//            msg2.sendToTarget();
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(localPort));
            Socket socket = serverSocket.accept();
            remoteIp = socket.getInetAddress();
            localIp = socket.getLocalAddress();
            Message msg1 = handler.obtainMessage(1, remoteIp.toString());
            msg1.sendToTarget();
            InputStream is = socket.getInputStream();
            String savedAs = "WDFL_File_" + System.currentTimeMillis();
            byte[] buffer = new byte[4096];
            int bytesRead;
            switch (dataType) {
                case 1:
                    FileOutputStream fos = new FileOutputStream(fileSent);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    while (true) {
                        bytesRead = is.read(buffer, 0, buffer.length);
                        if (bytesRead == -1) {
                            break;
                        }
                        bos.write(buffer, 0, bytesRead);
                        bos.flush();
                    }
                    fos.close();
                    bos.close();
                    break;
                case 2:
                    bytesRead = is.read(recvBuf, 0, recvBuf.length);
                    if (bytesRead == -1) {
                        break;
                    }
                    break;
                default:
                    break;
            }
            is.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
//            signalActivity(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
//            signalActivity(e.getMessage());
        }
    }
}
