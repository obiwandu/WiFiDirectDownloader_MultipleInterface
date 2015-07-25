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
    public int dataLen;
    public int urlLen;
    public int start;
    public int end;
    public byte[] header;

    public ProtocolHeader encapPro(DownloadTask task, int type) {
        ByteBuffer bb = ByteBuffer.allocate(4 * Integer.SIZE / Byte.SIZE);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(type);
        bb.putInt(task.url.length());
        bb.putInt(task.start);
        bb.putInt(task.end);
        header = bb.array();

        return this;
    }

    public ProtocolHeader decapPro(byte[] head) {
        ByteBuffer bb = ByteBuffer.wrap(head, 0, 4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        type = bb.getInt();
        bb = ByteBuffer.wrap(head, 4, 4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        if (type == 1111) {
            dataLen = bb.getInt();
        } else {
            urlLen = bb.getInt();
        }
        bb = ByteBuffer.wrap(head, 8, 4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        start = bb.getInt();
        bb = ByteBuffer.wrap(head, 12, 4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        end = bb.getInt();

        return this;
    }
}