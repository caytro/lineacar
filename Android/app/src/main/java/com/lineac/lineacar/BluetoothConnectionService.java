package com.lineac.lineacar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static com.lineac.lineacar.MainActivity.RECEIVE_MESSAGE;

class BluetoothConnectionService {
    private static final String TAG = "toto";
    public static final String EOL="\n";
    private final BluetoothAdapter mBluetoothAdapter;

    private BluetoothSocket mBluetoothSocket;
    private BluetoothDevice mBluetoothDevice;
    private Handler mHandler;  // Activity has to setHandler(mHandler) after having defined Handler.handleMessage(Message msg) method
    private boolean handlerLock = false; // used to prevent mHandler.obtainMessage during message processing.

    private ConnectedThread mConnectedThread;

    BluetoothConnectionService() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    /* Returns a ArrayList containing BT paired devices ArrayList<BluetoothDevice> */

    static ArrayList<BluetoothDevice> getBtPairedDevices() {
        Set<BluetoothDevice> mSetPairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        ArrayList<BluetoothDevice> mBtPairedDevices=new ArrayList<>();
            if (mSetPairedDevices.size() > 0) {
                mBtPairedDevices.addAll(mSetPairedDevices);
            }
        return (mBtPairedDevices);
    }

    // call this method connectBtDevice(BluetoothDevice mBtDevice) to create socket, connect and launch connectedThread
    // mBtDevice has to be previously bounded to Android device
    // Activity use BlueToothConnectionService.write() to send data to BT device
    // Activity has to create a Handler mHandler, configure mHandler.handleMessage(Message msg)
    // and finally BlueToothConnectionService.setHandler(mHandler) to receive data from BT device

    boolean connectBtDevice(BluetoothDevice mBtDevice) {
        boolean success=true;
        mBluetoothDevice=mBtDevice;
        mBluetoothAdapter.cancelDiscovery();
        BluetoothSocket mSocket= getBtSocketAsClient();
        if (mSocket == null) {
            String message = "connectBtDevice : Fail connect to " + mBluetoothDevice.getName();
            Log.d(TAG, message);
            success = false;
        } else {
            mBluetoothSocket=mSocket;
            success=initBtSocketConnexionAsClient(mBluetoothSocket);
            if (success) {
                connected(mBluetoothSocket);
            } else {
                Log.d(TAG, "connectBtDevice : Unable to connect " + mBtDevice.getName());
            }
        }
        return success;
    }

    private BluetoothSocket getBtSocketAsClient() {
        UUID mUuidDevice;
        ParcelUuid[] mParcelUuidDevice = mBluetoothDevice.getUuids();
        BluetoothSocket mSocket = null;
        /* return null if UUID unavailable */

        if (mParcelUuidDevice.length > 0) {
            mUuidDevice = mParcelUuidDevice[0].getUuid();
            try {
                mSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(mUuidDevice);
            } catch (IOException e) {
                Log.d(TAG, "getBtSocketAsClient : createRfcommSocketToServiceRecord failed " + e.getLocalizedMessage());
                mSocket=null;
            }

        }else {
            Log.d(TAG, "getBtSocketAsClient : UUID unavailable");
        }
        return mSocket;
    }

    private boolean initBtSocketConnexionAsClient(BluetoothSocket mBtSocket) {
        boolean success=true;
        try {
            mBtSocket.connect();
        } catch (IOException e) {
            Log.d(TAG,"initBtSocketConnexionAsClient : connect failed "+ e.getLocalizedMessage());
            success=false;
            try {
                mBtSocket.close();
            } catch(IOException e1){
                Log.d(TAG,"initBtSocketConnexionAsClient : disconnect failed "+ e1.getLocalizedMessage());
            }
        }
        return success;
    }

        // handlerLock is used to prevent listening thread to send a new message while the handler is processing a previous message
        // the method implementing Handler.handleMessage() has to execute unLockHandler()

    private void lockHandler() {
        handlerLock=true;
    }

    void unLockHandler() {
        handlerLock=false;
    }

    void setHandler(Handler mH) {
        mHandler=mH;
    }



    /**
     ConnectedThread is responsible for maintaining the BTConnection, Sending the data, and
     receiving incoming data through input/output streams respectively.
     **/
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        String incommingMessage = "";

        ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];  // buffer store for the stream
            StringBuilder mSbReceived = new StringBuilder();
            int nbIncomingBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream


/*
                try {
                    lockHandler();
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);
                    mHandler.obtainMessage(RECEIVE_MESSAGE, bytes, -1, buffer).sendToTarget();          // Send to message queue Handler
                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage() );
                    break;
                }
*/
                try {
                    nbIncomingBytes=mmInStream.read(buffer,0,1);
                    if (nbIncomingBytes > 0) {
                        if (buffer[0]==EOL.getBytes()[0]) {
                            Log.d(TAG, "BluetoothConnectionService.ConnectedThread.run : Message ready to send to mHandler : "+mSbReceived.toString());
                            mHandler.obtainMessage(RECEIVE_MESSAGE, 0, -1, mSbReceived.toString()).sendToTarget();          // Send to message queue Handler
                            mSbReceived=new StringBuilder();
                        } else {

                            mSbReceived.append(Character.valueOf((char)buffer[0]));
                        }
                    }

                } catch (IOException e) {
                    Log.d(TAG,"ConnectedThread.run : IOException : "+e.getLocalizedMessage());
                    break;
                }

            }
        }

        //Call this from the main activity to send data to the remote device

        void write(byte[] bytes) {
            String text = new String(bytes, StandardCharsets.US_ASCII);
            Log.d(TAG, "BluetoothConnectionService.ConnectedTread.write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "BluetoothConnectionService.ConnectedTread.write: Error writing to output stream. " + e.getMessage() );
            }
        }
    }

    private void connected(BluetoothSocket mmSocket) {
        Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    void write(byte[] out) {
         Log.d(TAG, "write: Write Called.");
        //perform the write
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {

            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    void writeLn(byte[] out) {
        Log.d(TAG, "BluetoothConnectionService.writeLn Called : "+new String(out,StandardCharsets.US_ASCII));
        //perform the write
        write(out);
        write(EOL.getBytes());
    }

    void closeBtSocket() {
        Log.d(TAG, "close Bluetooth socket");

        if (mBluetoothSocket != null) {
            try {
                mBluetoothSocket.close();
            } catch (IOException e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
        } else {
            Log.d(TAG, "closeBtSocket : mBluetoothSocket is null");
        }
    }

}
