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

package com.bde.ancs.androidancs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceListActivity extends Activity {
    private BluetoothAdapter mBtAdapter;
    //private TextView mEmptyList;
    public static final String TAG = "DeviceListActivity";
    //private HRPService mService = null;
    List<BluetoothDevice> deviceList;
    private DeviceAdapter deviceAdapter;
    public int somevalue = 10;
    //private ServiceConnection onService = null;
    Map<String, Integer> devRssiValues;

    /*private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case HRPService.GATT_DEVICE_FOUND_MSG:
                Bundle data = msg.getData();
                final BluetoothDevice device = data.getParcelable(BluetoothDevice.EXTRA_DEVICE);
                final int rssi = data.getInt(HRPService.EXTRA_RSSI);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addDevice(device, rssi);
                    }
                });
                break;
            default:
                super.handleMessage(msg);
            }
        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);

        getWindow().setBackgroundDrawableResource(R.drawable.rect);
        
        final BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = manager.getAdapter();
        //mEmptyList = (TextView) findViewById(R.id.empty);
        //noDevicesTv = (TextView) findViewById(R.id.no_devices);
        Button cancelButton = (Button) findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        populateList();
    }
    ListView newDevicesListView;
    private void populateList() {
        /* Initialize device list container */
        Log.d(TAG, "populateList");
        deviceList = new ArrayList<BluetoothDevice>();
        deviceAdapter = new DeviceAdapter(this, deviceList);
        devRssiValues = new HashMap<String, Integer>();

        newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setBackgroundResource(R.drawable.rect);
        newDevicesListView.setAdapter(deviceAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);
        
        TextView emptyView = (TextView) findViewById(R.id.empty);
        newDevicesListView.setEmptyView(emptyView);

        /*
        Return the set of BluetoothDevice objects that are bonded (paired) to the local adapter. 
		If Bluetooth state is not STATE_ON, this API will return an empty set. 
		After turning on Bluetooth, wait for ACTION_STATE_CHANGED with STATE_ON to get the updated value. 
         */
        /*Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        for (BluetoothDevice pairedDevice : pairedDevices) {
            boolean result = false;
            result = mService.isBLEDevice(pairedDevice);
            if (result == true) {
                addDevice(pairedDevice, 0);
            }
        }*/

        /*mService.scan(true);*/
        mBtAdapter.startLeScan(callback);
    }

    private BluetoothAdapter.LeScanCallback callback = new BluetoothAdapter.LeScanCallback() {
		
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			// TODO Auto-generated method stub
			final BluetoothDevice finalDevice = device;
            final int finalRssi = rssi;
            /*if (device.getName().contains("TKO")) {
            	rssi = rssi + 1;
            	rssi = rssi - 1;
            }*/
            //if (checkIfHID(scanRecord)) {
            if (device.getName().contains("TKO")) {
            	/*if (deviceList.size() == 0) {
                    noDevicesTv.setVisibility(View.GONE);
                    newDevicesListView.setVisibility(View.VISIBLE);
            	}*/
	            runOnUiThread(new Runnable() {
	                @Override
	                public void run() {
	                    addDevice(finalDevice, finalRssi);
	                }
	            });
            }
		}
	};
	private boolean checkIfHID(byte[] scanRecord) {
		int index = 0;
		int len = 0;
		int flag = 0;
		while (index < 31) {
			len = scanRecord[index++];
			flag = scanRecord[index++];
			if (len >= 3 && flag == 2) {
				if (scanRecord[index++] == 0x12 && scanRecord[index++] == 0x18) {
					return true;
				}
			} else {
				index += len;
			}
		}
		return false;
	}
    private void addDevice(BluetoothDevice device, int rssi) {
        boolean deviceFound = false;

        for (BluetoothDevice listDev : deviceList) {
            if (listDev.getAddress().equals(device.getAddress())) {
            	//֮ǰ��ɨ���Ѿ�ɨ���������豸���ѱ�־��Ϊ�棬�Ͳ���Ҫ�������deviceList����
                deviceFound = true;
                break;
            }
        }
        devRssiValues.put(device.getAddress(), rssi);
        if (!deviceFound) {
            //mEmptyList.setVisibility(View.GONE);
            deviceList.add(device);
            deviceAdapter.notifyDataSetChanged();
        }
    }

    TextView noDevicesTv;
    @Override
    public void onStart() {
        super.onStart();
        //Log.d(TAG, "onStart mService= " + mService);

        //The local Bluetooth adapter has finished the device discovery process
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //The state of the local Bluetooth adapter has been changed. 
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(mReceiver, filter);
        
        /*if (deviceList.size() == 0) {
        	noDevicesTv.setVisibility(View.VISIBLE);
        	newDevicesListView.setVisibility(View.VISIBLE);
        }*/
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unbindService(onService);
        //mService.scan(false);
        mBtAdapter.stopLeScan(callback);
    }

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice device = deviceList.get(position);
            /*if (mService.mBluetoothGatt.getConnectionState(device) == mService.mBluetoothGatt.STATE_CONNECTED){
                Log.i(TAG, "connected devcie");
                showMessage("device already connected");
                return;
            }*/
            //mService.scan(false);
            mBtAdapter.stopLeScan(callback);

            Bundle b = new Bundle();
            b.putString(BluetoothDevice.EXTRA_DEVICE, device.getAddress());

            Intent result = new Intent();
            result.putExtras(b);

            setResult(Activity.RESULT_OK, result);
            finish();
        }
    };

    /**
     * The BroadcastReceiver that listens for discovered devices and changes the
     * title when discovery is finished.
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (deviceList.size() == 0) {
                    //mEmptyList.setText(R.string.no_ble_devices);
                }
            }
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                if (!mBtAdapter.isEnabled())
                    finish();
            }
        }
    };

    class DeviceAdapter extends BaseAdapter {
        Context context;
        List<BluetoothDevice> devices;
        LayoutInflater inflater;

        public DeviceAdapter(Context context, List<BluetoothDevice> devices) {
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.devices = devices;
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup vg;

            if (convertView != null) {
                vg = (ViewGroup) convertView;
            } else {
                vg = (ViewGroup) inflater.inflate(R.layout.device_element, null);
            }

            BluetoothDevice device = devices.get(position);
            final TextView tvadd = ((TextView) vg.findViewById(R.id.address));
            final TextView tvname = ((TextView) vg.findViewById(R.id.name));
            final TextView tvpaired = (TextView) vg.findViewById(R.id.paired);
            final TextView tvrssi = (TextView) vg.findViewById(R.id.rssi);

            tvrssi.setVisibility(View.VISIBLE);
            byte rssival = (byte) devRssiValues.get(device.getAddress()).intValue();
            if (rssival != 0) {
                tvrssi.setText("Rssi = " + String.valueOf(rssival));
            }

            tvname.setText(device.getName());
            tvadd.setText(device.getAddress());
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                tvname.setTextColor(Color.GRAY);
                tvadd.setTextColor(Color.GRAY);
                tvpaired.setTextColor(Color.GRAY);
                tvpaired.setVisibility(View.VISIBLE);
                tvrssi.setVisibility(View.GONE);
            } else {
                tvname.setTextColor(Color.BLACK);
                tvadd.setTextColor(Color.BLACK);
                tvpaired.setVisibility(View.GONE);
                tvrssi.setVisibility(View.VISIBLE);
                tvrssi.setTextColor(Color.BLACK);
            }
            /*if (mService.mBluetoothGatt.getConnectionState(device) == mService.mBluetoothGatt.STATE_CONNECTED){
                Log.i(TAG, "connected device::"+device.getName());
                tvname.setTextColor(Color.WHITE);
                tvadd.setTextColor(Color.WHITE);
                tvpaired.setVisibility(View.VISIBLE);
                tvpaired.setText("connected");
                tvrssi.setVisibility(View.GONE);
            }*/
            return vg;
        }
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
