package edu.pdx.cs410.wifi.direct.file.transfer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import edu.pdx.cs410.wifi.direct.file.transfer.master.MasterActivity;
import edu.pdx.cs410.wifi.direct.file.transfer.slave.SlaveActivity;

/**
 * Created by User on 8/3/2015.
 */
public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_main);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onMasterMode(View view) {
        Intent masterStartIntent = new Intent(this, MasterActivity.class);
        startActivity(masterStartIntent);
    }

    public void onSlaveMode(View view) {
        Intent slaveStartIntent = new Intent(this, SlaveActivity.class);
        startActivity(slaveStartIntent);
    }
}
