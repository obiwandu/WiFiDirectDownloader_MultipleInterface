package edu.pdx.cs410.wifi.direct.file.transfer.oldClass;

import android.os.Handler;
import android.os.Looper;
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
import java.net.SocketAddress;

/**
 * Created by User on 7/4/2015.
 */
public class SendThread extends Thread {

    private boolean serviceEnabled;
    private int remotePort;
    private InetAddress remoteIp;
    private InetAddress localIp;
    private File fileSent;
    private String strSent;
    private Handler handler;
    private String masterIp;
    private int dataType;
    public boolean isSent;
//    private ResultReceiver serverResult;

    public SendThread(int port, InetAddress ip, Handler hdl, File file) {
        serviceEnabled = true;
        remotePort = port;
        remoteIp = ip;
        handler = hdl;
        dataType = 1;
        fileSent = file;
    }

    public SendThread(int port, InetAddress ip, Handler hdl, String buf) {
        serviceEnabled = true;
        remotePort = port;
        remoteIp = ip;
        handler = hdl;
        dataType = 2;
        strSent = buf;
    }

    @Override
    public void run() {
        String fileName = "";
        isSent = true;
        try {
            Socket socket = new Socket();
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(remotePort));
            socket.connect(new InetSocketAddress(remoteIp, remotePort), 0);
            remoteIp = socket.getInetAddress();
            localIp = socket.getLocalAddress();
            OutputStream os = socket.getOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            switch (dataType) {
                case 1:
                    FileInputStream fis = new FileInputStream(fileSent);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    while (true) {
                        bytesRead = bis.read(buffer, 0, buffer.length);
                        if (bytesRead == -1) {
                            break;
                        }
                        os.write(buffer, 0, bytesRead);
                        os.flush();
                    }
                    fis.close();
                    bis.close();
                    break;
                case 2:
                    buffer = strSent.getBytes();
                    bytesRead = buffer.length;
                    if (bytesRead == -1) {
                        break;
                    }
                    os.write(buffer, 0, bytesRead);
                    os.flush();
                    break;
                default:
                    break;
            }
            os.close();
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
