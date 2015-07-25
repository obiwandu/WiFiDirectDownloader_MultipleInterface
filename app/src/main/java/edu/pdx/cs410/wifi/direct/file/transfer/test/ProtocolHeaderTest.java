package edu.pdx.cs410.wifi.direct.file.transfer.test;

import android.test.InstrumentationTestCase;

import edu.pdx.cs410.wifi.direct.file.transfer.ProtocolHeader;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.DownloadTask;

/**
 * Created by User on 7/24/2015.
 */
public class ProtocolHeaderTest extends InstrumentationTestCase{
    public void testTC1() {
        DownloadTask task = new DownloadTask(100, 1000, 10000, "sfsdfds");
        ProtocolHeader header = new ProtocolHeader();
        header.encapPro(task, 10);
        header.decapPro(header.header);
    }
}
