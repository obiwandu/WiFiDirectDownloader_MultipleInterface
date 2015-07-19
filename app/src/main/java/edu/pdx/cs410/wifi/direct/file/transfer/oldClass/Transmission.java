package edu.pdx.cs410.wifi.direct.file.transfer.oldClass;

import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by User on 6/28/2015.
 */
public class Transmission {
    private String recvPath;
    private String sendPath;
    private String recvFileName;
    private String sendFileName;

    public Transmission() {
        sendPath = recvPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        sendPath += "/A-NRS-Send";
        recvPath += "/A-NRS-Recv";
        sendFileName = "ToBeSent";
        recvFileName = "Recved";
        initDir();
        initTestFile();
    }

    private void initDir() {
        File dir = new File(recvPath);
        dir.mkdirs();
        dir = new File(sendPath);
        dir.mkdirs();
    }

    private void initTestFile() {
        /*write something to the file*/
        try {
            File file = new File(sendPath, sendFileName);
            FileOutputStream fos = new FileOutputStream(file);
            String content = "Just for test, here is all the content...";
            fos.write(content.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void recvFileBack(Socket socket) {
        try {
            //Client-Server handshake
//            String savedAs = recvFileName + System.currentTimeMillis();
//            signalActivity("About to start handshake");

            String savedAs = recvFileName + "-part2";
            File file = new File(recvPath, savedAs);

            byte[] buffer = new byte[4096];
            int bytesRead;

//            signalActivity("About to start handshake");
            InputStream is = socket.getInputStream();
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            while (true) {
                bytesRead = is.read(buffer, 0, buffer.length);
                if (bytesRead == -1) {
                    break;
                }
                bos.write(buffer, 0, bytesRead);
                bos.flush();

            }

            bos.close();
            fos.close();

//            signalActivity("File Transfer Complete, saved as: " + savedAs);
        } catch (Exception e) {
//            signalActivity(e.getMessage());
        }
    }

    public void recvFile(Socket socket) {
        try {
//            signalActivity("About to start handshake");
            //Client-Server handshake
//            String savedAs = recvFileName + System.currentTimeMillis();
            String savedAs = recvFileName;
            File file = new File(recvPath, savedAs);

            byte[] buffer = new byte[4096];
            int bytesRead;

//            signalActivity("About to start handshake");
            InputStream is = socket.getInputStream();
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            while (true) {
                bytesRead = is.read(buffer, 0, buffer.length);
                if (bytesRead == -1) {
                    break;
                }
                bos.write(buffer, 0, bytesRead);
                bos.flush();

            }

            bos.close();
            fos.close();
//            signalActivity("File Transfer Complete, saved as: " + savedAs);
        } catch (Exception e) {
//            signalActivity(e.getMessage());
        }
    }

    public void sendFile(Socket socket) {
        try {
//            signalActivity("About to start handshake");
            File file = new File(sendPath, sendFileName);

            byte[] buffer = new byte[4096];
            int bytesRead;
            OutputStream os = socket.getOutputStream();
//            PrintWriter pw = new PrintWriter(os);

//            InputStream is = socket.getInputStream();
//            InputStreamReader isr = new InputStreamReader(is);
//            BufferedReader br = new BufferedReader(isr);
//            signalActivity("About to start handshake");

            //Handshake complete, start file transfer
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            // long BytesToSend = fileToSend.length();

            while (true) {
                bytesRead = bis.read(buffer, 0, buffer.length);

                if (bytesRead == -1) {
                    break;
                }

                //BytesToSend = BytesToSend - bytesRead;
                os.write(buffer, 0, bytesRead);
                os.flush();
            }

            bis.close();
            fis.close();
//            signalActivity("File Transfer Complete, sent file: " + fileToSend.getName());
//            os.close();
//            signalActivity("File Transfer Complete, sent file: " + fileToSend.getName());

        } catch (Exception e) {
//            signalActivity(e.getMessage());
        }
    }

    public void sendFileBack(Socket socket) {
        try {
//            signalActivity("About to start handshake");
            File file = new File(recvPath, recvFileName);

            byte[] buffer = new byte[4096];
            int bytesRead;
            OutputStream os = socket.getOutputStream();

            //Handshake complete, start file transfer
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);

            while (true) {
                bytesRead = bis.read(buffer, 0, buffer.length);

                if (bytesRead == -1) {
                    break;
                }

                os.write(buffer, 0, bytesRead);
                os.flush();
            }

            bis.close();
            fis.close();
//            signalActivity("File Transfer Complete, sent file: " + fileToSend.getName());
//            os.close();
        } catch (Exception e) {
//            signalActivity(e.getMessage());
        }
    }
}

