package edu.pdx.cs410.wifi.direct.file.transfer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;

/**
 * Created by User on 7/23/2015.
 */
public class ProtocolHeader {
    public int type;
    public long dataLen;
    public long bw;
    public long urlLen;
    public long start;
    public long end;
    public byte[] header;
    public boolean stop;
    public static final int HEADER_LEN = (Integer.SIZE + 3 * Long.SIZE) / Byte.SIZE;

    public ProtocolHeader (int t) {
        stop = false;
        type = t;    /* specified type, now only support 3333 which means stop slave */
        ByteBuffer bb = ByteBuffer.allocate(HEADER_LEN);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(type);
        bb.putLong(0);
        bb.putLong(0);
        bb.putLong(0);
        header = bb.array();
    }

    public ProtocolHeader (DownloadTask task) {
        stop = false;
        type = 2222;    /* 2222 means task package, including type, url len, start & end */
        ByteBuffer bb = ByteBuffer.allocate(HEADER_LEN);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(type);
        bb.putLong(task.url.length());
//        if (type == 1111) {
//            /* 1111 means data package */
//            bb.putInt(task.end - task.start + 1);
//        } else {
//            bb.putInt(task.url.length());
//        }
        bb.putLong(task.start);
        bb.putLong(task.end);
        header = bb.array();

//        return this;
    }

    public ProtocolHeader (DownloadTask task, long bw) {
        stop = false;
        type = 1111;    /* 1111 means data package, including type, bw, start & end */
        ByteBuffer bb = ByteBuffer.allocate(HEADER_LEN);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(type);
        bb.putLong(bw);
//        if (type == 1111) {
//            bb.putInt(task.end - task.start + 1);
//        } else {
//            bb.putInt(task.url.length());
//        }
        bb.putLong(task.start);
        bb.putLong(task.end);
        header = bb.array();

//        return this;
    }

    public ProtocolHeader (byte[] head) {
        stop = false;
        ByteBuffer bb = ByteBuffer.wrap(head, 0, 4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        type = bb.getInt();
        bb = ByteBuffer.wrap(head, 4, 8);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        if (type == 1111) {
            /* data package */
//            dataLen = bb.getInt();
            bw = bb.getLong();
        } else if (type == 2222){
            /* task package */
            urlLen = bb.getLong();
        } else {
            stop = true;
        }
        bb = ByteBuffer.wrap(head, 12, 8);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        start = bb.getLong();
        bb = ByteBuffer.wrap(head, 20, 8);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        end = bb.getLong();
        dataLen = end - start + 1;

//        return this;
    }
}