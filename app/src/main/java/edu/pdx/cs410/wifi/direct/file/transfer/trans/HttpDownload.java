package edu.pdx.cs410.wifi.direct.file.transfer.trans;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;

/**
 * Created by User on 6/15/2015.
 */

public class HttpDownload {
    private DownloadTask task;

    static public void download(String strUrl, File recvFile) {
        OutputStream output = null;
        InputStream input = null;
        try {
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

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                output.close();
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static public void partialDownload(String strUrl, File recvFile, DownloadTask task) {
        OutputStream output = null;
        InputStream input = null;
        try {
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

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                output.close();
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}