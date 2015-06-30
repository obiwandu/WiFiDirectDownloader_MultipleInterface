package edu.pdx.cs410.wifi.direct.file.transfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import android.os.Bundle;
import android.app.Activity;
import android.os.ResultReceiver;
import android.widget.TextView;

public class MultiThreadServer extends Thread {
    private boolean serviceEnabled;
    private int port;
    private File saveLocation;
//    private ResultReceiver serverResult;

    public MultiThreadServer(int port_p, File saveLocation_p) {
        serviceEnabled = true;
        port = port_p;
        saveLocation = saveLocation_p;
    }

    @Override
    public void run() {
//        port = ((Integer) intent.getExtras().get("port")).intValue();
//        saveLocation = (File) intent.getExtras().get("saveLocation");
//        serverResult = (ResultReceiver) intent.getExtras().get("serverResult");
        //signalActivity("Starting to download");
        String fileName = "";
        ServerSocket welcomeSocket = null;
        Socket socket = null;
        try {
            welcomeSocket = new ServerSocket(port);
            while (true && serviceEnabled) {
                //Listen for incoming connections on specified port
                //Block thread until someone connects
                socket = welcomeSocket.accept();
                InetAddress slaveIp = socket.getInetAddress();
//                signalActivity("TCP Connection Established: " + socket.toString() + " Starting file transfer");

                InputStream is = socket.getInputStream();
//                signalActivity("About to start handshake");

                //Client-Server handshake
//                signalActivity("Handshake complete, getting file: " + fileName);
                String savedAs = "WDFL_File_" + System.currentTimeMillis();
//                File file = new File(saveLocation, savedAs);
//                File file = new File(saveLocation);
                byte[] buffer = new byte[4096];
                int bytesRead;
                FileOutputStream fos = new FileOutputStream(saveLocation);
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
//                signalActivity("File Transfer Complete, saved as: " + savedAs);

//                signalActivity("File Transfer Complete, saved as: " + saveLocation.toString() + ". Ready to send file back");
                socket = new Socket(slaveIp, port);
                //send the task back to master
                OutputStream os = socket.getOutputStream();
                FileInputStream fis = new FileInputStream(saveLocation);
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
                welcomeSocket.close();
//                signalActivity("File send back successfully!");
                //Start writing to file
            }
        } catch (IOException e) {
//            signalActivity(e.getMessage());
        } catch (Exception e) {
//            signalActivity(e.getMessage());
        }
        //Signal that operation is complete
//        serverResult.send(port, null);
    }
}