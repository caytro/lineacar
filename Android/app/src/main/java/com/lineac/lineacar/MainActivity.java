package com.lineac.lineacar;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "toto";
    private static final int ENABLE_BT_REQUEST_CODE = 1 ;
    private static final int BT_DEVICE_SELECT_REQUEST_CODE=2;
    private static final int CONNECTION_STATE_UP = 1;
    private static final int CONNECTION_STATE_DOWN = 2;
    public static final int RECEIVE_MESSAGE =1;
    public static final int MAX_BUFFER_SIZE = 256;


    private BluetoothConnectionService mBluetoothConnectionService;


    private Button mUpButton;
    private Button mRightButton;
    private Button mDownButton;
    private Button mLeftButton;
    private Button mStopButton;
    private Button mConnectionButton;
    private Button mTestButton;
    private TextView mConnectionStateText;
    private TextView mLogText;
    private ScrollView mLogTextScrollView;
    private BluetoothDevice mBluetoothDevice;
    private int connexionState;
    public  Handler mHandler;
    private StringBuilder mSb=new StringBuilder(MAX_BUFFER_SIZE);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide(); // hide the title bar
        }

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen
        setContentView(R.layout.activity_main);

        connexionState = CONNECTION_STATE_DOWN;
        mUpButton = findViewById(R.id.activity_main_haut_btn);
        mRightButton = findViewById((R.id.activity_main_droit_btn));
        mDownButton = findViewById(R.id.activity_main_bas_btn);
        mLeftButton = findViewById(R.id.activity_main_gauche_btn);
        mStopButton = findViewById(R.id.activity_main_stop_btn);
        mConnectionButton = findViewById(R.id.activity_main_connexion_button);
        mTestButton = findViewById(R.id.activity_main_test_button);
        mLogText = findViewById(R.id.activity_main_log_text);
        mConnectionStateText = findViewById(R.id.activity_main_connexion_state_tv);
        mLogTextScrollView = findViewById(R.id.activity_main_log_text_scroll_view);

        mConnectionStateText.setText(R.string.connection_state_disconnected);
        mLogText.setText(R.string.appTitre);

        mBluetoothConnectionService=new BluetoothConnectionService();
        initBtMessageHandler();


        Log.d(TAG, "onCreate : Checking Bluetooth state...");
        enableBt();

        Log.d(TAG, "onCreate : configure connect button click listener...");
        mConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connexionState == CONNECTION_STATE_DOWN) {
                    Intent mBtDeviceSelectIntent = new Intent(MainActivity.this, com.lineac.lineacar.BtDeviceSelectActivity.class);
                    startActivityForResult(mBtDeviceSelectIntent, BT_DEVICE_SELECT_REQUEST_CODE);
                } else {
                        mBluetoothConnectionService.closeBtSocket();
                        connexionState = CONNECTION_STATE_DOWN;
                        mConnectionButton.setText(getText(R.string.labelSwitchConnect));
                        mConnectionStateText.setText(getString(R.string.connection_state_disconnected));
                }
            }
        });

        mTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothConnectionService.writeLn("Test".getBytes());
            }
        });

        Log.d(TAG, "onCreate : configure up button click listener...");
        mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickControlButton(v);
            }
        });

        Log.d(TAG, "onCreate : configure down button click listener...");
        mDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickControlButton(v);
            }
        });

        Log.d(TAG, "onCreate : configure right click listener...");
        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickControlButton(v);
            }
        });

        Log.d(TAG, "onCreate : configure left button click listener...");
        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickControlButton(v);
            }
        });

        Log.d(TAG, "onCreate : configure stop button click listener...");
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickControlButton(v);
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (connexionState == CONNECTION_STATE_UP) {
            mBluetoothConnectionService.closeBtSocket();
        }

    }


    @SuppressLint("HandlerLeak")
    private void initBtMessageHandler() {
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                Log.d(TAG, "initBtMessageHandler : ");
                if (msg.what == RECEIVE_MESSAGE) {
                    String strIncom = (String) msg.obj;
                    Log.d(TAG, "initBtMessageHandler : msg.obj : " + msg.obj);
                    Log.d(TAG, "initBtMessageHandler : msg.arg1 : " + msg.arg1);
                    Log.d(TAG, "initBtMessageHandler : strIncom : " + strIncom);
                    // create string from bytes array
                    mLogText.append(strIncom.concat("\n"));
                    mLogTextScrollView.fullScroll(View.FOCUS_DOWN);
                }
            }
        };
        mBluetoothConnectionService.setHandler(mHandler);
    }


    private void clickControlButton(View mButton) {
        if (connexionState == CONNECTION_STATE_UP) {
            String message = "";
            if (mButton.getId() == mUpButton.getId()) {
                message = getResources().getString(R.string.message_up);
            } else if (mButton.getId() == mDownButton.getId()) {
                message = getResources().getString(R.string.message_down);
            } else if (mButton.getId() == mRightButton.getId()) {
                message = getResources().getString(R.string.message_right);
            } else if (mButton.getId() == mLeftButton.getId()) {
                message = getResources().getString(R.string.message_left);
            } else if (mButton.getId() == mStopButton.getId()) {
                message = getResources().getString(R.string.message_stop);
            }
            mBluetoothConnectionService.writeLn("Sending message : ".getBytes());
            mBluetoothConnectionService.writeLn(message.getBytes());
        } else {
            Toast.makeText(getApplicationContext(),getString(R.string.connection_state_disconnected),Toast.LENGTH_LONG).show();
        }
     }



    protected void enableBt() {
        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBtAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth disabled -> enabling");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_CODE);
        } else {
            Log.d(TAG, "Bluetooth is already enabled");
        }
    }

    private  void connectBtDevice() {
        boolean success;
        success=mBluetoothConnectionService.connectBtDevice(mBluetoothDevice);

        if (success) {

            mConnectionStateText.setText(getString(R.string.connection_state_connected, mBluetoothDevice.getName()));
            connexionState = CONNECTION_STATE_UP;
            mConnectionButton.setText(R.string.labelSwitchDisconnect);
        } else {
            Toast.makeText(getApplicationContext(),getString(R.string.unable_to_connect_to,mBluetoothDevice.getName()),Toast.LENGTH_LONG).show();
            Log.d(TAG,getString(R.string.unable_to_connect_to,mBluetoothDevice.getName()));
            mConnectionStateText.setText(getString(R.string.connection_state_disconnected));
            connexionState=CONNECTION_STATE_DOWN;
            mConnectionButton.setText(R.string.labelSwitchConnect);
            mBluetoothConnectionService.closeBtSocket();
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "ActivityResult - requestCode : "+requestCode+" resultCode : "+resultCode);
        switch (requestCode) {
            case ENABLE_BT_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(getApplicationContext(), R.string.bt_up, Toast.LENGTH_LONG).show();
                } else {

                    Toast.makeText(getApplicationContext(), R.string.bt_down, Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(),R.string.GiveUp, Toast.LENGTH_LONG).show();
                    Log.d(TAG, getString(R.string.bluetooth_disable_bye));
                    finish();
                }
                break;
            case BT_DEVICE_SELECT_REQUEST_CODE :
                if (resultCode == RESULT_OK) {
                    if (data != null && data.getExtras()!=null) {
                        mBluetoothDevice = data.getExtras().getParcelable(BtPairedRecyclerViewAdapter.EXTRA_BT_SELECTED_DEVICE);
                        if (mBluetoothDevice != null) {
                            Log.d(TAG, "Device name : " + mBluetoothDevice.getName() + " - MAC : " + mBluetoothDevice.getAddress());
                            connectBtDevice();
                        } else {
                            Log.d(TAG,"null exception");
                        }

                    }
                } else {
                    Log.d(TAG, "resultCode not OK");
                }
                break;
            default:
                Log.d(TAG, "Unknown requestCode");
                break;
        }
    }
    


}
