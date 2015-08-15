package edu.pdx.cs410.wifi.direct.file.transfer.slave;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.R;
import edu.pdx.cs410.wifi.direct.file.transfer.WiFiDirect;

/**
 * Created by User on 8/3/2015.
 */
public class SlaveActivity extends Activity {
    private WiFiDirect wifiDirect;
    private Intent slaveServiceIntent;
    private boolean slaveThreadActive;
    public InetAddress masterIp;
    public InetAddress slaveIp;
    public final int nrsPort = 8010;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slave);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        initDir();

        wifiDirect = new WiFiDirect(this);
        wifiDirect.search();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            /* Unregister broadcast receiver */
//            if (wifiServerReceiver != null) {
//                unregisterReceiver(wifiServerReceiver);
//            }

            /* remove wifi direct group */
//            if (wifiManager != null) {
//                wifiManager.removeGroup(wifichannel, null);
//            }
            wifiDirect.close();

            /* stop master service */
            if (slaveServiceIntent != null) {
                stopService(slaveServiceIntent);
            }
        } catch (IllegalArgumentException e) {
            /* This will happen if the server was never running and the stop
               button was pressed.
               Do nothing in this case. */
        }
    }

    public void setSlaveWiFiStatus(String message) {
        TextView server_wifi_status_text = (TextView) findViewById(R.id.tvSlaveWiFiStatus);
        server_wifi_status_text.setText(message);
    }

    public void setSlaveStatus(String message) {
        TextView server_status_text = (TextView) findViewById(R.id.tvSlaveStatus);
        server_status_text.setText(message);
    }

    public void setSlaveExceptionStatus(String message) {
        TextView server_status_text = (TextView) findViewById(R.id.tvSlaveExceptionStatus);
        server_status_text.setText(message);
    }

    public void initDir() {
        String path = "/";
        File downloadTarget = new File(path);

        /*initialize storage directory*/
        try {
            String recvPath;
            String sendPath;
            String recvFileName;
            String sendFileName;
            sendPath = recvPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            sendPath += "/A-NRS-Send";
            recvPath += "/A-NRS-Recv";
            sendFileName = "ToBeSent";
            recvFileName = "Recved";
            File dir = new File(recvPath);
            dir.mkdirs();
            dir = new File(sendPath);
            dir.mkdirs();
            /*write something to the file*/
            File file = new File(sendPath, sendFileName);
            FileOutputStream fos = new FileOutputStream(file);
            String content = "Just for test, here is all the content...";
            fos.write(content.getBytes());
            fos.close();
            downloadTarget = new File(recvPath + "/" + recvFileName);
        } catch (Exception e) {
            setSlaveExceptionStatus(e.getMessage());
        }
    }

    public void onListen(View view) {
        //If server is already listening on port or transfering data, do not attempt to start server service
        if (!slaveThreadActive) {
            //Create new thread, open socket, wait for connection, and transfer file
            slaveServiceIntent = new Intent(this, SlaveService.class);
//	    	serverServiceIntent.putExtra("saveLocation", downloadTarget);
            slaveServiceIntent.putExtra("port", nrsPort);
            slaveServiceIntent.putExtra("masterIp", masterIp);
            slaveServiceIntent.putExtra("slaveIp", slaveIp);
            slaveServiceIntent.putExtra("slaveResult", new ResultReceiver(null) {
                @Override
                protected void onReceiveResult(int resultCode, final Bundle resultData) {
                    if (resultCode == 1) {
                        if (resultData != null) {
                            final TextView server_file_status_text = (TextView) findViewById(R.id.tvSlaveStatus);

                            server_file_status_text.post(new Runnable() {
                                public void run() {
                                    server_file_status_text.setText((String) resultData.get("message"));
                                }
                            });
                        }
                    } else if (resultCode == 2) {
                        if (resultData != null) {
                            final TextView client_filename_text = (TextView) findViewById(R.id.tvSlaveExtraStatus);

                            client_filename_text.post(new Runnable() {
                                public void run() {
                                    client_filename_text.setText((String) resultData.get("progress"));
                                }
                            });
                        }
                    } else if (resultCode == 0) {
                        if (resultData == null) {
                            //Server service has shut down. Download may or may not have completed properly.
                            slaveThreadActive = false;

                            final TextView server_status_text = (TextView) findViewById(R.id.tvSlaveStatus);
                            server_status_text.post(new Runnable() {
                                public void run() {
                                    server_status_text.setText(R.string.server_stopped);
                                }
                            });
                        }
                    }else if (resultCode == 3) {
                        if (resultData != null) {
                            final ListView lvStat = (ListView) findViewById(R.id.lvSlaveStat);
                            lvStat.post(new Runnable() {
                                public void run() {
                                    String[] nameStat = (String[]) resultData.get("nameStat");
                                    float[] proStat = (float[]) resultData.get("proStat");
                                    long[] bwStat = (long[]) resultData.get("bwStat");
                                    long[] avgBwStat = (long[]) resultData.get("avgBwStat");
                                    long[] alBytesStat = (long[]) resultData.get("alBytesStat");
                                    setThreadStaus(nameStat, proStat, bwStat, avgBwStat, alBytesStat);
                                }
                            });
                        }
                    } else if (resultCode == 4) {
                        if (resultData != null) {
                            final TextView tvMasterException = (TextView) findViewById(R.id.tvSlaveExceptionStatus);
                            tvMasterException.post(new Runnable() {
                                public void run() {
                                    tvMasterException.setText((String) resultData.get("exception"));
                                }
                            });
                        }
                    }
                }
            });

            slaveThreadActive = true;
            startService(slaveServiceIntent);
        } else {
            //Set status to already running
            TextView serverServiceStatus = (TextView) findViewById(R.id.server_status_text);
            serverServiceStatus.setText("The server is already running");
        }
    }

    public void setThreadStaus(String[] nameStat, float[] proStat, long[] bwStat, long[] avgBwStat,  long[] alBytesStat) {
        ListView lvStat = (ListView) findViewById(R.id.lvSlaveStat);
        int thdNum = proStat.length;
        String[] strStat = new String[thdNum];
        for (int i = 0; i < thdNum; i++) {
            strStat[i] = "Name:" + nameStat[i] + "\t\t|\t\t"
                    + "Progress:" + Float.toString(proStat[i]) + "%\n"
                    + "BW:" + Long.toString(bwStat[i]) + "KB/s\t\t|\t\t"
                    + "AvgBW:" + Long.toString(avgBwStat[i]) + "KB/s\n"
                    + "AlreadyBytes:" + Long.toString(alBytesStat[i]) + "B";
        }

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.info_list_item, strStat);
        lvStat.setAdapter(arrayAdapter);
    }
}