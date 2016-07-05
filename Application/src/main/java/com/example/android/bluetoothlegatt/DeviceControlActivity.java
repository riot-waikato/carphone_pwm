/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity implements SensorEventListener {
    public CarmoverStub carmover = new CarMover();

    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private boolean readyToSend = false;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private int statusA = 0;
    private int statusB = 0;

    private final int RESOLUTION = 50;
    private final int KEEPALIVE = 970;
    //////////////////////////////////DIRECTION VALUES
    private final int STOP = 0;
    private final int FORWARD_LOW = 1;
    private final int FORWARD_MID = 2;
    private final int FORWARD_HIGH = 3;
    private final int BACKWARD_LOW = 4;
    private final int BACKWARD_MID = 5;
    private final int BACKWARD_HIGH = 6;
    private final int LEFT_HALF = 1;
    private final int LEFT_FULL = 2;
    private final int RIGHT_HALF = 3;
    private final int RIGHT_FULL = 4;

    private int multiplier = 14;

    private static char[] chars = new char[]  {'0', '0'};
    private static char[] chars_last = new char[] {0, 0};
    //////////////////////////////////END_DIR

    private SensorManager sensorManager;
    private Sensor Accelerometer;

    private static final int SENSOR_DELAY = 200000; //delay in microseconds - this gives us silky smooth 5fps

    private long lastUpdate = System.currentTimeMillis();
    private long curTime = System.currentTimeMillis();

    private float[] Sensor_Readings = new float[] {0, 0, 0}; //X, Y, Z - Maybe this could be done with a vector?


    private int backcolor = 0xffa500;
    private int slidecolor = 0xff0000;
    private int forecolor = 0xffa500;

    private SharedPreferences sharedprefs;

    private String backkey = "background_color";
    private String slidekey = "slide_color";
    private String indicatorkey = "indicator_color ";
    private String prefskey = "CARPHONEPREFS";


    private int seperate = 0;
    private int invert_disp = 1;
    private int invert_cont = 1;
    private String sepkey = "seperate";
    private String invdispkey = "invdispay";
    private String invcontkey = "invcontol";
    private boolean sepchanged = false;
    private boolean dispchanged = false;
    private boolean contchanged = false;


    private void setPrefs() {
        SharedPreferences.Editor editor = sharedprefs.edit();
        backcolor = Integer.parseInt(sharedprefs.getString(backkey, Integer.toString(backcolor)));
        slidecolor = Integer.parseInt(sharedprefs.getString(slidekey, Integer.toString(slidecolor)));
        forecolor = Integer.parseInt(sharedprefs.getString(indicatorkey, Integer.toString(forecolor)));

        seperate = Integer.parseInt(sharedprefs.getString(sepkey, Integer.toString(0)));
        invert_disp = Integer.parseInt(sharedprefs.getString(invdispkey, Integer.toString(1)));
        invert_cont = Integer.parseInt(sharedprefs.getString(invcontkey, Integer.toString(1)));

        setColors();
    }

    private void setColors() {
        ImageView circle = (ImageView) findViewById(R.id.indicator_updown);
        ImageView circle2 = (ImageView) findViewById(R.id.indicator_leftright);
        ImageView redSquare = (ImageView) findViewById(R.id.redsquare_updown);
        ImageView redSquare2 = (ImageView) findViewById(R.id.redsquare_leftright);
        ImageView background = (ImageView) findViewById(R.id.background_image);

        background.setBackgroundColor(backcolor);

        circle.setBackgroundColor(slidecolor);
        circle2.setBackgroundColor(slidecolor);
        redSquare.setBackgroundColor(forecolor);
        redSquare2.setBackgroundColor(forecolor);

        //RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.controlmaster);
        //relativeLayout.setBackgroundColor(backcolor);
        System.out.println(Integer.toHexString(backcolor));
        //this.getWindow().getDecorView().setBackgroundColor(backcolor); //-enable this to break EVERTHING
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
            System.out.println("%%%%%%%% We KNOW we've connected here...");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    public void onSensorChanged(SensorEvent e) {
        Sensor sensor = e.sensor;
        if(sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            curTime = System.currentTimeMillis();
            if(curTime - lastUpdate >= RESOLUTION) {
                for (int i = 0; i < 3; i++)
                    Sensor_Readings[i] = e.values[i];

                ImageView circle = (ImageView) findViewById(R.id.indicator_updown);
                ImageView circle2 = (ImageView) findViewById(R.id.indicator_leftright);
                ImageView redSquare = (ImageView) findViewById(R.id.redsquare_updown);
                ImageView redSquare2 = (ImageView) findViewById(R.id.redsquare_leftright);

                TextView textForward = (TextView) findViewById(R.id.forward_char);
                TextView textBackward = (TextView) findViewById(R.id.backward_char);
                TextView textLeft = (TextView) findViewById(R.id.left_char);
                TextView textRight = (TextView) findViewById(R.id.right_char);

                String outputString = "Current time:  " + curTime + "\nLast update:  " + lastUpdate +
                        "\nY:  " + (-1 * Sensor_Readings[0]) + "\nX:  " + Sensor_Readings[1] +
                        "\nZ:  " + Sensor_Readings[2];

                carmover.setChars(new char[] {'0', '0'});
                carmover.setY(-1 * Sensor_Readings[0]*invert_cont);
                carmover.setX(Sensor_Readings[1]);
                carmover.setZ(Sensor_Readings[2]); //not used, but potentially helpful - pythag to detect forceful movement, etc
                carmover.Act50ms();
                chars = carmover.getChars();


                if(chars_last[0] != chars[0] || chars_last[1] != chars[1]) { //only update if we really need to
                    if (mBluetoothLeService != null) {
                        for (int i = 0; i < chars.length; i++) {
                            mBluetoothLeService.writeCustomCharacteristic((int) chars[1 - i], 1 - i);
                        }
                        lastUpdate = curTime;
                    }
                    chars_last[0] = chars[0];
                    chars_last[1] = chars[1];
                }
                else if (curTime - lastUpdate >= KEEPALIVE) //send a keepalive every second or so if no updates
                    if (mBluetoothLeService != null) {
                        for (int i = 0; i < chars.length; i++) {
                            mBluetoothLeService.writeCustomCharacteristic((int) chars[1 - i], 1 - i);
                        }
                        lastUpdate = curTime;
                    }




                /////////////////////////layout things
                DisplayMetrics displayMetrics = new DisplayMetrics();
                WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE); // the results will be higher than using the activity context object or the getWindowManager() shortcut
                wm.getDefaultDisplay().getMetrics(displayMetrics);
                int screenWidth = displayMetrics.widthPixels;
                int screenHeight = displayMetrics.heightPixels;

                int offsetX = (int)(multiplier * Sensor_Readings[0]*invert_disp);
                int offsetY = (int)(multiplier * Sensor_Readings[1]);

                //dimensions:
                //circle should be ~128
                //boundaries: 3 5 7 | 2 4 6 | 2.5 4.2
                //multiplier: 15
                //this gives us 45 75 105 | 30 60 90 | 37.7 63

                int disp = 0;
                int size_orange = 64;
                redSquare.layout(screenWidth/2 - 64 - seperate, screenHeight/2 -256, screenWidth/2 + 64 - seperate, screenHeight/2 + 128);

                ////text
                textForward.setX(screenWidth/2 - 6 - seperate);
                textForward.setY(screenHeight/2 - 296);

                textBackward.setX(screenWidth/2 - 6 -seperate);
                textBackward.setY(screenHeight/2 + 128);

                textLeft.setY(screenHeight/2 - 64 - 18);
                textLeft.setX(screenWidth/2 - 224 + seperate);

                textRight.setY(screenHeight/2 - 64 - 18);
                textRight.setX(screenWidth/2 + 224 - 20 + seperate);
                ////text

                circle.layout(screenWidth/2 - size_orange - seperate,
                        screenHeight/2 - 2*size_orange + offsetX,
                        screenWidth/2 + size_orange - seperate,
                        screenHeight/2 + offsetX);

                circle2.layout(screenWidth/2 - size_orange + offsetY + seperate,
                        screenHeight/2 - 2*size_orange,
                        screenWidth/2 + size_orange+ offsetY + seperate,
                        screenHeight/2 );


                redSquare2.layout(screenWidth/2 - 194 + seperate, screenHeight/2 -128, screenWidth/2 + 194 + seperate, screenHeight/2);

                ViewGroup vg = (ViewGroup)findViewById(R.id.controlmaster);
                vg.invalidate();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //do nothing
    }
    //
    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothLeService.readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);
                        }
                        return true;
                    }
                    return false;
                }
    };

    private void clearUI() {
        //mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        if(mDataField != null)
            mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.button_control);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        mDataField = (TextView) findViewById(R.id.data_value);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, Accelerometer, SensorManager.SENSOR_DELAY_UI); //may try swapping out with SENSOR_DELAY
        System.out.println("***** Sensor manager registered");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        sharedprefs = getApplicationContext().getSharedPreferences(prefskey, Context.MODE_PRIVATE);
        setPrefs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
        sensorManager.registerListener(this, Accelerometer, SensorManager.SENSOR_DELAY_UI); //may try swapping out with SENSOR_DELAY
        System.out.println("***** Sensor manager registered");

        setPrefs();
        sharedprefs = getApplicationContext().getSharedPreferences(prefskey, Context.MODE_PRIVATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        sensorManager.unregisterListener(this);
        System.out.println("***** Sensor manager unregistered");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);

        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                onBackPressed();
                return true;
            case R.id.preferences:
                Intent intent = new Intent();
                intent.setClassName(this, "com.example.android.bluetoothlegatt.PrefsActivity");
                startActivity(intent);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
