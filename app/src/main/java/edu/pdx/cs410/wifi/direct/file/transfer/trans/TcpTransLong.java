package edu.pdx.cs410.wifi.direct.file.transfer.trans;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import edu.pdx.cs410.wifi.direct.file.transfer.master.MasterService;

/**
 * Created by User on 7/25/2015.
 */
public class TcpTransLong {
    private Socket socket;

//    static public int recv(InetSocketAddress localAddr, byte[] recvBuf, MasterService masterService) throws Exception {
//        ServerSocket serverSocket = new ServerSocket();
//        BwMetric bwMetric = new BwMetric(masterService);
//        try {
//            serverSocket.setReuseAddress(true);
//            serverSocket.bind(localAddr);
//            Socket socket = serverSocket.accept();
//            InputStream is = socket.getInputStream();
//            byte[] buffer = new byte[4096];
//            int bytesRead;
//            FileOutputStream fos = new FileOutputStream(recvFile);
//            BufferedOutputStream bos = new BufferedOutputStream(fos);
//            while (true) {
//                bytesRead = is.read(buffer, 0, buffer.length);
//                if (bytesRead == -1) {
//                    break;
//                }
//
//                /* update bw */
//                bwMetric.bwMetric(bytesRead);
//
//                bos.write(buffer, 0, bytesRead);
//                bos.flush();
//            }
//            fos.close();
//            bos.close();
//            is.close();
//            socket.close();
//        } finally {
//            serverSocket.close();
//        }
//        return bwMetric.bw;
//    }

    static public void send(InetSocketAddress remoteAddr, InetSocketAddress localAddr, byte[] sendBuf) throws Exception {
        Socket socket = new Socket();
        try {
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
        } finally {
            socket.close();
        }
    }
}
