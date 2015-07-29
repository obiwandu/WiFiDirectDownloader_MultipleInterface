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
import edu.pdx.cs410.wifi.direct.file.transfer.slave.SlaveService;

/**
 * Created by User on 7/25/2015.
 */
public class TcpConnectorLong {
    private Socket socket;
    private BackendService backendService;
    private ServerSocket serverSocket;
    private MasterService masterService;
    private InputStream is;
    private OutputStream os;
    private boolean isMaster;

    public TcpConnectorLong(InetSocketAddress remoteAddr, InetSocketAddress localAddr, BackendService ms, int type) throws Exception {
        backendService = ms;
        if (type == 0) {
            isMaster = true;
            serverSocket = null;
            socket = new Socket();
            socket.setReuseAddress(true);
            socket.bind(localAddr);
            socket.connect(remoteAddr, 0);
            ms.signalActivity("Slave connected");
            is = socket.getInputStream();
            os = socket.getOutputStream();
        } else if (type == 1) {
            isMaster = false;
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

//    public TcpConnectorLong(InetSocketAddress localAddr, BackendService ms) throws Exception {
//        backendService = ms;
//        serverSocket.setReuseAddress(true);
//        serverSocket.bind(localAddr);
//        socket = serverSocket.accept();
//    }

    public void close() throws Exception {
        is.close();
        os.close();
        socket.close();
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    public long recv(RandomAccessFile recvFile, int dataLen) throws Exception {
        BwMetric bwMetric = new BwMetric(masterService, dataLen);

//        InputStream is = socket.getInputStream();
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
            bwMetric.bwMetric(bytesRead, isMaster);

            recvFile.write(buffer, 0, bytesRead);
        }
//        is.close();

        return bwMetric.bw;
    }

    public void recv(byte[] recvBuf, int dataLen) throws Exception {
//        InputStream is = socket.getInputStream();
        int bytesRead;

        bytesRead = is.read(recvBuf, 0, dataLen);
        if (bytesRead == -1) {
            throw new Exception("No data received!");
        }
//        is.close();

        return;
    }

    public void recv(byte[] recvBuf) throws Exception {
//        InputStream is = socket.getInputStream();
        int bytesRead;

        bytesRead = is.read(recvBuf, 0, recvBuf.length);
        if (bytesRead == -1) {
            throw new Exception("No data received!");
        }
//        is.close();

        return;
    }

    public void send(File sendFile) throws Exception {
        Socket socket = new Socket();

//        OutputStream os = socket.getOutputStream();
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
//        os.close();

        return;
    }

    public void send(byte[] sendBuf) throws Exception {
//        OutputStream os = socket.getOutputStream();
        int bytesRead;

        bytesRead = sendBuf.length;
        os.write(sendBuf, 0, bytesRead);
        os.flush();
//        os.close();

        return;
    }
}
