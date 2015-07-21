/*
 WiFi Direct File Transfer is an open source application that will enable sharing 
 of data between Android devices running Android 4.0 or higher using a WiFi direct
 connection without the use of a separate WiFi access point.This will enable data 
 transfer between devices without relying on any existing network infrastructure. 
 This application is intended to provide a much higher speed alternative to Bluetooth
 file transfers. 

 Copyright (C) 2012  Teja R. Pitla
 Contact: teja.pitla@gmail.com

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package edu.pdx.cs410.wifi.direct.file.transfer.slave;

import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import edu.pdx.cs410.wifi.direct.file.transfer.FileBrowser;
import edu.pdx.cs410.wifi.direct.file.transfer.R;
import edu.pdx.cs410.wifi.direct.file.transfer.master.ClientActivity;

public class MainActivity extends Activity {
    public final int fileRequestID = 55;
    /*transfer port*/
//	public final int port = 7950;
    private WifiP2pManager wifiManager;
    private Channel wifichannel;
    private BroadcastReceiver wifiServerReceiver;
    private IntentFilter wifiServerReceiverIntentFilter;
    private String path;
    private File downloadTarget;
    private Intent serverServiceIntent;
    private boolean serverThreadActive;
    public WifiP2pInfo wifiInfo;
    public InetAddress masterIp;
    public InetAddress slaveIp;
    public final int nrsPort = 8010;
//	public InetSocketAddress masterAddr;
//	public InetSocketAddress slaveAddr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Block auto opening keyboard
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //set status to stopped
        TextView serverServiceStatus = (TextView) findViewById(R.id.server_status_text);
        serverServiceStatus.setText(R.string.server_stopped);
