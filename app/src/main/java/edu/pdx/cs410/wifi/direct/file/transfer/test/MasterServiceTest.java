package edu.pdx.cs410.wifi.direct.file.transfer.test;

import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.test.InstrumentationTestCase;
import android.widget.TextView;

import java.net.InetAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.R;
import edu.pdx.cs410.wifi.direct.file.transfer.master.MultithreadMasterService;

/**
 * Created by User on 7/24/2015.
 */
public class MasterServiceTest extends InstrumentationTestCase{
    public void testTC1() throws Exception {
        Intent clientServiceIntent;
        String url = "https://notepad-plus-plus.org/repository/6.x/6.7.9.2/npp.6.7.9.2.Installer.exe";
        final int nrsPort = 8010;
        boolean transferActive;
        InetAddress masterIp = InetAddress.getByName("192.168.0.1");
        InetAddress slaveIp = InetAddress.getByName("192.168.0.234");
        clientServiceIntent = new Intent(null, MultithreadMasterService.class);
        clientServiceIntent.putExtra("url", url);
        clientServiceIntent.putExtra("port", nrsPort);
        clientServiceIntent.putExtra("masterIp", masterIp);
        clientServiceIntent.putExtra("slaveIp", slaveIp);
        clientServiceIntent.putExtra("masterResult", new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, final Bundle resultData) {
                if (resultCode == nrsPort) {
                    if (resultData == null) {
                        //Client service has shut down, the transfer may or may not have been successful. Refer to message
//                        transferActive = false;
                    } else {
                        System.out.print((String) resultData.get("message"));
//                        final TextView client_status_text = (TextView) findViewById(R.id.file_transfer_status);
//                        client_status_text.post(new Runnable() {
//                            public void run() {
//                                client_status_text.setText((String) resultData.get("message"));
//                            }
//                        });
                    }
                } else if (resultCode == 1) {
                    if (resultData != null) {
                        System.out.print((String) resultData.get("message"));
//                        final TextView client_filename_text = (TextView) findViewById(R.id.selected_filename);
//                        client_filename_text.post(new Runnable() {
//                            public void run() {
//                                client_filename_text.setText((String) resultData.get("progress"));
//                            }
//                        });
                    }
                }
            }
        });

        transferActive = true;
//        startService(clientServiceIntent);
    }
}
