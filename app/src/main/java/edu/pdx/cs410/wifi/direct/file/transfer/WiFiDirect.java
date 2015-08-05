package edu.pdx.cs410.wifi.direct.file.transfer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.view.View;

import edu.pdx.cs410.wifi.direct.file.transfer.master.MasterActivity;
import edu.pdx.cs410.wifi.direct.file.transfer.master.WiFiClientBroadcastReceiver;
import edu.pdx.cs410.wifi.direct.file.transfer.master.WiFiMasterBroadcastReceiver;
import edu.pdx.cs410.wifi.direct.file.transfer.slave.SlaveActivity;
import edu.pdx.cs410.wifi.direct.file.transfer.slave.WiFiServerBroadcastReceiver;
import edu.pdx.cs410.wifi.direct.file.transfer.slave.WiFiSlaveBroadcastReceiver;

/**
 * Created by User on 8/3/2015.
 */
public class WiFiDirect {
    private WifiP2pManager wifiManager;
    private WifiP2pManager.Channel wifichannel;
    private BroadcastReceiver receiver;
    private IntentFilter receiverIntentFilter;
    private MasterActivity masterActivity = null;
    private SlaveActivity slaveActivity = null;

    public WiFiDirect(MasterActivity act) {
        masterActivity = act;
        wifiManager = (WifiP2pManager) masterActivity.getSystemService(Context.WIFI_P2P_SERVICE);
        wifichannel = wifiManager.initialize(masterActivity, masterActivity.getMainLooper(), null);
        receiver = new WiFiMasterBroadcastReceiver(wifiManager, wifichannel, masterActivity);
        receiverIntentFilter = new IntentFilter();

        /* Initialization */
        receiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        receiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        receiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        receiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        masterActivity.registerReceiver(receiver, receiverIntentFilter);
    }

    public WiFiDirect(SlaveActivity act) {
        slaveActivity = act;
        wifiManager = (WifiP2pManager) slaveActivity.getSystemService(Context.WIFI_P2P_SERVICE);
        wifichannel = wifiManager.initialize(slaveActivity, slaveActivity.getMainLooper(), null);
        receiver = new WiFiSlaveBroadcastReceiver(wifiManager, wifichannel, slaveActivity);
        receiverIntentFilter = new IntentFilter();

        /* Initialization */
        receiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        receiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        receiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        receiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        slaveActivity.registerReceiver(receiver, receiverIntentFilter);
    }

    public void search() {
        //Discover peers, no call back method given
        wifiManager.discoverPeers(wifichannel, null);

    }

    public void connect(final WifiP2pDevice wifiPeer) {
//        this.targetDevice = wifiPeer;

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = wifiPeer.deviceAddress;

        /*Make sure that client is always the group owner*/
        /*the greater groupOwnerIntent is, the greater possibility the current end can be the GO*/
        config.groupOwnerIntent = 15;
        wifiManager.connect(wifichannel, config, new WifiP2pManager.ActionListener() {
            public void onSuccess() {
                //setClientStatus("Connection to " + targetDevice.deviceName + " sucessful");
            }

            public void onFailure(int reason) {
                //setClientStatus("Connection to " + targetDevice.deviceName + " failed");
            }
        });

    }

    public void close() {
        try {
            /* Unregister broadcast receiver */
            if (receiver != null) {
                if (masterActivity != null) {
                    masterActivity.unregisterReceiver(receiver);
                }
                if (slaveActivity != null) {
                    slaveActivity.unregisterReceiver(receiver);
                }
            }

            /* remove wifi direct group */
            if (wifiManager != null) {
                wifiManager.removeGroup(wifichannel, null);
            }
        } catch (IllegalArgumentException e) {
            /* This will happen if the server was never running and the stop button was pressed.
               Do nothing in this case. */
        }
    }
}
