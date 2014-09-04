package com.bde.ancs.androidancs;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bde.ancs.lib.AndroidANCSService;

public class MainActivity extends Activity {

	private Button mScanBtn, mConnectBtn/*, mAddNoti, mRemoveNoti, mBattery14, mBattery15*/;
	private TextView trialNumTV;
	private RelativeLayout background;
	private BluetoothDevice mDevice;
	private BluetoothDevice mOldDevice;
	private BluetoothDevice mConnectedDevice;
	private AndroidANCSService mService;
	private ProgressDialog mDialog;
	BluetoothAdapter mBluetoothAdapter;
	private final int REQUEST_ENABLE_BT = 0;
	private BluetoothStateReceiver btStateReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		background = (RelativeLayout) findViewById(R.id.background);
		background.setBackgroundResource(R.drawable.bg_disconnect);
		
		mScanBtn = (Button) findViewById(R.id.scanBtn);
		mConnectBtn = (Button) findViewById(R.id.connectBtn);
		mScanBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,
						DeviceListActivity.class);
				/*if (mDevice != null) {
					
				}*/
				startActivityForResult(intent, 1);
			}
		});
		mConnectBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!mDialog.isShowing()) {
					mDialog.show();
				}
				if (((Button) v).getText().toString().toLowerCase().equals("connect")) {
					mService.connect(mDevice);
				} else {
					mService.disconnect(mDevice);
				}
			}
		});
		
		mDialog = new ProgressDialog(this);
		mDialog.setTitle("请稍后");
		mDialog.setMessage("...操作中...");
		mDialog.setCanceledOnTouchOutside(false);
		
		btStateReceiver = new BluetoothStateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(btStateReceiver, filter);
		
		boolean isBtTurnOn = true;
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			isBtTurnOn = false;
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

		
				
		
		/*mAddNoti = (Button) findViewById(R.id.addNoti);
		mAddNoti.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				addNoti();
			}
		});
		mRemoveNoti = (Button) findViewById(R.id.removeNoti);
		mRemoveNoti.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				cancleNoti();
			}
		});
		
		mBattery14 = (Button) findViewById(R.id.battery14);
		mBattery14.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_BATTERY_CHANGED);
				//int level = intent.getIntExtra("level", 0);
				int level = 14;
				intent.putExtra("level", level);
				//电量的总刻度
				//int scale = intent.getIntExtra("scale", 100);
				int scale = 100;
				intent.putExtra("scale", scale);
				//把它转成百分比
				int percentage = (level*100)/scale;
				sendBroadcast(intent);
				if (mService != null) {
					mService.sendBatteryNotification(14);
				}
			}
		});
		
		mBattery15 = (Button) findViewById(R.id.battery15);
		mBattery15.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mService != null) {
					mService.sendBatteryNotification(15);
				}
			}
		});*/
		
		

		/*mAddNoti.setEnabled(false);
		mBattery14.setEnabled(false);
		mBattery15.setEnabled(false);
		mRemoveNoti.setEnabled(false);*/
		
		
		
		/*if (AndroidANCSService.IS_TRIAL) {
			trialNumTV = (TextView) findViewById(R.id.trialNum);
			trialNumTV.setVisibility(View.VISIBLE);
			SharedPreferences sp = getSharedPreferences("trial", Context.MODE_PRIVATE);
			
			trialNumTV.setText("今天剩余使用次数：" + (AndroidANCSService.NOTIFICATION_PER_DAY - sp.getInt("currentNotificationNum", 1)));
			mService.setCallback(new AndroidANCSService.Callback() {

				@Override
				public void trialTime(int trialNum) {
					// TODO Auto-generated method stub
					trialNumTV.setText("今天剩余使用次数：" + (AndroidANCSService.NOTIFICATION_PER_DAY - trialNum));
				}
				
			});
		}*/
		if (isBtTurnOn) {
			setUpBt();
		}
	}

	private boolean hasSetUpBt = false;
	private void setUpBt() {
		if (hasSetUpBt) {
			AndroidANCSService.reset();
		} else {
			SharedPreferences sp = MainActivity.this.getSharedPreferences(PAIRED_DEVICE, Context.MODE_PRIVATE);
			String address = sp.getString(DEVICE_ADDRESS, "");
			if (!address.equals("")) {
				mOldDevice = mBluetoothAdapter.getRemoteDevice(address);
			}
		}
		hasSetUpBt = true;
		mService = AndroidANCSService.getInstance(this);
		mService.setHandler(mHandler);
		if (mOldDevice != null) {
			isScanStarted = true;
			mDevice = mOldDevice;
			mBluetoothAdapter.startLeScan(callback);
			
		}
		//mService.getAllContacts();
	}
	private LeScanCallback callback = new LeScanCallback() {
		
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			// TODO Auto-generated method stub
			if (device.getAddress().equals(mOldDevice.getAddress()) && mService != null) {		
				mBluetoothAdapter.stopLeScan(callback);
				mService.connect(device);
				isScanStarted = false;
			}
		}
	};

	public static final String PAIRED_DEVICE = "paired device";
	public static final String DEVICE_ADDRESS = "device address";
	private static boolean CONNECT_TO_NEW_DEVICE = false;
	boolean isScanStarted = false;
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case AndroidANCSService.CONNECTED:
				mConnectBtn.setText("Disconnect");
				mConnectedDevice = mDevice;
				if (isScanStarted) {
					mBluetoothAdapter.stopLeScan(callback);
					isScanStarted = false;
				}
				background.setBackgroundResource(R.drawable.bg_connected);
				if (mOldDevice == null || (mOldDevice != null && !mOldDevice.getAddress().equals(mDevice.getAddress()))) {
					SharedPreferences sp = MainActivity.this.getSharedPreferences(PAIRED_DEVICE, Context.MODE_PRIVATE);
					sp.edit().putString(DEVICE_ADDRESS, mDevice.getAddress()).commit();
					mOldDevice = mBluetoothAdapter.getRemoteDevice(mDevice.getAddress());
				}
				break;
			case AndroidANCSService.DISCONNECTED:
				if (mDialog.isShowing()) {
					mDialog.dismiss();
				}
				background.setBackgroundResource(R.drawable.bg_disconnect);
				mConnectBtn.setText("Connect");
				mConnectedDevice = null;
				
				if (CONNECT_TO_NEW_DEVICE) {
					CONNECT_TO_NEW_DEVICE = false;
					mService.connect(mDevice);
				} else {
					if (mOldDevice != null) {
						//BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
						isScanStarted = true;
						mBluetoothAdapter.startLeScan(callback);
					}
				}
				
				//mScanBtn.setEnabled(true);
				//mConnectBtn.setClickable(true);
				/*mAddNoti.setEnabled(false);
				mBattery14.setEnabled(false);
				mBattery15.setEnabled(false);
				mRemoveNoti.setEnabled(false);*/
				break;
			case AndroidANCSService.NOTIFICATION_ENABLED:
				if (mDialog.isShowing()) {
					mDialog.dismiss();
				}
				Toast.makeText(MainActivity.this, "Notification enable success", Toast.LENGTH_SHORT).show();
				//mConnectBtn.setText("Disconnect");
				//mScanBtn.setEnabled(false);
				//mConnectBtn.setClickable(false);
				/*mAddNoti.setEnabled(true);
				mBattery14.setEnabled(true);
				mBattery15.setEnabled(true);
				mRemoveNoti.setEnabled(true);*/
				break;
			}
		}

	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		// super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_ENABLE_BT:
			if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();
                mBluetoothAdapter = ((BluetoothManager)getSystemService(
                		Context.BLUETOOTH_SERVICE)).getAdapter();
                setUpBt();

            } else {
                // User did not enable Bluetooth or an error occurred
                //Log.d(TAG, "BT not enabled");
                Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                finish();
            }
			break;
		case 1:

			if (resultCode == Activity.RESULT_OK && data != null) {
				mDialog.show();
				String deviceAddress = data
						.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
				mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(
						deviceAddress);
				// Log.d(TAG, "... onActivityResultdevice.address==" + mDevice +
				// "mserviceValue" + mService);
				// updateUi();
				// mService.connect(mDevice, false);
				if (!mDialog.isShowing()) {
					mDialog.show();
				}
				if (mConnectedDevice != null) {
					CONNECT_TO_NEW_DEVICE = true;
					mService.disconnect(mConnectedDevice);
				} else {
					if (isScanStarted) {
						mBluetoothAdapter.stopLeScan(callback);
					}
					mService.connect(mDevice);
				}
				
			}
			break;
		}
	}

	int ids = 0;

	void addNoti() {
		Notification.Builder builder = new Notification.Builder(
				MainActivity.this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("a").setContentText("c")
				.setTicker("13527852905");
		NotificationManager m = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		m.notify(ids++, builder.build());
	}

	void cancleNoti() {
		NotificationManager m = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		m.cancelAll();
		ids = 0;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		System.out.println("onDestroy");
		mDialog.dismiss();
		mService.close();
		unregisterReceiver(btStateReceiver);
	}

	private void incommingCall() {
		Notification.Builder builder = new Notification.Builder(
				MainActivity.this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("a").setContentText("c");
		NotificationManager m = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		m.notify(ids++, builder.build());
	}
	boolean isStateChanged = false;
	private class BluetoothStateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
			switch (state) {
			case BluetoothAdapter.STATE_TURNING_ON:
				break;
			case BluetoothAdapter.STATE_ON:
				break;
			case BluetoothAdapter.STATE_TURNING_OFF:
				break;
			case BluetoothAdapter.STATE_OFF:
				background.setBackgroundResource(R.drawable.bg_disconnect);
				mConnectBtn.setText("Connect");
				mConnectedDevice = null;
				
				final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
				mBluetoothAdapter = bluetoothManager.getAdapter();
				if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
					Intent enableBtIntent = new Intent(
							BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
				}
				break;
			}
		}
		
	}
}
