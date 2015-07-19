package edu.pdx.cs410.wifi.direct.file.transfer.slave;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.File;
import java.net.InetAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.HttpDownload;
import edu.pdx.cs410.wifi.direct.file.transfer.oldClass.RecvThread;
import edu.pdx.cs410.wifi.direct.file.transfer.oldClass.SendThread;

/**
 * Created by User on 7/5/2015.
 */
public class SlaveThread extends Thread {
    public int transPort;
    public String masterIp;
    public SlaveThread(String mIp, int port){
        masterIp = mIp;
        transPort = port;
    }

    @Override
    public void run(){
        Handler taskHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                    /*master has done its task*/
//                        masterDone = true;
                    case 2:
                    /*slave has done its task*/
//                        slaveDone = true;
                    default:
                        super.handleMessage(msg);
                }
            }
        };

        while (true) {
            byte[] buf = new byte[256];
            Thread recvThread = new Thread(new RecvThread(transPort, taskHandler, buf));
            recvThread.start();

            String url = "www.test.com";
            DownloadTask task = new DownloadTask(1001, 2001, true, url);

//            HttpDownload httpDl = new HttpDownload(url, task);
            File recvFile = new File("");
            HttpDownload.download(url, recvFile);
            File file = new File("temp");

            try {
                Thread sendThread = new Thread(new SendThread(transPort, InetAddress.getByName(masterIp), taskHandler, file));
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
