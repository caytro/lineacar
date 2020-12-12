package com.lineac.lineacar;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;

public class BtDeviceSelectActivity extends AppCompatActivity  {

    BtPairedRecyclerViewAdapter mBtPairedRecycledViewAdapter;
    public static final String TAG = "totoDeviceSelect";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ArrayList<BluetoothDevice> mBtDevicesList;


        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide(); // hide the title bar
        }

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen
        setContentView(R.layout.activity_bt_paired_device_recycler_view);

        Log.d(TAG, "onCreate : call BluetoothConnectionService.getBtPairedDevices()");
        mBtDevicesList = BluetoothConnectionService.getBtPairedDevices();
        RecyclerView rv = findViewById(R.id.activity_bt_settings_paired_devices_lv);
        Log.d(TAG, "onCreate : setting layout manager");
        rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        Log.d(TAG, "onCreate : initialize mBtPairedRecycledViewAdapter");
        mBtPairedRecycledViewAdapter = new BtPairedRecyclerViewAdapter();
        mBtPairedRecycledViewAdapter.mDeviceList = mBtDevicesList;
        mBtPairedRecycledViewAdapter.mContext = this;
        Log.d(TAG, "onCreate : setAdapter");
        rv.setAdapter(mBtPairedRecycledViewAdapter);
        mBtPairedRecycledViewAdapter.mActivity=this;

    }

}
