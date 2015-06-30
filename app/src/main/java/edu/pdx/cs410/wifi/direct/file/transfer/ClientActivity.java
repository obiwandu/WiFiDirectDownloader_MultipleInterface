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

package edu.pdx.cs410.wifi.direct.file.transfer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.widget.AdapterView.OnItemClickListener;

//import edu.pdx.cs410.wifi.direct.file.transfer.HttpDl;

public class ClientActivity extends Activity {
	
	public final int fileRequestID = 98;
	public final int port = 7950;


	
	private WifiP2pManager wifiManager;
	private Channel wifichannel;
	private BroadcastReceiver wifiClientReceiver;

	private IntentFilter wifiClientReceiverIntentFilter;

	private boolean connectedAndReadyToSendFile;
	
	private boolean filePathProvided;
	private File fileToSend;
	private File fileToRecv;
	private boolean transferActive;
	
	private Intent clientServiceIntent; 
	private WifiP2pDevice targetDevice;
	private WifiP2pInfo wifiInfo;

		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        wifiManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        
        wifichannel = wifiManager.initialize(this, getMainLooper(), null);
        wifiClientReceiver = new WiFiClientBroadcastReceiver(wifiManager, wifichannel, this);
        
        wifiClientReceiverIntentFilter = new IntentFilter();;
        wifiClientReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiClientReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiClientReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiClientReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        
        connectedAndReadyToSendFile = false;
        filePathProvided = false;
        fileToSend = null;
        transferActive = false;
        clientServiceIntent = null;
        targetDevice = null;
        wifiInfo = null;
        
        registerReceiver(wifiClientReceiver, wifiClientReceiverIntentFilter);

        setClientFileTransferStatus("Client is currently idle");

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
			fileToSend = new File(sendPath + "/" + sendFileName);
			fileToRecv = new File(recvPath + "/" + recvFileName + "-part2");
			filePathProvided = true;
		}
		catch (Exception e){
			setClientFileTransferStatus(e.getMessage());
		}

        //setTargetFileStatus("testing");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_client, menu);
        
        return true;
    }

    
    public void setTransferStatus(boolean status)
    {
    	connectedAndReadyToSendFile = status;
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    public void setNetworkToReadyState(boolean status, WifiP2pInfo info, WifiP2pDevice device)
    {
    	wifiInfo = info;
    	targetDevice = device;
    	connectedAndReadyToSendFile = status;
    }
    
    private void stopClientReceiver()
    {        
       	try
    	{
            unregisterReceiver(wifiClientReceiver);
    	}
    	catch(IllegalArgumentException e)
    	{
    		//This will happen if the server was never running and the stop button was pressed.
    		//Do nothing in this case.
    	}
    }
        
    public void searchForPeers(View view) {
        
        //Discover peers, no call back method given
        wifiManager.discoverPeers(wifichannel, null);

    }
    
    
    public void browseForFile(View view) {
        Intent clientStartIntent = new Intent(this, FileBrowser.class);
        startActivityForResult(clientStartIntent, fileRequestID);  
        
    }
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	//fileToSend

    	if (resultCode == Activity.RESULT_OK && requestCode == fileRequestID) {
    		//Fetch result
    		File targetDir = (File) data.getExtras().get("file");
    		
    		if(targetDir.isFile())
    		{
    			if(targetDir.canRead())
    			{
    				fileToSend = targetDir;
    				filePathProvided = true;
    				
    				setTargetFileStatus(targetDir.getName() + " selected for file transfer");
    					    			
    			}
    			else
    			{
    				filePathProvided = false;
    				setTargetFileStatus("You do not have permission to read the file " + targetDir.getName());
    			}

    		}
    		else
    		{
				filePathProvided = false;
    			setTargetFileStatus("You may not transfer a directory, please select a single file");
    		}

        }
    }

