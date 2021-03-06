package edu.pdx.cs410.wifi.direct.file.transfer.trans;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import edu.pdx.cs410.wifi.direct.file.transfer.BackendService;
import edu.pdx.cs410.wifi.direct.file.transfer.Statistic;
import edu.pdx.cs410.wifi.direct.file.transfer.ThreadStatistics;
import edu.pdx.cs410.wifi.direct.file.transfer.master.MasterService;
import edu.pdx.cs410.wifi.direct.file.transfer.slave.SlaveService;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;

/**
 * Created by User on 6/15/2015.
 */

public class HttpDownload {
    private DownloadTask task;

    static public void download(String strUrl, File recvFile) throws Exception {
        OutputStream output = null;
        InputStream input = null;
        URL url = new URL(strUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        input = conn.getInputStream();
        output = new FileOutputStream(recvFile);

        byte[] buffer = new byte[4 * 1024];
        int bytesRead = 0;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0 , bytesRead);
        }
        output.flush();
        input.close();
        output.close();
    }

//    static public int download(String strUrl, File recvFile, BackendService slaveService) throws Exception {
//        BwMetric bwMetric = new BwMetric(slaveService);
//        OutputStream output = null;
//        InputStream input = null;
//        URL url = new URL(strUrl);
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//
//        input = conn.getInputStream();
//        output = new FileOutputStream(recvFile);
//
//        byte[] buffer = new byte[4 * 1024];
//        int bytesRead = 0;
//        while ((bytesRead = input.read(buffer)) != -1) {
//            bwMetric.bwMetric(bytesRead);
//            output.write(buffer);
//        }
//        output.flush();
//        input.close();
//        output.close();
//
//        return bwMetric.bw;
//    }

    static public long download(String strUrl, File recvFile,
                                BackendService masterService, ThreadStatistics stat) throws Exception {
        OutputStream output = null;
        InputStream input = null;
        URL url = new URL(strUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        long totalLen = conn.getContentLength();
//        BwMetric bwMetric = new BwMetric(masterService, totalLen);

        input = conn.getInputStream();
        output = new FileOutputStream(recvFile);

        stat.startMetric(totalLen);
        byte[] buffer = new byte[4 * 1024];
        int bytesRead = 0;
        while ((bytesRead = input.read(buffer)) != -1) {
//            bwMetric.bwMetric(bytesRead, false);
            /* Statistics */
            stat.updateBytes((long) bytesRead);
            output.write(buffer, 0 , bytesRead);
        }
        output.flush();
        input.close();
        output.close();

//        return bwMetric.bw;
        return stat.bw;
    }

    static public long download(String strUrl, RandomAccessFile recvFile,
                                BackendService masterService, ThreadStatistics stat) throws Exception {
        InputStream input = null;
        URL url = new URL(strUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        long totalLen = conn.getContentLength();
//        BwMetric bwMetric = new BwMetric(masterService,totalLen);

        input = conn.getInputStream();

        stat.startMetric(totalLen);
        byte[] buffer = new byte[4 * 1024];
        int bytesRead = 0;
        while ((bytesRead = input.read(buffer)) != -1) {
//            bwMetric.bwMetric(bytesRead, false);
            int i = 0;
            /* Statistics */
            stat.updateBytes((long) bytesRead);
            recvFile.write(buffer, 0, bytesRead);
        }
        input.close();

//        return bwMetric.bw;
        return stat.bw;
    }

//    static public int download(String strUrl, RandomAccessFile recvFile, BackendService slaveService) throws Exception {
//        BwMetric bwMetric = new BwMetric(slaveService);
////        OutputStream output = null;
//        InputStream input = null;
//        URL url = new URL(strUrl);
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//
//        input = conn.getInputStream();
////        output = new FileOutputStream(recvFile);
//
//        byte[] buffer = new byte[4 * 1024];
//        int bytesRead = 0;
//        while ((bytesRead = input.read(buffer)) != -1) {
//            bwMetric.bwMetric(bytesRead);
//            recvFile.write(buffer, 0, bytesRead);
////            output.write(buffer);
//        }
////        output.flush();
//        input.close();
////        output.close();
//
//        return bwMetric.bw;
//    }

//    static public void partialDownload(String strUrl, File recvFile, DownloadTask task) throws Exception {
//        OutputStream output = null;
//        InputStream input = null;
//        URL url = new URL(strUrl);
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setRequestProperty("Range", "bytes=" + Long.toString(task.start) +
//                "-" + Long.toString(task.end));
//        input = conn.getInputStream();
//        output = new FileOutputStream(recvFile);
//
//        byte[] buffer = new byte[4 * 1024];
//        int bytesRead = 0;
//        while ((bytesRead = input.read(buffer)) != -1) {
//            output.write(buffer, 0, bytesRead);
//        }
//        output.flush();
//        input.close();
//        output.close();
//    }

    static public long partialDownload(String strUrl, File recvFile, DownloadTask task,
                                       BackendService masterService, ThreadStatistics stat) throws Exception {
//        BwMetric bwMetric = new BwMetric(masterService, task.end - task.start + 1);
        OutputStream output = null;
        InputStream input = null;
        int totalLen = 0;
        URL url = new URL(strUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Range", "bytes=" + Long.toString(task.start) +
                                "-" + Long.toString(task.end));
        input = conn.getInputStream();
        output = new FileOutputStream(recvFile);

        stat.startMetric(task.end - task.start + 1);
        int bytesRead = 0;
        byte[] buffer = new byte[4 * 1024];
        while ((bytesRead = input.read(buffer)) != -1) {
            totalLen += bytesRead;
//            bwMetric.bwMetric(bytesRead, false);
            /* Statistics */
            stat.updateBytes((long) bytesRead);
            output.write(buffer, 0, bytesRead);
        }
        output.flush();
        input.close();
        output.close();

//        return bwMetric.bw;
        return stat.bw;
    }

    static public long partialDownload(String strUrl, RandomAccessFile recvFile, DownloadTask task,
                                       BackendService masterService, ThreadStatistics stat) throws Exception {
//        BwMetric bwMetric = new BwMetric(masterService, task.end - task.start + 1);
        InputStream input = null;
        URL url = new URL(strUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Range", "bytes=" + Long.toString(task.start) +
                "-" + Long.toString(task.end));
        input = conn.getInputStream();

        stat.startMetric(task.end - task.start + 1);
        int bytesRead = 0;
        byte[] buffer = new byte[4 * 1024];
        while ((bytesRead = input.read(buffer)) != -1) {
//            bwMetric.bwMetric(bytesRead, false);
            /* Statistics */
            stat.updateBytes((long) bytesRead);
            recvFile.write(buffer, 0, bytesRead);
        }
        input.close();

//        return bwMetric.bw;
        return stat.bw;
    }

//    static public int partialDownload(String strUrl, File recvFile, DownloadTask task, BackendService slaveService) throws Exception {
//        BwMetric bwMetric = new BwMetric(slaveService);
//        OutputStream output = null;
//        InputStream input = null;
//        URL url = new URL(strUrl);
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setRequestProperty("Range", "bytes=" + Integer.toString(task.start) +
//                "-" + Integer.toString(task.end));
//        input = conn.getInputStream();
//        output = new FileOutputStream(recvFile);
//
//        int bytesRead = 0;
//        byte[] buffer = new byte[4 * 1024];
//        while ((bytesRead = input.read(buffer)) != -1) {
//            bwMetric.bwMetric(bytesRead);
//            output.write(buffer);
//        }
//        output.flush();
//        input.close();
//        output.close();
//
//        return bwMetric.bw;
//    }

//    static public int partialDownload(String strUrl, RandomAccessFile recvFile, DownloadTask task, BackendService slaveService) throws Exception {
//        BwMetric bwMetric = new BwMetric(slaveService);
////        OutputStream output = null;
//        InputStream input = null;
//        URL url = new URL(strUrl);
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setRequestProperty("Range", "bytes=" + Integer.toString(task.start) +
//                "-" + Integer.toString(task.end));
//        input = conn.getInputStream();
////        output = new FileOutputStream(recvFile);
//
//        int bytesRead = 0;
//        byte[] buffer = new byte[4 * 1024];
//        while ((bytesRead = input.read(buffer)) != -1) {
//            bwMetric.bwMetric(bytesRead);
//            recvFile.write(buffer, 0, bytesRead);
////            output.write(buffer);
//        }
////        output.flush();
//        input.close();
////        output.close();
//
//        return bwMetric.bw;
//    }
}