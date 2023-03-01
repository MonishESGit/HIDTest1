package com.example.hidtest1;

import static androidx.core.app.ActivityCompat.requestPermissions;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothHidDeviceAppSdpSettings;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.example.hidtest1.MainActivity.*;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executors;

public class HidUtils {
    public static final String TAG = "Hid-Utils";
    public static boolean _connected = false;
    public static boolean isRegister = false;
    public static ConnectionStateChangeListener connectionStateChangeListener;

    public static Context cont = null;

    static BluetoothProfile bluetoothProfile;
    static BluetoothDevice mDevice;
    static BluetoothHidDevice mHidDevice;

    public static void setContext(@Nullable Context appContext) {
        cont = appContext;
    }

    public interface ConnectionStateChangeListener {
        void onConnecting();

        void onConnected();

        void onDisConnected();
    }

    public static void registerApp(Context context) {
        Toast.makeText(context,"Debug2", Toast.LENGTH_LONG).show();
        if (!isRegister) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            }
            bluetoothAdapter.setName("Peripheral MK");

            bluetoothAdapter.getProfileProxy(context, mProfileServiceListener, BluetoothProfile.HID_DEVICE);
        }
    }

    public static boolean isConnected() {
        return HidUtils._connected;
    }

    private static void isConnected(boolean _connected) {
        HidUtils._connected = _connected;
    }

    public static BluetoothProfile.ServiceListener mProfileServiceListener = new BluetoothProfile.ServiceListener() {
        @RequiresApi(api = Build.VERSION_CODES.S)
        @Override
        public void onServiceDisconnected(int profile) {
            Log.e(TAG, "hid onServiceDisconnected");
            if (profile == BluetoothProfile.HID_DEVICE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.checkSelfPermission(cont, Manifest.permission.BLUETOOTH_CONNECT);
                }
                mHidDevice.unregisterApp();
            }
        }

        @SuppressLint("NewApi")
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.e(TAG, "hid onServiceConnected");
            bluetoothProfile = proxy;
            if (profile == BluetoothProfile.HID_DEVICE) {
                mHidDevice = (BluetoothHidDevice) proxy;
                HidConsts.HidDevice = mHidDevice;
                BluetoothHidDeviceAppSdpSettings sdp = new BluetoothHidDeviceAppSdpSettings(HidConsts.NAME, HidConsts.DESCRIPTION, HidConsts.PROVIDER, BluetoothHidDevice.SUBCLASS1_COMBO, HidConsts.Descriptor);
                if (ActivityCompat.checkSelfPermission(cont, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions((Activity) cont,new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 1001);
                }
                mHidDevice.registerApp(sdp, null, null, Executors.newCachedThreadPool(), mCallback);
            }
        }
    };
    public static final BluetoothHidDevice.Callback mCallback = new BluetoothHidDevice.Callback() {
        @Override
        public void onAppStatusChanged(BluetoothDevice pluggedDevice, boolean registered) {
            Log.e(TAG, "onAppStatusChanged: " + registered);
            isRegister = registered;
        }

        @Override
        public void onConnectionStateChanged(BluetoothDevice device, int state) {
            Log.e(TAG, "onConnectionStateChanged:" + state);
            if (state == BluetoothProfile.STATE_DISCONNECTED) {
                HidUtils.isConnected(false);
                if (connectionStateChangeListener != null) {
                    connectionStateChangeListener.onDisConnected();
                    mDevice = null;
                }
            } else if (state == BluetoothProfile.STATE_CONNECTED) {
                HidUtils.isConnected(true);
                mDevice = device;
                if (connectionStateChangeListener != null) {
                    connectionStateChangeListener.onConnected();
                }
            } else if (state == BluetoothProfile.STATE_CONNECTING) {
                if (connectionStateChangeListener != null) {
                    connectionStateChangeListener.onConnecting();
                }
            }
        }
    };
}
