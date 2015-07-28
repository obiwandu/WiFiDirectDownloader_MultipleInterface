package edu.pdx.cs410.wifi.direct.file.transfer.trans;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import edu.pdx.cs410.wifi.direct.file.transfer.BackendService;
import edu.pdx.cs410.wifi.direct.file.transfer.master.MasterService;

/**
 * Created by User on 7/25/2015.
 */
public class TcpConnector {
    private Socket socket;
    public BackendService backendService;
    private ServerSocket serverSocket;
    private MasterService masterService;
    private InputStream is;
    private OutputStream os;

    public TcpConnector(InetSocketAddress remoteAddr, InetSocketAddress localAddr, BackendService ms, int type) throws Exception {
        backendService = ms;
        if (type == 0) {
            serverSocket = null;
            socket = new Socket();
            socket.setReuseAddress(true);
            socket.bind(localAddr);
            socket.connect(remoteAddr, 0);
            ms.signalActivity("Slave connected");
            is = socket.getInputStream();
            os = socket.getOutputStream();
        } else if (type == 1) {
            backendService = ms;
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(localAddr);
            ms.signalActivity("Waiting for request from master");
            socket = serverSocket.accept();
            ms.signalActivity("Master connected");
        }
        is = socket.getInputStream();
        os = socket.getOutputStream();
    }

    public void close() throws Exception {
        is.close();
        os.close();
        socket.close();
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    public int recv(RandomAccessFile recvFile, int dataLen) throws Exception {
        BwMetric bwMetric = new BwMetric(backendService);

        byte[] buffer = new byte[4096];
        int bytesRead;
        int alreadyLen = 0;
        int currentLen = 0;

        while (true) {
            if (dataLen - alreadyLen < buffer.length) {
                currentLen = dataLen - alreadyLen;
            } else {
                currentLen = buffer.length;
            }

            bytesRead = is.read(buffer, 0, currentLen);
            /* update alreadyLen */
            alreadyLen += bytesRead;
            if (alreadyLen == dataLen) {
                break;
            }
            if (bytesRead == -1) {
                throw new Exception("Data receiving is not complete!");
            }

            /* update bw */
            bwMetric.bwMetric(bytesRead);

            recvFile.write(buffer, 0, bytesRead);
        }

        return bwMetric.bw;
    }

    public void recv(byte[] recvBuf, int dataLen) throws Exception {
        int bytesRead;

        bytesRead = is.read(recvBuf, 0, dataLen);
        if (bytesRead == -1) {
            throw new Exception("No data received!");
        }

        return;
    }

//    public void recv(byte[] recvBuf) throws Exception {
//        int bytesRead;
//
//        bytesRead = is.read(recvBuf, 0, recvBuf.length);
//        if (bytesRead == -1) {
//            throw new Exception("No data received!");
//        }
//
//        return;
//    }

    public void send(File sendFile, int fileLen) throws Exception {
        byte[] buffer = new byte[4096];
        int bytesRead;
        int alreadyLen = 0;
        int currentLen = 0;
        int totalLen = 0;

        FileInputStream fis = new FileInputStream(sendFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        while (true) {
            if (fileLen - alreadyLen < buffer.length) {
                currentLen = fileLen - alreadyLen;
            } else {
                currentLen = buffer.length;
            }
            bytesRead = bis.read(buffer, 0, currentLen);
            if (bytesRead == -1 || bytesRead == 0) {
                break;
            }
            alreadyLen += bytesRead;
            totalLen += bytesRead;
            os.write(buffer, 0, bytesRead);
            os.flush();
        }
        fis.close();
        bis.close();
        int a = totalLen;

        return;
    }

    public void send(byte[] sendBuf, int bufLen) throws Exception {
        int bytesRead;

//        bytesRead = sendBuf.length;
        bytesRead = bufLen;
        os.write(sendBuf, 0, bytesRead);
        os.flush();

        return;
    }
}
