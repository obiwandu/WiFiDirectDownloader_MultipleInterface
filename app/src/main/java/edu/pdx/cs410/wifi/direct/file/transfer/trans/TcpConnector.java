package edu.pdx.cs410.wifi.direct.file.transfer.trans;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import edu.pdx.cs410.wifi.direct.file.transfer.BackendService;
import edu.pdx.cs410.wifi.direct.file.transfer.Log;
import edu.pdx.cs410.wifi.direct.file.transfer.Statistic;
import edu.pdx.cs410.wifi.direct.file.transfer.ThreadStatistics;
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
//    private BufferedInputStream bis;
//    private BufferedOutputStream bos;
    private boolean isLAN;
    private Log log;

    public TcpConnector(InetSocketAddress remoteAddr, InetSocketAddress localAddr, BackendService ms, int type) throws Exception {
        backendService = ms;
        isLAN = true;
        if (type == 0) {
            log = new Log("master_connector_log");
            serverSocket = null;
            socket = new Socket();
            socket.setReuseAddress(true);
            socket.bind(localAddr);
            socket.connect(remoteAddr, 0);
            ms.signalActivity("Slave connected");
        } else if (type == 1) {
            log = new Log("slave_connector_log");
            backendService = ms;
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(localAddr);
            ms.signalActivity("Waiting for request from master");
            socket = serverSocket.accept();
            ms.signalActivity("Master connected");
        }
        is = socket.getInputStream();
//        bis = new BufferedInputStream(is);
        os = socket.getOutputStream();
//        bos = new BufferedOutputStream(os);
    }

    public void close() throws Exception {
        is.close();
        os.close();
//        bis.close();
//        bos.close();
        socket.close();
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    /* Limit the data len */
//    public long recv(RandomAccessFile recvFile, long dataLen) throws Exception {
//        BwMetric bwMetric = new BwMetric(backendService, dataLen);
//
//        byte[] buffer = new byte[4096];
//        int bytesRead;
//        long alreadyLen = 0;
//        int currentLen = 0;
//
//        while (true) {
//            if (dataLen - alreadyLen < buffer.length) {
//                currentLen = (int)(dataLen - alreadyLen);
//            } else {
//                currentLen = buffer.length;
//            }
//
//            bytesRead = is.read(buffer, 0, currentLen);
//            /* update alreadyLen */
//            alreadyLen += bytesRead;
//            if (alreadyLen == dataLen) {
//                break;
//            }
//            if (bytesRead == -1) {
//                throw new Exception("Data receiving is not complete!");
//            }
//
//            /* update bw */
//            bwMetric.bwMetric(bytesRead, isLAN);
//
//            recvFile.write(buffer, 0, bytesRead);
//        }
//
//        return bwMetric.bw;
//    }

    /* Data len is not limited */
    public long recv(RandomAccessFile recvFile, int dataLen) throws Exception {
        BwMetric bwMetric = new BwMetric(backendService, dataLen);

        byte[] buffer = new byte[4096];
        int bytesRead;
        int alreadyLen = 0;
        int currentLen = 0;
        int leftLen;
//        int realLen = 0;
//        int absStart = 0;
//        int absEnd = 0;

        while (true) {
            leftLen = dataLen - alreadyLen;
            currentLen = (leftLen < buffer.length) ? leftLen : buffer.length;

            bytesRead = is.read(buffer, 0, currentLen);
//            absStart = alreadyLen;
//            absEnd = absStart + bytesRead - 1;
            /* update alreadyLen */
            alreadyLen += bytesRead;
            if (bytesRead <= 0) {
                break;
            }
            if (leftLen == 0) {
                break;
            }

//            /* Skip first 2KB data */
//            int offset = 0;
//            if (absStart < 2048) {
//                if (absEnd < 2048) {
//                    /* Useless data, skip */
//                } else {
//                    /* Fist useful data block */
////                    offset = absEnd - 2048 + 1;
//                    offset = 2048 - absStart;
//                    recvFile.write(buffer, offset, bytesRead - offset);
//                    realLen += bytesRead - offset;
//                }
//            } else {
//                recvFile.write(buffer, 0, bytesRead);
//                realLen += bytesRead;
//            }
            recvFile.write(buffer, 0, bytesRead);
            /* update bw */
            bwMetric.bwMetric(bytesRead, isLAN);
        }
        log.record("RECV:expect data len:" + Integer.toString(dataLen) + "|actual data len:" + Integer.toString(alreadyLen));

        return bwMetric.bw;
    }

    /* Data len is not limited, with statistic */
    public long recv(RandomAccessFile recvFile, int dataLen, ThreadStatistics stat) throws Exception {
        BwMetric bwMetric = new BwMetric(backendService, dataLen);

        byte[] buffer = new byte[4096];
        int bytesRead;
        int alreadyLen = 0;
        int currentLen = 0;
        int leftLen;

        stat.startMetric( dataLen);
        while (true) {
            leftLen = dataLen - alreadyLen;
            currentLen = (leftLen < buffer.length) ? leftLen : buffer.length;

            bytesRead = is.read(buffer, 0, currentLen);
            /* update alreadyLen */
            alreadyLen += bytesRead;
            if (bytesRead <= 0) {
                break;
            }
            if (leftLen == 0) {
                break;
            }

            recvFile.write(buffer, 0, bytesRead);
            /* update bw */
            bwMetric.bwMetric(bytesRead, isLAN);
            /* Statistic */
            stat.updateMetric((long) bytesRead, backendService, isLAN);
//            stat.stat(bytesRead);
        }
        log.record("RECV:expect data len:" + Integer.toString(dataLen) + "|actual data len:" + Integer.toString(alreadyLen));

//        return bwMetric.bw;
        return stat.bw;
    }

    public void recv(byte[] recvBuf, int dataLen) throws Exception {
        int bytesRead;

        bytesRead = is.read(recvBuf, 0, dataLen);
        if (bytesRead == -1) {
            throw new Exception("No data received!");
        }

        return;
    }

    /* Limit the data len */
//    public void send(File sendFile, long fileLen) throws Exception {
//        byte[] buffer = new byte[4096];
//        int bytesRead;
//        long alreadyLen = 0;
//        int currentLen = 0;
//        int totalLen = 0;
//
//        FileInputStream fis = new FileInputStream(sendFile);
//        BufferedInputStream bis = new BufferedInputStream(fis);
//        while (true) {
//            if (fileLen - alreadyLen < buffer.length) {
//                currentLen = (int)(fileLen - alreadyLen);
//            } else {
//                currentLen = buffer.length;
//            }
//            bytesRead = bis.read(buffer, 0, currentLen);
//            if (bytesRead == -1 || bytesRead == 0) {
//                break;
//            }
//            alreadyLen += bytesRead;
//            totalLen += bytesRead;
//            os.write(buffer, 0, bytesRead);
//            os.flush();
//        }
//        fis.close();
//        bis.close();
//        int a = totalLen;
//
//        return;
//    }

    /* Data len is not limited */
    public void send(File sendFile, int fileLen) throws Exception {
        byte[] buffer = new byte[4096];
        int bytesRead;
        int alreadyLen = 0;
        int leftLen = 0;
        int currentLen = 0;

        FileInputStream fis = new FileInputStream(sendFile);
        BufferedInputStream bfis = new BufferedInputStream(fis);

        while (true) {
            leftLen = fileLen - alreadyLen;
            currentLen = (leftLen < buffer.length) ? leftLen : buffer.length;
            bytesRead = bfis.read(buffer);
            if (bytesRead <= 0) {
                break;
            }
            alreadyLen += bytesRead;
            os.write(buffer, 0, bytesRead);
            os.flush();
        }
        log.record("SEND:expect data len:" + Integer.toString(fileLen) + "|actual data len:" + Integer.toString(alreadyLen));
        fis.close();
        //bis.close();

        return;
    }

    public void send(byte[] sendBuf, int bufLen) throws Exception {
        int bytesRead;

        bytesRead = bufLen;
        os.write(sendBuf, 0, bytesRead);
        os.flush();

        return;
    }
}
