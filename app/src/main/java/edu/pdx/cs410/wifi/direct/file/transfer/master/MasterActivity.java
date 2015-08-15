package edu.pdx.cs410.wifi.direct.file.transfer.master;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.util.ArrayList;

import edu.pdx.cs410.wifi.direct.file.transfer.R;
import edu.pdx.cs410.wifi.direct.file.transfer.Statistic;
import edu.pdx.cs410.wifi.direct.file.transfer.WiFiDirect;

/**
 * Created by User on 8/3/2015.
 */
public class MasterActivity extends Activity {
    private BroadcastReceiver wifiMasterReceiver;
    public InetAddress masterIp;
    public InetAddress slaveIp;
    public ArrayList<InetAddress> slaveList = new ArrayList<InetAddress>();
    public final int nrsPort = 8010;
    private WiFiDirect wifiDirect;
    private Intent masterServiceIntent;
    private boolean transferActive = false;
    private boolean connectedAndReadyToSendFile;
    private String url = "http://d3g.qq.com/sngapp/app/update/20150616190155_8476/qzone_5.5.1.192_android_r98080_20150616175052_release_QZGW_D.apk";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        initDir();

        EditText etURL = (EditText) findViewById(R.id.etURL);
        etURL.setText(url);

        wifiDirect = new WiFiDirect(this);
        wifiDirect.search();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            /* Unregister broadcast receiver */
//            if (wifiClientReceiver != null) {
//                unregisterReceiver(wifiClientReceiver);
//            }

            /* remove wifi direct group */
//            if (wifiManager != null) {
//                wifiManager.removeGroup(wifichannel, null);
//            }
            wifiDirect.close();