//	public void download_process()
//	{
//		int totalTaskSize = 0;
//		int masterTaskSize = 0;
//		int slaveTaskSize = 0;
//		char cmdstr[] = new char[256];
//
//		String urlNotePadPP = "dl.notepad-plus-plus.org/downloads/6.x/6.7.5/npp.6.7.5.Installer.exe";
//
//    	/*band width monitoring*/
//		int masterBw = 0;
//		int slaveBw = 0;
//		float masterBandWidth = 0;
//		int masterTimeSpan = 0;
//		int masterDataSize = 0;
//		float transBandWidth = 0;
//		int transTimeSpan = 0;
//		int transDataSize = 0;
//		float totalBandWidth = 0;
//		int totalTimeSpan = 0;
//		int totalDataSize = 0;
//		char infoBuf[] = new char[30];
//
//    	/*task management*/
//		int alreadyTaskSize = 0;
//		int initTaskSize = 100000;
//
//		//start_tim = get_tick();
//
//    	/*find slave(now only 1 slave existing)*/
//
//    	/*handle url, get file size and devide*/
//    	/*connect server, get total size of file*/
//		HttpDl *temptDl = new HttpDl(urlNotePadPP, "eth0", 15150);
//		temptDl->requestDl(0, 0);
//		totalTaskSize = temptDl->totalSize;
//		//printf("total size:%d task size:%d\r\n", totalTaskSize, task_size);
//		Master m(urlNotePadPP, temptDl->serverDomain, temptDl->remotePort,
//			temptDl->remoteFilePath.c_str(), temptDl->remoteFileName.c_str(), totalTaskSize);
//		delete temptDl;
//
//    /*give url info to slave*/
//		TcpConn *testConn = new TcpConn("eth1", 15157, "u2", 15157, 0);
//		m.commandBuilder(cmdstr, 0, 0, 2);
//		testConn->sendFromBuf(cmdstr, strlen(cmdstr) + 1);
//		testConn->recvToBuf(infoBuf, 30);
//    /*task is not finished*/
//		while (alreadyTaskSize < totalTaskSize)
//		{
//        /*schedule tasks*/
//			if (masterBw)
//			{
//				if (slaveBw)
//				{
//                /*m!=0, s!=0*/
//					float m = (float)masterBw;
//					float s = (float)slaveBw;
//					float proportion = m / (m + s);
//					printf("proportion: %f\n", proportion);
//					masterTaskSize = (int)((float)(2 * initTaskSize) * proportion);
//					slaveTaskSize = 2 * initTaskSize - masterTaskSize;
//				}
//				else
//				{
//                /*s==0, slave is lost*/
//					masterTaskSize = 2 * initTaskSize;
//					slaveTaskSize = 0;
//				}
//			}
//			else
//			{
//				if (slaveBw)
//				{
//                /*m==0*/
//					//download can't be done
//					printf("connection lost, download can't be done!\n");
//				}
//				else
//				{
//                /*m==0, s==0,first chunk*/
//					masterTaskSize = initTaskSize;
//					slaveTaskSize = initTaskSize;
//				}
//			}
//
//        /*assign task for slave*/
//			int slaveStart = alreadyTaskSize + slaveTaskSize;
//			int slaveEnd = alreadyTaskSize + 2 * slaveStart - 1;
//			m.commandBuilder(cmdstr, slaveStart, slaveEnd, 0);
//			testConn->sendFromBuf(cmdstr, strlen(cmdstr) + 1);
//			testConn->recvToBuf(infoBuf, 30);
//
//        /*execute master's task*/
//			int masterStart = alreadyTaskSize;
//			int masterEnd = alreadyTaskSize + slaveTaskSize - 1;
//			HttpDl *chunkDl = new HttpDl(urlNotePadPP, "eth0", 15151);
//			chunkDl->requestDl(masterStart, masterEnd);
//			chunkDl->execDl(NULL);
//			masterBw = (int)(chunkDl->bandWidth);
//			masterTimeSpan = chunkDl->timeSpan;
//			masterDataSize = chunkDl->dlDataSize;
//			delete chunkDl;
//
//        /*get slave bandwidth*/
//			testConn->recvToBuf(infoBuf, 30);
//			testConn->sendFromBuf("bandwidth received\n", 19);
//			slaveBw = atoi(infoBuf);
//
//        /*fetch data from slave*/
//			testConn->recvToFile(slaveTaskSize, ".", NULL, "slave_test_transfered");
//			testConn->sendFromBuf("data received\n", 14);
//			transBandWidth = testConn->bandWidth;
//			transTimeSpan = testConn->timeSpan;
//			transDataSize = testConn->recvDataSize;
//
//        /*update task*/
//			alreadyTaskSize += masterTaskSize + slaveTaskSize;
//		}
//		delete testConn;
//
//		printf("download totally compelete!\n");
//		printf("masterBandWidth:%fB/s masterTimeSpan:%ds masterDataSize:%dB\n", masterBandWidth, masterTimeSpan/1000, masterDataSize);
//		//printf("slaveBandWidth:%dB/s\n", slaveBw);
//		printf("transBandWidth:%fB/s transTimeSpan:%ds transDataSize:%dB\n", transBandWidth, transTimeSpan/1000, transDataSize);
//		float tmpAvgBw = ((float)(masterDataSize + transDataSize))/(((float)(masterTimeSpan + transTimeSpan))/(float)1000);
//		//printf("average bandwidth:%dB/s\n", ((masterDataSize + transDataSize) * 1000)/(masterTimeSpan + transTimeSpan));
//		printf("average bandwidth:%fB/s\n", tmpAvgBw);
//		printf("master bw: %d, slave bw: %d\n", masterBw, slaveBw);
//		return 0;
//	}
    
    public void sendFile(View view) {
        
    	//Only try to send file if there isn't already a transfer active
    	if(!transferActive)
    	{
	        if(!filePathProvided)
	        {
	        	setClientFileTransferStatus("Select a file to send before pressing send");
	        }
	        if(!connectedAndReadyToSendFile)
	        {
	        	setClientFileTransferStatus("You must be connected to a server before attempting to send a file");
	        }
	        /*
	        else if(targetDevice == null)
	        {
	        	setClientFileTransferStatus("Target Device network information unknown");
	        }
	        */
	        else if(wifiInfo == null)
	        {
	        	setClientFileTransferStatus("Missing Wifi P2P information");
	        }
	        else
	        {
	        	//Launch client service
	        	clientServiceIntent = new Intent(this, ClientService.class);
	        	clientServiceIntent.putExtra("fileToSend", fileToSend);
				clientServiceIntent.putExtra("fileToRecv", fileToRecv);
	        	clientServiceIntent.putExtra("port", new Integer(port));
	        	//clientServiceIntent.putExtra("targetDevice", targetDevice);
	        	clientServiceIntent.putExtra("wifiInfo", wifiInfo);
	        	clientServiceIntent.putExtra("clientResult", new ResultReceiver(null) {
		    	    @Override
		    	    protected void onReceiveResult(int resultCode, final Bundle resultData) {
		    	    	
		    	    	if(resultCode == port )
		    	    	{
			    	        if (resultData == null) {
			    	           //Client service has shut down, the transfer may or may not have been successful. Refer to message 
			    	        	transferActive = false;				    	        				    	       			    	        				    	        			    	        	
			    	        }
			    	        else
			    	        {    	        	
			    	        	final TextView client_status_text = (TextView) findViewById(R.id.file_transfer_status);

			    	        	client_status_text.post(new Runnable() {
			    	                public void run() {
			    	                	client_status_text.setText((String)resultData.get("message"));
			    	                }
			    	        	});		    	   		    	        	
			    	        }
		    	    	}
		    	           	        
		    	    }
		    	});
	        	
	        	transferActive = true;
		        startService(clientServiceIntent);

	        	
	        	
	        	//end
	        }
    	}
    }

	public void masterDownlad()
	{
		// to be continued
	}

    @Override
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        //Continue to listen for wifi related system broadcasts even when paused
        //stopClientReceiver();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        //Kill thread that is transferring data 
        
        //Unregister broadcast receiver
        stopClientReceiver();
    }
    
    
    public void setClientWifiStatus(String message)
    {
    	TextView connectionStatusText = (TextView) findViewById(R.id.client_wifi_status_text);
    	connectionStatusText.setText(message);	
    }
    
    public void setClientStatus(String message)
    {
    	TextView clientStatusText = (TextView) findViewById(R.id.client_status_text);
    	clientStatusText.setText(message);	
    }
    
    public void setClientFileTransferStatus(String message)
    {
    	TextView fileTransferStatusText = (TextView) findViewById(R.id.file_transfer_status);
    	fileTransferStatusText.setText(message);	
    }
    
    public void setTargetFileStatus(String message)
    {
    	TextView targetFileStatus = (TextView) findViewById(R.id.selected_filename);
    	targetFileStatus.setText(message);	
    }
    
     
    public void displayPeers(final WifiP2pDeviceList peers)
    {
    	//Dialog to show errors/status
		final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("WiFi Direct File Transfer");
		
		//Get list view
    	ListView peerView = (ListView) findViewById(R.id.peers_listview);
    	
    	//Make array list
    	ArrayList<String> peersStringArrayList = new ArrayList<String>();
    	
    	//Fill array list with strings of peer names
    	for(WifiP2pDevice wd : peers.getDeviceList())
    	{
    		peersStringArrayList.add(wd.deviceName);
    	}
    	
    	//Set list view as clickable
    	peerView.setClickable(true);
    	   
    	//Make adapter to connect peer data to list view
    	ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, peersStringArrayList.toArray());    			
    	
    	//Show peer data in listview
    	peerView.setAdapter(arrayAdapter);
    		
    	
		peerView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View view, int arg2,long arg3) {
				
				//Get string from textview
				TextView tv = (TextView) view;
				
				WifiP2pDevice device = null;
				
				//Search all known peers for matching name
		    	for(WifiP2pDevice wd : peers.getDeviceList())
		    	{
		    		if(wd.deviceName.equals(tv.getText()))
		    			device = wd;		    			
		    	}
				
				if(device != null)
				{
					//Connect to selected peer
					connectToPeer(device);
										
				}
				else
				{
					dialog.setMessage("Failed");
					dialog.show();
										
				}							
			}			
				// TODO Auto-generated method stub				
			});
  	
    }
        
    public void connectToPeer(final WifiP2pDevice wifiPeer)
    {
    	this.targetDevice = wifiPeer;
    	
    	WifiP2pConfig config = new WifiP2pConfig();
    	config.deviceAddress = wifiPeer.deviceAddress;
		/*Make sure that client is always the group owner*/
		/*the smaller groupOwnerIntent is, the greater possibility the current end can be the GO*/
		config.groupOwnerIntent = 15;
    	wifiManager.connect(wifichannel, config, new WifiP2pManager.ActionListener()  {
    	    public void onSuccess() {
    	    	
    	    	//setClientStatus("Connection to " + targetDevice.deviceName + " sucessful");
    	    }

    	    public void onFailure(int reason) {
    	    	//setClientStatus("Connection to " + targetDevice.deviceName + " failed");

    	    }
    	});    	
    
    }

}
