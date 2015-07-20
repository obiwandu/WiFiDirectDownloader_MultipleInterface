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


package edu.pdx.cs410.wifi.direct.file.transfer.master;

import android.content.BroadcastReceiver;
import android.net.wifi.p2p.WifiP2pInfo;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import edu.pdx.cs410.wifi.direct.file.transfer.master.ClientActivity;
import edu.pdx.cs410.wifi.direct.file.transfer.oldClass.RecvThread;
import edu.pdx.cs410.wifi.direct.file.transfer.trans.TcpTrans;

/*
 Some of this code is developed from samples from the Google WiFi Direct API Guide 
 http://developer.android.com/guide/topics/connectivity/wifip2p.html
 */

public class WiFiClientBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private Channel channel;
    private ClientActivity activity;

    public WiFiClientBroadcastReceiver(WifiP2pManager manager, Channel channel, ClientActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
        activity.setClientStatus("Client Broadcast receiver created");

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                activity.setClientWifiStatus("Wifi Direct is enabled");
            } else {
                activity.setClientWifiStatus("Wifi Direct is not enabled");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            //This broadcast is sent when status of in range peers changes. Attempt to get current list of peers.
            manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {

                public void onPeersAvailable(WifiP2pDeviceList peers) {

                    activity.displayPeers(peers);

                }
            });
            //update UI with list of peers
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            NetworkInfo networkState = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            WifiP2pInfo wifiInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            activity.wifiInfo = wifiInfo;

            if (networkState.isConnected()) {
                //set client state so that all needed fields to make a transfer are ready
                activity.setTransferStatus(true);
                activity.setNetworkToReadyState(true, wifiInfo, device);
                activity.setClientStatus("WiFi Direct Connection Status: Connected, GO: " + wifiInfo.isGroupOwner);
                if (wifiInfo.isGroupOwner) {
                    /*connect to master to make master know slave's ip*/
                    try {
                        activity.setClientWifiStatus("WiFi Direct Connection Info is exchanging...");
                        activity.masterIp = wifiInfo.groupOwnerAddress;
                        InetSocketAddress localAddr = new InetSocketAddress(wifiInfo.groupOwnerAddress, 8001);  // specify a port for this transmission
//                        activity.setClientWifiStatus("master IP is " + activity.masterIp.getHostAddress());
                        Thread conn = new Thread(new FirstRecvThd(localAddr, handler));
                        conn.start();
//                        Handler h = new Handler(Looper.getMainLooper()) {
//                            @Override
//                            public void handleMessage(Message msg) {
//                                switch (msg.what) {
//                                    case 1:
//                                    /*data has been sent back, start listen to master again*/
////                                        activity.slaveIp = (InetAddress) msg.obj;
//                                        activity.setClientWifiStatus("Connection Status: Connected, slave IP is " + (String)msg.obj);
//                                        activity.slaveIp = (String)msg.obj;
//                                    case 2:
//                                        activity.setClientWifiStatus("Connection Status: " + (String) msg.obj);
//                                    default:
//                                        super.handleMessage(msg);
//                                }
//                            }
//                        };
//                        byte[] buf = new byte[256];
//                        Thread con = new Thread(new RecvThread(activity.port, h, buf));
//                        con.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else{
                    activity.setClientStatus("Master must be GO");
                }
            } else {
                //set variables to disable file transfer and reset client back to original state

                activity.setTransferStatus(false);
                activity.setClientStatus("Connection Status: Disconnected");
                manager.cancelConnect(channel, null);

            }
            //activity.setClientStatus(networkState.isConnected());

            // Respond to new connection or disconnections
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
                    activity.setClientWifiStatus("master IP: " + activity.masterIp.getHostAddress()
                            + " | slave IP:" + activity.slaveIp.getHostAddress());
                    break;
                case 2:
                    String str_msg = (String)msg.obj;
                    activity.setClientFileTransferStatus(str_msg);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    private class FirstRecvThd extends Thread {
        private InetSocketAddress localAddr;
        public InetAddress remoteIp;
        public Handler handler;
        public FirstRecvThd(InetSocketAddress localAddress, Handler hdl) {
            localAddr = localAddress;
//            serviceEnabled = true;
            handler = hdl;
        }

        public void run() {
            try {
                remoteIp = TcpTrans.listen(localAddr);
            } catch (Exception e){
                Message msg = handler.obtainMessage(2, e.toString());
                msg.sendToTarget();
            }
            Message msg = handler.obtainMessage(1, remoteIp);
            msg.sendToTarget();
        }
    }
}