            /* stop master service */
            if (masterServiceIntent != null) {
                stopService(masterServiceIntent);
            }
        } catch (IllegalArgumentException e) {
            /* This will happen if the server was never running and the stop button was pressed.
               Do nothing in this case. */
        }
    }

    private void initDir() {
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
            File fileToSend = new File(sendPath + "/" + sendFileName);
            File fileToRecv = new File(recvPath + "/" + recvFileName + "-part2");
            boolean filePathProvided = true;
        } catch (Exception e) {
            setMasterExceptionStatus(e.getMessage());
        }
    }

    public void onStart(View view) {
        //Only try to send file if there isn't already a transfer active
        if (!transferActive) {
//            if (!filePathProvided) {
//                setClientFileTransferStatus("Select a file to send before pressing send");
//            }
            if (!connectedAndReadyToSendFile) {
                setMasterExceptionStatus("You must be connected to a server before attempting to send a file");
//            } else if (wifiInfo == null) {
//                setMasterExceptionStatus("Missing Wifi P2P information");
            } else {
                /* Load settings */
                final EditText masterChunkSize = (EditText) findViewById(R.id.etChunkSize);
                final long chunkSize = Long.parseLong(masterChunkSize.getText().toString());
                final EditText masterMinChunkSize = (EditText) findViewById(R.id.etMinChunkSize);
                final long minChunkSize = Long.parseLong(masterMinChunkSize.getText().toString());
                EditText etURL = (EditText) findViewById(R.id.etURL);
                url = etURL.getText().toString();

//                clientServiceIntent = new Intent(this, MasterService.class);
                masterServiceIntent = new Intent(this, MultithreadMasterService.class);
                masterServiceIntent.putExtra("url", url);
                masterServiceIntent.putExtra("port", nrsPort);
                masterServiceIntent.putExtra("masterIp", masterIp);
                masterServiceIntent.putExtra("slaveIp", slaveIp);
                masterServiceIntent.putExtra("slaveList", slaveList);
                masterServiceIntent.putExtra("chunkSize", chunkSize * 1024);
                masterServiceIntent.putExtra("minChunkSize", minChunkSize * 1024);
                masterServiceIntent.putExtra("masterResult", new ResultReceiver(null) {
                    @Override
                    protected void onReceiveResult(final int resultCode, final Bundle resultData) {
                        if (resultCode == 1) {
                            if (resultData != null) {
                                final TextView client_status_text = (TextView) findViewById(R.id.tvMasterStatus);

                                client_status_text.post(new Runnable() {
                                    public void run() {
                                        client_status_text.setText((String) resultData.get("message"));
                                    }
                                });
                            }
                        } else if (resultCode == 2) {
                            if (resultData != null) {
                                final TextView client_filename_text = (TextView) findViewById(R.id.tvMasterProgress);

                                client_filename_text.post(new Runnable() {
                                    public void run() {
                                        client_filename_text.setText((String) resultData.get("progress"));
                                    }
                                });
                            }
                        } else if (resultCode == 0) {
                            if (resultData == null) {
                                //Client service has shut down, the transfer may or may not have been successful. Refer to message
                                transferActive = false;
                            }
                        } else if (resultCode == 3) {
                            if (resultData != null) {
                                final ListView lvStat = (ListView) findViewById(R.id.lvStat);
                                lvStat.post(new Runnable() {
                                    public void run() {
                                        String[] nameStat = (String[]) resultData.get("nameStat");
                                        float[] proStat = (float[]) resultData.get("proStat");
                                        long[] bwStat = (long[]) resultData.get("bwStat");
                                        long[] avgBwStat = (long[]) resultData.get("avgBwStat");
                                        long[] alBytesStat = (long[]) resultData.get("alBytesStat");
                                        setThreadStaus(nameStat, proStat, bwStat, avgBwStat, alBytesStat);

                                        setMasterProgress("Total time:" + (String) resultData.get("totalTime") + "s"
                                                + "\nTotal progress:" + (String) resultData.get("totalPro") + "%"
                                                + "\nTotal avg BW:" + (String) resultData.get("totalAvgBw") + "KB/s");
                                    }
                                });
                            }
                        } else if (resultCode == 4) {
                            if (resultData != null) {
                                final TextView tvMasterException = (TextView) findViewById(R.id.tvMasterExceptionStatus);
                                tvMasterException.post(new Runnable() {
                                    public void run() {
                                        tvMasterException.setText((String) resultData.get("exception"));
                                    }
                                });
                            }
                        }
                    }
                });

                transferActive = true;
                startService(masterServiceIntent);
            }
        }
    }

    public void onLocalDownload(View view) {
        final EditText masterChunkSize = (EditText) findViewById(R.id.etChunkSize);
        String temp = masterChunkSize.getText().toString();
        final long chunkSize = Long.parseLong(masterChunkSize.getText().toString());
        final EditText masterMinChunkSize = (EditText) findViewById(R.id.etMinChunkSize);
        final long minChunkSize = Long.parseLong(masterMinChunkSize.getText().toString());
        EditText etURL = (EditText) findViewById(R.id.etURL);
        url = etURL.getText().toString();

        masterServiceIntent = new Intent(this, MasterService.class);
        masterServiceIntent.putExtra("url", url);
        masterServiceIntent.putExtra("port", nrsPort);
        masterServiceIntent.putExtra("chunkSize", chunkSize * 1024);
        masterServiceIntent.putExtra("minChunkSize", minChunkSize * 1024);
        masterServiceIntent.putExtra("masterResult", new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, final Bundle resultData) {
                if (resultCode == 1) {
                    if (resultData != null) {
                        final TextView client_status_text = (TextView) findViewById(R.id.tvMasterStatus);

                        client_status_text.post(new Runnable() {
                            public void run() {
                                client_status_text.setText((String) resultData.get("message"));
                            }
                        });
                    }
                } else if (resultCode == 2) {
                    if (resultData != null) {
                        final TextView client_filename_text = (TextView) findViewById(R.id.tvMasterProgress);

                        client_filename_text.post(new Runnable() {
                            public void run() {
                                client_filename_text.setText((String) resultData.get("progress"));
                            }
                        });
                    }
                } else if (resultCode == 0) {
                    //Client service has shut down, the transfer may or may not have been successful. Refer to message
//                        transferActive = false;
                } else if (resultCode == 3) {
                    if (resultData != null) {
                        final ListView lvStat = (ListView) findViewById(R.id.lvStat);
                        lvStat.post(new Runnable() {
                            public void run() {
                                String[] nameStat = (String[]) resultData.get("nameStat");
                                float[] proStat = (float[]) resultData.get("proStat");
                                long[] bwStat = (long[]) resultData.get("bwStat");
                                long[] avgBwStat = (long[]) resultData.get("avgBwStat");
                                long[] alBytesStat = (long[]) resultData.get("alBytesStat");
                                setThreadStaus(nameStat, proStat, bwStat, avgBwStat, alBytesStat);

                                setMasterProgress("Total time:" + (String) resultData.get("totalTime") + "s"
                                        + "\nTotal progress:" + (String) resultData.get("totalPro") + "%"
                                        + "\nTotal avg BW:" + (String) resultData.get("totalAvgBw") + "KB/s");
                            }
                        });
                    }
                } else if (resultCode == 4) {
                    if (resultData != null) {
                        final TextView tvMasterException = (TextView) findViewById(R.id.tvMasterExceptionStatus);
                        tvMasterException.post(new Runnable() {
                            public void run() {
                                tvMasterException.setText((String) resultData.get("exception"));
                            }
                        });
                    }
                }
            }
        });

        startService(masterServiceIntent);
    }

    public void setTransferStatus(boolean status) {
        connectedAndReadyToSendFile = status;
    }

    public void setMasterWifiStatus(String message) {
        TextView connectionStatusText = (TextView) findViewById(R.id.tvMasterWifiStatus);
        connectionStatusText.setText(message);
    }

    public void setMasterStatus(String message) {
        TextView clientStatusText = (TextView) findViewById(R.id.tvMasterStatus);
        clientStatusText.setText(message);
    }

    public void setMasterExceptionStatus(String message) {
        TextView fileTransferStatusText = (TextView) findViewById(R.id.tvMasterExceptionStatus);
        fileTransferStatusText.setText(message);
    }

    public void setMasterProgress(String message) {
        TextView targetFileStatus = (TextView) findViewById(R.id.tvMasterProgress);
        targetFileStatus.setText(message);
    }

    public void setThreadStaus(String[] nameStat, float[] proStat, long[] bwStat, long[] avgBwStat,  long[] alBytesStat) {
        ListView lvStat = (ListView) findViewById(R.id.lvStat);
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

    public void displayPeers(final WifiP2pDeviceList peers) {
        //Dialog to show errors/status
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("WiFi Direct File Transfer");

        //Get list view
        ListView peerView = (ListView) findViewById(R.id.peers_listview);
        //Make array list
        ArrayList<String> peersStringArrayList = new ArrayList<String>();
        //Fill array list with strings of peer names
        for (WifiP2pDevice wd : peers.getDeviceList()) {
            peersStringArrayList.add(wd.deviceName);
        }

        //Set list view as clickable
        peerView.setClickable(true);
        //Make adapter to connect peer data to list view
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.device_list_item, peersStringArrayList.toArray());
        //Show peer data in listview
        peerView.setAdapter(arrayAdapter);
        peerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
                //Get string from textview
                TextView tv = (TextView) view;
                WifiP2pDevice device = null;

                //Search all known peers for matching name
                for (WifiP2pDevice wd : peers.getDeviceList()) {
                    if (wd.deviceName.equals(tv.getText()))
                        device = wd;
                }

                if (device != null) {
                    /* Connect to selected peer */
//                    connectToPeer(device);
                    wifiDirect.connect(device);
                } else {
                    dialog.setMessage("Failed");
                    dialog.show();
                }
            }
            // TODO Auto-generated method stub
        });
    }
}