/*        
        wifiManager.createGroup(wifichannel,  new WifiP2pManager.ActionListener()  {
    	    public void onSuccess() {
    	    	setServerFileTransferStatus("WiFi Group creation successful");

    	    	//Group creation successful
    	    }

    	    public void onFailure(int reason) {
    	    	setServerFileTransferStatus("WiFi Group creation failed");

    	    	//Group creation failed

    	    }
    	});    	
    	*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void startFileBrowseActivity(View view) {
        Intent clientStartIntent = new Intent(this, FileBrowser.class);
        startActivityForResult(clientStartIntent, fileRequestID);
        //Path returned to onActivityResult
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == fileRequestID) {
            //Fetch result
            File targetDir = (File) data.getExtras().get("file");

            if (targetDir.isDirectory()) {
                if (targetDir.canWrite()) {
                    downloadTarget = targetDir;
                    TextView filePath = (TextView) findViewById(R.id.server_file_path);
                    filePath.setText(targetDir.getPath());
                    setServerFileTransferStatus("Download directory set to " + targetDir.getName());
                } else {
                    setServerFileTransferStatus("You do not have permission to write to " + targetDir.getName());
                }
            } else {
                setServerFileTransferStatus("The selected file is not a directory. Please select a valid download directory.");
            }
        }
    }

    public void onListen(View view) {
        //If server is already listening on port or transfering data, do not attempt to start server service
        if (!serverThreadActive) {
            //Create new thread, open socket, wait for connection, and transfer file
            serverServiceIntent = new Intent(this, SlaveService.class);
//	    	serverServiceIntent.putExtra("saveLocation", downloadTarget);
            serverServiceIntent.putExtra("port", nrsPort);
            serverServiceIntent.putExtra("masterIp", masterIp);
            serverServiceIntent.putExtra("slaveIp", slaveIp);
            serverServiceIntent.putExtra("slaveResult", new ResultReceiver(null) {
                @Override
                protected void onReceiveResult(int resultCode, final Bundle resultData) {
                    if (resultCode == nrsPort) {
                        if (resultData == null) {
                            //Server service has shut down. Download may or may not have completed properly.
                            serverThreadActive = false;

                            final TextView server_status_text = (TextView) findViewById(R.id.server_status_text);
                            server_status_text.post(new Runnable() {
                                public void run() {
                                    server_status_text.setText(R.string.server_stopped);
                                }
                            });
                        } else {
                            final TextView server_file_status_text = (TextView) findViewById(R.id.server_file_transfer_status);

                            server_file_status_text.post(new Runnable() {
                                public void run() {
                                    server_file_status_text.setText((String) resultData.get("message"));
                                }
                            });
                        }
                    }
                }
            });

            serverThreadActive = true;
            startService(serverServiceIntent);
//
//            /*start a thread to listen to master*/
////			Handler handler = new Handler(Looper.getMainLooper()){
////				@Override
////				public void handleMessage(Message msg){
////					switch(msg.what){
////                        case 1:
////                            /*start a new thread to handle msg from master*/
////                            Thread slaveDlThread = new Thread(new SlaveDlThread((String)msg.obj, this));
////                            setServerStatus((String)msg.obj);
////                            break;
////                        case 2:
////                            /*transfer task back to master*/
////                            Thread slaveSendThread = new Thread(new SlaveSendThread(port, slaveIp, this));
////						default:
////							super.handleMessage(msg);
////					}
////				}
////			};
////			Thread socketServerThread = new Thread(new MultiThreadServer(port, downloadTarget, handler));
////			socketServerThread.start();
//
//	    	//Set status to running
//	    	TextView serverServiceStatus = (TextView) findViewById(R.id.server_status_text);
//	    	serverServiceStatus.setText(R.string.server_running);
        } else {
            //Set status to already running
            TextView serverServiceStatus = (TextView) findViewById(R.id.server_status_text);
            serverServiceStatus.setText("The server is already running");
        }
    }

    public void startServer(View view) {
        path = "/";
        downloadTarget = new File(path);
        serverServiceIntent = null;
        serverThreadActive = false;

		/*initial wifi direct manager*/
        wifiManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        wifichannel = wifiManager.initialize(this, getMainLooper(), null);
        wifiServerReceiver = new WiFiServerBroadcastReceiver(wifiManager, wifichannel, this);
        wifiServerReceiverIntentFilter = new IntentFilter();
        ;
        wifiServerReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiServerReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiServerReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiServerReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        registerReceiver(wifiServerReceiver, wifiServerReceiverIntentFilter);

        setServerFileTransferStatus("No File being transfered");

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
            setServerFileTransferStatus(e.getMessage());
        }
        wifiManager.discoverPeers(wifichannel, null);
        /*start slave thread*/
//		if (!serverThreadActive) {
//			Thread slaveThread = new Thread(new SlaveThread(masterIp, port));
//			serverThreadActive = true;
//			setServerStatus("server is running");
//		}


    }

    public void stopServer(View view) {
        //stop download thread
        if (serverServiceIntent != null) {
            stopService(serverServiceIntent);
        }
    }

    public void startClientActivity(View view) {
        stopServer(null);
        Intent clientStartIntent = new Intent(this, ClientActivity.class);
        startActivity(clientStartIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stopServer(null);
        //unregisterReceiver(wifiServerReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            /* Unregister broadcast receiver */
            if (wifiServerReceiver != null) {
                unregisterReceiver(wifiServerReceiver);
            }

            /* remove wifi direct group */
            if (wifiManager != null) {
                wifiManager.removeGroup(wifichannel, null);
            }

            /* stop master service */
            if (serverServiceIntent != null) {
                stopService(serverServiceIntent);
            }
        } catch (IllegalArgumentException e) {
            /* This will happen if the server was never running and the stop
               button was pressed.
               Do nothing in this case. */
        }
    }

    public void setServerWifiStatus(String message) {
        TextView server_wifi_status_text = (TextView) findViewById(R.id.server_wifi_status_text);
        server_wifi_status_text.setText(message);
    }

    public void setServerStatus(String message) {
        TextView server_status_text = (TextView) findViewById(R.id.server_status_text_2);
        server_status_text.setText(message);
    }

    public void setServerFileTransferStatus(String message) {
        TextView server_status_text = (TextView) findViewById(R.id.server_file_transfer_status);
        server_status_text.setText(message);
    }
}