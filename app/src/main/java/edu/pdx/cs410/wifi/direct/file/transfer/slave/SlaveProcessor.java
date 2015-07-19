package edu.pdx.cs410.wifi.direct.file.transfer.slave;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;

/**
 * Created by User on 7/12/2015.
 */
public class SlaveProcessor {
    static private DownloadTask parseRequest(String request) {
        int start = 0;
        int end = 0;
        boolean isPartial = false;
        String url = "";
        String[] commands = request.split("\n");
        for (String cmd : commands) {
            String[] kv = cmd.split(":");
            if (kv[0].equals("taskstart")) {
                start = Integer.parseInt(kv[1]);
            } else if (kv[0].equals("taskend")) {
                end = Integer.parseInt(kv[1]);
            } else if (kv[0].equals("partial")) {
                isPartial = Boolean.parseBoolean(kv[1]);
            } else if (kv[0].equals("url")) {
                url = kv[1];
            }
        }

        DownloadTask task = new DownloadTask(start, end, isPartial, url);
        return task;
    }

    static public void processRequest(byte[] request, InetSocketAddress[] sockAddr) {
//        String req = request.toString();
        String req = new String(request);
        DownloadTask task = parseRequest(req);

        SlaveInvoker.downLoad(task, sockAddr);
    }
}
