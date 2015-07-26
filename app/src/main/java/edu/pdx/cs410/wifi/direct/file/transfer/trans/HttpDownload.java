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
        while (input.read(buffer) != -1) {
            output.write(buffer);
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

    static public int download(String strUrl, File recvFile, BackendService masterService) throws Exception {
        BwMetric bwMetric = new BwMetric(masterService);
        OutputStream output = null;
        InputStream input = null;
        URL url = new URL(strUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        input = conn.getInputStream();
        output = new FileOutputStream(recvFile);

        byte[] buffer = new byte[4 * 1024];
        int bytesRead = 0;
        while ((bytesRead = input.read(buffer)) != -1) {
            bwMetric.bwMetric(bytesRead);
            output.write(buffer);
        }
        output.flush();
        input.close();
        output.close();

        return bwMetric.bw;
    }

    static public int download(String strUrl, RandomAccessFile recvFile, BackendService masterService) throws Exception {
        BwMetric bwMetric = new BwMetric(masterService);
//        OutputStream output = null;
        InputStream input = null;
        URL url = new URL(strUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        input = conn.getInputStream();
//        output = new FileOutputStream(recvFile);

        byte[] buffer = new byte[4 * 1024];
        int bytesRead = 0;
        while ((bytesRead = input.read(buffer)) != -1) {
            bwMetric.bwMetric(bytesRead);
            recvFile.write(buffer, 0, bytesRead);
//            output.write(buffer);
        }
//        output.flush();
        input.close();
//        output.close();

        return bwMetric.bw;
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

    static public void partialDownload(String strUrl, File recvFile, DownloadTask task) throws Exception {
        OutputStream output = null;
        InputStream input = null;
        URL url = new URL(strUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Range", "bytes=" + Integer.toString(task.start) +
                "-" + Integer.toString(task.end));
        input = conn.getInputStream();
        output = new FileOutputStream(recvFile);

        byte[] buffer = new byte[4 * 1024];
        while (input.read(buffer) != -1) {
            output.write(buffer);
        }
        output.flush();
        input.close();
        output.close();
    }

    static public int partialDownload(String strUrl, File recvFile, DownloadTask task, BackendService masterService) throws Exception {
        BwMetric bwMetric = new BwMetric(masterService);
        OutputStream output = null;
        InputStream input = null;
        URL url = new URL(strUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Range", "bytes=" + Integer.toString(task.start) +
                "-" + Integer.toString(task.end));
        input = conn.getInputStream();
        output = new FileOutputStream(recvFile);

        int bytesRead = 0;
        byte[] buffer = new byte[4 * 1024];
        while ((bytesRead = input.read(buffer)) != -1) {
            bwMetric.bwMetric(bytesRead);
            output.write(buffer);
        }
        output.flush();
        input.close();
        output.close();

        return bwMetric.bw;
    }

    static public int partialDownload(String strUrl, RandomAccessFile recvFile, DownloadTask task, BackendService masterService) throws Exception {
        BwMetric bwMetric = new BwMetric(masterService);
//        OutputStream output = null;
        InputStream input = null;
        URL url = new URL(strUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Range", "bytes=" + Integer.toString(task.start) +
                "-" + Integer.toString(task.end));
        input = conn.getInputStream();
//        output = new FileOutputStream(recvFile);

        int bytesRead = 0;
        byte[] buffer = new byte[4 * 1024];
        while ((bytesRead = input.read(buffer)) != -1) {
            bwMetric.bwMetric(bytesRead);
            recvFile.write(buffer, 0, bytesRead);
//            output.write(buffer);
        }
//        output.flush();
        input.close();
//        output.close();

        return bwMetric.bw;
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