package com.lineac.lineacar;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class BtPairedRecyclerViewAdapter extends RecyclerView.Adapter<BtPairedRecyclerViewAdapter.MyViewHolder>{

    ArrayList<BluetoothDevice> mDeviceList;
    static final String EXTRA_BT_SELECTED_DEVICE = "EXTRA_BT_SELECTED_DEVICE";
    Context mContext;
    Activity mActivity;

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.activity_bt_paired_device_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.display(i, mDeviceList.get(i));
    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }

    private void onBtDeviceClicked(BluetoothDevice mBtSelectedDevice) {
        Intent mIntentDeviceSelected = new Intent();
        mIntentDeviceSelected.putExtra(EXTRA_BT_SELECTED_DEVICE, mBtSelectedDevice);

        mActivity.setResult(Activity.RESULT_OK,mIntentDeviceSelected);
        mActivity.finish();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView mPairedDeviceNameTv;
        private TextView mPairedDeviceMacTv;
        BluetoothDevice mCurrentBtDevice;


        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mPairedDeviceNameTv = itemView.findViewById(R.id.activity_bt_settings_paired_device_item_tv);
            mPairedDeviceMacTv = itemView.findViewById(R.id.activity_bt_paired_device_item_mac_tv);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("toto", "select BT!");
                    mPairedDeviceNameTv.setTextColor(mContext.getResources().getColor(R.color.colorAccent,mContext.getTheme()));
                    onBtDeviceClicked(mCurrentBtDevice);
                }
            });
        }

        void display(int position,BluetoothDevice mBtDevice) {
            mCurrentBtDevice=mBtDevice;
            mPairedDeviceNameTv.setText(mBtDevice.getName());
            mPairedDeviceMacTv.setText(mBtDevice.getAddress());
        }

    }

}
