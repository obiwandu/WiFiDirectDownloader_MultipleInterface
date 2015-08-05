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
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.util.ArrayList;

import edu.pdx.cs410.wifi.direct.file.transfer.R;
import edu.pdx.cs410.wifi.direct.file.transfer.WiFiDirect;

/**
 * Created by User on 8/3/2015.
 */
public class MasterActivity extends Activity {
    private BroadcastReceiver wifiMasterReceiver;
    public InetAddress masterIp;
    public InetAddress slaveIp;
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
//                clientServiceIntent = new Intent(this, MasterService.class);
                masterServiceIntent = new Intent(this, MultithreadMasterService.class);
                masterServiceIntent.putExtra("url", url);
                masterServiceIntent.putExtra("port", nrsPort);
                masterServiceIntent.putExtra("masterIp", masterIp);
                masterServiceIntent.putExtra("slaveIp", slaveIp);
                masterServiceIntent.putExtra("masterResult", new ResultReceiver(null) {
                    @Override
                    protected void onReceiveResult(int resultCode, final Bundle resultData) {
                        if (resultCode == 1) {
                            if (resultData != null) {
                                final TextView client_status_text = (TextView) findViewById(R.id.tvMasterExceptionStatus);

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
                        }
                    }
                });

                transferActive = true;
                startService(masterServiceIntent);
            }
        }
    }

    public void onLocalDownload(View view) {
        masterServiceIntent = new Intent(this, MasterService.class);
        masterServiceIntent.putExtra("url", url);
        masterServiceIntent.putExtra("port", nrsPort);
        masterServiceIntent.putExtra("masterResult", new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, final Bundle resultData) {
                if (resultCode == 1) {
                    if (resultData != null) {
                        final TextView client_status_text = (TextView) findViewById(R.id.tvMasterExceptionStatus);

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

    public void setTargetFileStatus(String message) {
        TextView targetFileStatus = (TextView) findViewById(R.id.tvMasterProgress);
        targetFileStatus.setText(message);
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
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, peersStringArrayList.toArray());
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
