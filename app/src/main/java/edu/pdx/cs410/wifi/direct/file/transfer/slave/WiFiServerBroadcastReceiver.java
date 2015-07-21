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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.oldClass.SendThread;
import edu.pdx.cs410.wifi.direct.file.transfer.slave.MainActivity;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpTrans;

/*
Some of this code is developed from samples from the Google WiFi Direct API Guide 
http://developer.android.com/guide/topics/connectivity/wifip2p.html
*/


public class WiFiServerBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private Channel channel;
    private MainActivity activity;

    public WiFiServerBroadcastReceiver(WifiP2pManager manager, Channel channel, MainActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;

        activity.setServerStatus("Server Broadcast Receiver created");

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                activity.setServerWifiStatus("Wifi Direct is enabled");
            } else {
                activity.setServerWifiStatus("Wifi Direct is not enabled");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            NetworkInfo networkState = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            WifiP2pInfo wifiInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
            activity.wifiInfo = wifiInfo;

            if (networkState.isConnected()) {
                activity.setServerStatus("WiFi Direct Connection Status: Connected | Is Group Owner: " + wifiInfo.isGroupOwner);
//                activity.masterIp = wifiInfo.groupOwnerAddress.toString();
                if (!wifiInfo.isGroupOwner) {
                    try {
                        activity.setServerWifiStatus("WiFi Direct Connection Info is exchanging...");
                        activity.masterIp = wifiInfo.groupOwnerAddress;
                        InetSocketAddress remoteAddr = new InetSocketAddress(wifiInfo.groupOwnerAddress, 8001);  // specify a port number here

                        Thread conn = new Thread(new FirstSendThd(remoteAddr, handler));
                        conn.start();
                    } catch (Exception e) {
                        activity.setServerFileTransferStatus("Exception:"+ e.toString());
                    }
                }
                else{
                    activity.setServerStatus("Error: Slave can not be GO!");
                }
            } else {
                activity.setServerStatus("Connection Status: Disconnected");
                manager.cancelConnect(channel, null);
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    activity.slaveIp = (InetAddress)msg.obj;
                    activity.setServerWifiStatus("master IP: " + activity.masterIp.getHostAddress()
                                                  + " | slave IP:" + activity.slaveIp.getHostAddress());
                    break;
                case 2:
                    String str_msg = (String)msg.obj;
                    activity.setServerFileTransferStatus(str_msg);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    public class FirstSendThd extends Thread {
        InetAddress localIp;
        InetSocketAddress remoteAddr;
        Handler handler;

        public FirstSendThd(InetSocketAddress remoteAddress, Handler hdl) {
            remoteAddr = remoteAddress;
            handler = hdl;
        }

        @Override
        public void run() {
            try {
                localIp = TcpTrans.connect(remoteAddr);
            }
            catch (Exception e) {
                Message msg = handler.obtainMessage(2, e.toString());
                msg.sendToTarget();
            }
            Message msg = handler.obtainMessage(1, localIp);
            msg.sendToTarget();
        }
    }
}