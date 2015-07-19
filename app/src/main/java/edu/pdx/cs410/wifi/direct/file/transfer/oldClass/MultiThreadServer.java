package edu.pdx.cs410.wifi.direct.file.transfer.oldClass;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import edu.pdx.cs410.wifi.direct.file.transfer.oldClass.SlaveSendThread;

public class MultiThreadServer extends Thread {
    private boolean serviceEnabled;
    private int port;
    private File saveLocation;
    private Handler handler;
    private String masterIp;
    public boolean isSent;
//    private ResultReceiver serverResult;

    public MultiThreadServer(int port_p, File saveLocation_p, Handler hdl) {
        serviceEnabled = true;
        port = port_p;
        saveLocation = saveLocation_p;
        handler = hdl;
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
        isSent = true;
        try {
            welcomeSocket = new ServerSocket(port);
            while (true && serviceEnabled && isSent) {
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

                /*after getting command, start downloading thread immediately*/
                String url = "www.note-pad.org";
                /*downloading*/

                /*after downloading, start a thread to transfer data*/
                Handler handler = new Handler(Looper.getMainLooper()){
                    @Override
                    public void handleMessage(Message msg){
                        switch(msg.what){
                            case 1:
                            /*data has been sent back, start listen to master again*/
                                isSent = true;
                            default:
                                super.handleMessage(msg);
                        }
                    }
                };
                /*start a new thread to send data back to master*/
                isSent = false;
                Thread slaveSendThread = new Thread(new SlaveSendThread(port, masterIp, handler));
                slaveSendThread.start();
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