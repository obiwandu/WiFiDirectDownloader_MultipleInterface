package edu.pdx.cs410.wifi.direct.file.transfer.trans;

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
 * Created by User on 7/6/2015.
 */
public class TcpTrans {
    static public InetAddress connect(InetSocketAddress remoteAddr) {
        InetAddress localIp = null;
        try {
            Socket socket = new Socket();
            socket.setReuseAddress(true);
            socket.connect(remoteAddr, 0);
            InetSocketAddress localSockAddr = (InetSocketAddress)socket.getLocalSocketAddress();
            localIp = localSockAddr.getAddress();

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return localIp;
    }

    static public InetAddress listen(InetSocketAddress localAddr) {
        InetAddress remoteIp = null;
        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(localAddr);
            Socket socket = serverSocket.accept();
            InetSocketAddress remoteSockAddr = (InetSocketAddress)socket.getRemoteSocketAddress();
            remoteIp = remoteSockAddr.getAddress();

            socket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return remoteIp;
    }

    static public InetSocketAddress[] recv(InetSocketAddress localAddr, byte[] recvBuf) {
        InetSocketAddress[] retSockAddr = new InetSocketAddress[2];
        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(localAddr);
            Socket socket = serverSocket.accept();
            retSockAddr[0] = (InetSocketAddress)socket.getRemoteSocketAddress();
            retSockAddr[1] = localAddr;
            InputStream is = socket.getInputStream();
            int bytesRead;

            bytesRead = is.read(recvBuf, 0, recvBuf.length);
            if (bytesRead == -1) {
                throw new Exception();
            }

            is.close();
            socket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retSockAddr;
    }

    static public InetSocketAddress[] recv(InetSocketAddress localAddr, File recvFile) {
        InetSocketAddress[] retSockAddr = new InetSocketAddress[2];
        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(localAddr);
            Socket socket = serverSocket.accept();
            retSockAddr[0] = (InetSocketAddress)socket.getRemoteSocketAddress();
            retSockAddr[1] = localAddr;
            InputStream is = socket.getInputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;

            FileOutputStream fos = new FileOutputStream(recvFile);
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

            is.close();
            socket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retSockAddr;
    }

    static public void send(InetSocketAddress remoteAddr, InetSocketAddress localAddr, byte[] sendBuf) {
        try {
            Socket socket = new Socket();
            socket.setReuseAddress(true);
            socket.bind(localAddr);
            socket.connect(remoteAddr, 0);
            OutputStream os = socket.getOutputStream();
            byte[] buffer;
            int bytesRead;

            buffer = sendBuf;
            bytesRead = buffer.length;
            os.write(buffer, 0, bytesRead);
            os.flush();

            os.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public void send(InetSocketAddress remoteAddr, InetSocketAddress localAddr, File sendFile) {
        try {
            Socket socket = new Socket();
            socket.setReuseAddress(true);
            socket.bind(localAddr);
            socket.connect(remoteAddr, 0);
            OutputStream os = socket.getOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;

            FileInputStream fis = new FileInputStream(sendFile);
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

            os.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
