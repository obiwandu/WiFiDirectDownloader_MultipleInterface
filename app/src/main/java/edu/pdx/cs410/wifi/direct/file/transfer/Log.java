package edu.pdx.cs410.wifi.direct.file.transfer;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * Created by User on 8/2/2015.
 */
public class Log {
    private File logFile;
    public Log(String logName){
        /* set recv file path */
        String logFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/A-NRS-Log";
        File dir = new File(logFilePath);
        dir.mkdirs();
        String logFileName = logName;
        logFile = new File(logFilePath, logFileName);
        if (logFile.exists()){
            logFile.delete();
        }
    }

    public void record(String info) throws Exception {
        long currentTime = System.currentTimeMillis();
        String record = "Time:" + Long.toString(currentTime) + "|" + info + "\n";
        FileWriter fw = new FileWriter(logFile, true);
        fw.write(record);
        fw.close();

//        FileOutputStream fos = new FileOutputStream(logFile);
//        fos.write(record.getBytes());
//        fos.close();
        System.out.print(("Time:" + Long.toString(currentTime) + "|" + info + "\n").getBytes());
    }
}
