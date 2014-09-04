package com.bde.ancs.lib;



import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.ByteArrayBuffer;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.bde.ancs.androidancs.R;
//import android.service.notification.StatusBarNotification;

public class AndroidANCSService/* extends Service*/ {

	private final static String TAG = "AndroidANCSService";
	public UUID ANCS_SERVICE = UUID.fromString("7905F431-B5CE-4E99-A40F-4B1E122D00D0");
	public UUID ANCS_NOTIFICATION_SOURCE = UUID.fromString("9FBF120D-6301-42D9-8C58-25E699A21DBD");
	public UUID ANCS_CONTROL_POINT = UUID.fromString("69D1D8F3-45E1-49A8-9821-9BBDFDAAD9D9");
	public UUID ANCS_DATA_SOURCE = UUID.fromString("22EAC6E9-24D6-4BB5-BE44-B36ACE7C7BFB");
	public UUID CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
	
	public UUID BATTERY_SERVICE = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
	public UUID BATTERY_LEVEL = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
	
	public UUID CURRENT_TIME_SERVICE = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb");
	public UUID CURRENT_TIME = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb");
	public UUID LOCAL_TIME_INFO = UUID.fromString("00002a0f-0000-1000-8000-00805f9b34fb");
	public UUID REFERENCE_TIME_INFO = UUID.fromString("00002a14-0000-1000-8000-00805f9b34fb");
	
	public static final int CONNECTED = 0;
	public static final int DISCONNECTED = 1;
	public static final int NOTIFICATION_ENABLED = 2;
	//private ArrayList<StatusBarNotification> notifications = new ArrayList<StatusBarNotification>();
	private ArrayList<ANCSNotification> notifications = new ArrayList<ANCSNotification>();
	private Context mContext;
	private BluetoothGattServer mGattServer;
	//private ArrayList<BluetoothGattCharacteristic> mCharacteristics = new ArrayList<BluetoothGattCharacteristic>();
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mDevice;
	private BroadcastReceiver mReceiver = new NotificationReceiver();
	private BroadcastReceiver mPhoneStateReceiver = new PhoneStateReceiver();
	/*private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			//super.handleMessage(msg);
			// get command from control point
			System.out.println("Get command from control point...");
			byte[] value;
			try {
				value = doCommand((byte[]) msg.obj);
				BluetoothGattCharacteristic charac = mGattServer.getService(ANCS_SERVICE).getCharacteristic(ANCS_DATA_SOURCE);
				//BluetoothGattCharacteristic charac = mCharacteristics.get(1);
				if (charac == null) {
					System.out.println("charc is null,it't not send notification to device");
					return;
				}
				System.out.println("value is " + ret);
				StringBuilder value = new StringBuilder(ret.length);
				for (byte b : ret) {
					value.append(b);
				}
				System.out.println(value.toString());
				//charac.setValue(ret);

				//mGattServer.notifyCharacteristicChanged(mDevice, charac, false);
				try {
					if (!gattServerNotifyCharacteristicChanged(mDevice, charac, value, false)) {
						System.out.println("notify failed...");
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	};*/
	
	public boolean gattServerNotifyCharacteristicChanged(BluetoothDevice device, BluetoothGattCharacteristic characteristic, byte []value, boolean confirm) throws IOException {
		//byte []value = characteristic.getValue();
		if (mGattServer == null || value == null) {
			return false;
		}
		
		int sended = 0;

		ByteArrayEntity bae = new ByteArrayEntity(value);
		InputStream in = bae.getContent();
		
		while (sended < value.length) {
			byte []dataPerTime;
			if (value.length - sended > 20) {
				dataPerTime = new byte[20];
			} else {
				dataPerTime = new byte[value.length - sended];
			}
			
			in.read(dataPerTime);
			characteristic.setValue(dataPerTime);
			
			if (!(mGattServer.notifyCharacteristicChanged(device, characteristic, confirm))) {
				return false;
			}
			sended += dataPerTime.length;
		}
		return true;
		
		/*characteristic.setValue(value);
		return mGattServer.notifyCharacteristicChanged(device, characteristic, confirm);*/
	}
	/*public boolean addNotificationToArray(StatusBarNotification sbn) {
		return notifications.add(sbn);
	}
	public boolean removeNotificationFromArray (StatusBarNotification sbn){
		return notifications.remove(sbn);
	}*/
	public boolean addNotificationToArray(ANCSNotification sbn) {
		if (sbn.getPostTime() == MISSING_CALL_POST_TIME) {
			int i = 0;
			i++;
		}
		if (notifications.size() > 0 && notifications.get(notifications.size() - 1).getPostTime()
				== INCOMMING_CALL_POST_TIME) {
			//当前只能有一个incomming call notification
			System.out.println("add failed");
			return false;
		}
		return notifications.add(sbn);
	}
	public boolean removeNotificationFromArray (ANCSNotification sbn){
		if (!notifications.remove(sbn)) {
			ANCSNotification ANCSNotificationToDelete = null;
			for (ANCSNotification noti : notifications) {
				if (noti.getPostTime() == sbn.getPostTime()) {
					ANCSNotificationToDelete = noti;
				}
			}
			if (ANCSNotificationToDelete != null) {
				return notifications.remove(ANCSNotificationToDelete);
			}
		}
		return true;
	}
	public static int statusOk = 0x0000;
	public static int statusNotOk = 0x0006;
	private BluetoothGattServerCallback mServerCallback = new BluetoothGattServerCallback() {

		private BluetoothGattCharacteristic getGattServerCharacteristicFromCharacteristic(BluetoothGattCharacteristic characteristic) {
			UUID serviceUuid = characteristic.getService().getUuid();
			UUID characUuid = characteristic.getUuid();
			BluetoothGattService service = mGattServer.getService(serviceUuid);
			if (service == null) {
				System.out.println("Service is null...so characteristic write failed");
				return null;
			}
			BluetoothGattCharacteristic charac = service.getCharacteristic(characUuid);
			/*if (charac == null) {
				System.out.println("Characteristic is null...so characteristic write failed");
				return null;
			}*/
			return charac;
		}
		private BluetoothGattDescriptor getGattServerDescriptorFromDescriptor(BluetoothGattDescriptor descriptor) {
			UUID serviceUuid = descriptor.getCharacteristic().getService().getUuid();
			UUID characUuid = descriptor.getCharacteristic().getUuid();
			UUID descriptorUuid = descriptor.getUuid();
			BluetoothGattService service = mGattServer.getService(serviceUuid);
			if (service == null) {
				System.out.println("Service is null...so characteristic write failed");
				return null;
			}
			BluetoothGattCharacteristic charac = service.getCharacteristic(characUuid);
			if (charac == null) {
				System.out.println("Characteristic is null...so characteristic write failed");
				return null;
			}
			BluetoothGattDescriptor desc = charac.getDescriptor(descriptorUuid);
			return desc;
		}
		@Override
		public void onCharacteristicReadRequest(BluetoothDevice device,
				int requestId, int offset,
				BluetoothGattCharacteristic characteristic) {
			// TODO Auto-generated method stub
			//byte []value = null;
			System.out.println("onCharacteristicReadRequest");
			BluetoothGattCharacteristic charac = getGattServerCharacteristicFromCharacteristic(characteristic);
			//final int currentTimeServiceHashCode = CURRENT_TIME_SERVICE.hashCode();
			if (charac.getUuid().equals(CURRENT_TIME)) {
				byte []data = getCurrentTimeData();
				charac.setValue(data);
			}
			if (charac == null || offset != 0) {
				mGattServer.sendResponse(device, requestId, statusNotOk, offset, null);
				return;
			}
			mGattServer.sendResponse(device, requestId, statusOk, offset, charac.getValue());
		}

		@Override
		public void onCharacteristicWriteRequest(BluetoothDevice device,
				int requestId, BluetoothGattCharacteristic characteristic,
				boolean preparedWrite, boolean responseNeeded, int offset,
				byte[] value) {
			// TODO Auto-generated method stub
			System.out.println("onCharacteristicWriteRequest");
			/*UUID serviceUuid = characteristic.getService().getUuid();
			UUID characUuid = characteristic.getUuid();
			BluetoothGattService service = mGattServer.getService(serviceUuid);
			if (service == null) {
				System.out.println("Service is null...so characteristic write failed");
				return;
			}
			BluetoothGattCharacteristic charac = service.getCharacteristic(characUuid);
			if (charac == null) {
				System.out.println("Characteristic is null...so characteristic write failed");
				return;
			}*/
			BluetoothGattCharacteristic charac = getGattServerCharacteristicFromCharacteristic(characteristic);
			int status = statusNotOk;
			if (charac != null && offset == 0) {
				charac.setValue(value);
				status = statusOk;
			}
			if (responseNeeded) {
				System.out.println("sendResponse...");
				mGattServer.sendResponse(mDevice, requestId, status, offset, value);
			}
			//isWritten = true;
			/*for (BluetoothGattCharacteristic every : mCharacteristics) {
				if (every.getUuid().equals(characteristic.getUuid())) {
					if (offset == 0) {
						System.out.println("found Characteristic...");
						every.setValue(value);
						isWritten = true;
						break;
					}
				}
			}*/
			/*if (responseNeeded) {
				mGattServer.sendResponse(mDevice, requestId, statusOk, offset, value);
			}*/
			System.out.println("status == statusOk ? " + (status == statusOk));
			System.out.println("characteristic.getUuid().equals(ANCS_CONTROL_POINT) ? " + (characteristic.getUuid().equals(ANCS_CONTROL_POINT)));
			/*System.out.println("ccc's value is ");
			sysoutByte(charac.getDescriptor(CCC).getValue());*/
			//System.out.println("descriptor == enableNotification ? " + (byteArrayEquals(charac.getDescriptor(CCC).getValue(), BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)));
			if (status == statusOk && characteristic.getUuid().equals(ANCS_CONTROL_POINT) ) {
				/*BluetoothGattCharacteristic dataSource = getCharacteristic(ANCS_SERVICE, ANCS_DATA_SOURCE);
				if (dataSource == null || byteArrayEquals(dataSource.getDescriptor(CCC).getValue(), BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
					System.out.println("no dataSource notification");
					return;
				}*/
				System.out.println("Control point");
				Message msg = Message.obtain(mHandler);
				//Bundle data = new Bundle();
				//data.putByteArray("Command", value);
				//msg.obj = charac;
				//msg.setData(data);
				msg.what = EVENT_CONTROL_POINT;
				msg.obj = value;
				msg.sendToTarget();
				
				System.out.println("control point value is ");
				sysoutByte(value);
			}
			
			/*super.onCharacteristicWriteRequest(device, requestId, characteristic,
					preparedWrite, responseNeeded, offset, value);*/
			/*if (responseNeeded) {
				mGattServer.sendResponse(mDevice, requestId, statusOk, offset, value);
			}*/
		}

		@Override
		public void onConnectionStateChange(BluetoothDevice device, int status,
				int newState) {
			// TODO Auto-generated method stub
			System.out.println("onConnectionStateChange");
			if (newState == BluetoothGatt.STATE_CONNECTED) {
				System.out.println("connected...");
				Message msg = Message.obtain(mActivityHandler, CONNECTED);
				
				mDevice = device;
				System.out.println("mDevice is " + mDevice);
				isConnected = true;
				msg.sendToTarget();
				/*mActivityHandler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						Log.i(TAG, "sending time notification" );
						long time = new Date().getTime();
						long nextSecondTime = ((time / 1000) + 1) * 1000;
						sendTimeNotification();
						mActivityHandler.postDelayed(this, nextSecondTime - time);
					}
					
				});*/
				
				
				/*SharedPreferences sharedPreferences = mContext.getSharedPreferences("ANCSConnectedDeviceAddress", Context.MODE_WORLD_READABLE);
				Editor editor = sharedPreferences.edit();//获取编辑器
				//editor.clear();
				editor.putString("Address", mDevice.getAddress());
				editor.commit();//提交修改
				sharedPreferences.edit().putString("Address", mDevice.getAddress()).commit();*/
				
				
			} else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
				mDevice = null;
				Message msg = Message.obtain(mActivityHandler, DISCONNECTED);
				
				System.out.println("disconnected...");
				System.out.println("mDevice is null");
				isConnected = false;
				/*SharedPreferences sharedPreferences = mContext.getSharedPreferences("ANCSConnectedDeviceAddress", Context.MODE_MULTI_PROCESS);
				Editor editor = sharedPreferences.edit();//获取编辑器
				editor.clear();
				//editor.putString("Address", mDevice.getAddress());
				editor.commit();//提交修改
*/				
				msg.sendToTarget();
			}
		}

		@Override
		public void onDescriptorReadRequest(BluetoothDevice device,
				int requestId, int offset, BluetoothGattDescriptor descriptor) {
			// TODO Auto-generated method stub
			System.out.println("onDescriptorReadRequest");
			//byte []value = null;
			//int status = statusOk;
			BluetoothGattDescriptor desc = getGattServerDescriptorFromDescriptor(descriptor);
			if (desc == null || offset != 0) {
				//status = statusNotOk;
				mGattServer.sendResponse(mDevice, requestId, statusNotOk, offset, null);
				return;
			}
			mGattServer.sendResponse(mDevice, requestId, statusOk, offset, desc.getValue());
			/*for (BluetoothGattCharacteristic every : mCharacteristics) {
				if (every.getUuid().equals(descriptor.getCharacteristic().getUuid())) {
					if (offset == 0) {
						System.out.println("found Characteristic...");
						value = every.getDescriptor(descriptor.getUuid()).getValue();
						((Activity) mContext).runOnUiThread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								Toast.makeText(mContext, "Notification Enable success", Toast.LENGTH_SHORT).show();
							}
							
						});
						
						//isWritten = true;
						break;
					}
				}
			}*/
			/*if (mGattServer != null) {
				mGattServer.sendResponse(mDevice, requestId, statusOk, offset, value);
			}*/
		}

		
		@Override
		public void onDescriptorWriteRequest(BluetoothDevice device,
				int requestId, BluetoothGattDescriptor descriptor,
				boolean preparedWrite, boolean responseNeeded, int offset,
				byte[] value) {
			// TODO Auto-generated method stub
			/*super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite,
					responseNeeded, offset, value);*/
			System.out.println("onDescriptorWriteRequest");
			//boolean isWritten = false;
			
			/*BluetoothGattService service = mGattServer.getService(descriptor.getCharacteristic().getService().getUuid());
			if (service == null) {
				System.out.println("service is null");
				return;
			}
			BluetoothGattCharacteristic charac = service.getCharacteristic(descriptor.getCharacteristic().getUuid());
			if (charac == null) {
				System.out.println("characteristic is null");
				return;
			}*/
			BluetoothGattDescriptor desc = getGattServerDescriptorFromDescriptor(descriptor);/*charac.getDescriptor(descriptor.getUuid());*/
			int status = statusOk;
			if (desc == null || offset != 0) {
				System.out.println("descriptor is null");
				status = statusNotOk;
				//return;
			} else {
				desc.setValue(value);
				Message msg = Message.obtain(mActivityHandler, NOTIFICATION_ENABLED);
				msg.sendToTarget();
				System.out.println("descriptor value is ");
				sysoutByte(value);
				//status = statusOk;
			}
			
			//isWritten = true;
			
			/*for (BluetoothGattCharacteristic every : mCharacteristics) {
				if (every.getUuid().equals(descriptor.getCharacteristic().getUuid())) {
					if (offset == 0) {
						System.out.println("found Characteristic...");
						every.getDescriptor(descriptor.getUuid()).setValue(value);
						isWritten = true;
						Message msg = Message.obtain(mActivityHandler, NOTIFICATION_ENABLED);
						msg.sendToTarget();
						break;
					}
				}
			}*/
			if (responseNeeded/* && isWritten*/) {
				System.out.println("sending Response");
				mGattServer.sendResponse(mDevice, requestId, status, offset, value);
			}
		}

		
		@Override
		public void onExecuteWrite(BluetoothDevice device, int requestId,
				boolean execute) {
			// TODO Auto-generated method stub
			super.onExecuteWrite(device, requestId, execute);
		}

		@Override
		public void onServiceAdded(int status, BluetoothGattService service) {
			// TODO Auto-generated method stub
			super.onServiceAdded(status, service);
		}
		
	};

	private Handler mActivityHandler;
	public void setHandler(Handler handler) {
		mActivityHandler = handler;
	}
	private static AndroidANCSService mService;
	private boolean isConnected = false;
	public static AndroidANCSService getInstance(Context context) {
		if (mService == null) {
			mService = new AndroidANCSService(context);
			//generateCategory();
		}
		return mService;
	}
	public static void reset() {
		mService = null;
	}
	private HashMap<String, ArrayList<String>> contacts = new HashMap<String, ArrayList<String>>();
	private AndroidANCSService(Context context) {
		mContext = context;
		registerBoradcastReceiver();
		initGattServer();
		generateServer();
		
		try {
			generateAttributeMap();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("android.os.Build.MODEL is " + android.os.Build.MODEL);
		startNewLog();
		getAllContacts();
	}
	public void close() {
		unregisterBoradcastReceiver();
		//mCharacteristics.clear();
		notifications.clear();
		System.out.println("disconnect");
		if (mDevice != null && mGattServer != null) {
			mGattServer.cancelConnection(mDevice);
			System.out.println("disconnect mDevice");
		}
		if (mGattServer != null) {
			mGattServer.clearServices();
			mGattServer.close();
			mGattServer = null;
			System.out.println("close mGattServer");
		}
		if (mService != null) {
			mService = null;
			//generateCategory();
		}
	}
	private HashMap<String, Attribute> mAttributes;
	private final static int EVENT_CONTROL_POINT = 1;
	private final static int EVENT_NOTIFICATION_RECEIVED = 2;
	private final static int EVENT_BATTERY_NOTIFICATION_RECEIVED = 3;
	public final static int INCOMMING_CALL_POST_TIME = 1000;
	public final static int MISSING_CALL_POST_TIME = 10000;
	// 处理DataSource的事情
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			//super.handleMessage(msg);
			// get command from control point
			switch (msg.what) {
			case EVENT_CONTROL_POINT:
				System.out.println("Get command from control point...");
				if (mGattServer == null) {
					return;
				}
				byte[] value;
				try {
					//value = msg.getData().getByteArray("Command");
					value = doCommand((byte[]) msg.obj);
					//BluetoothGattCharacteristic charac = (BluetoothGattCharacteristic) msg.obj;
					BluetoothGattCharacteristic charac = mGattServer.getService(ANCS_SERVICE).getCharacteristic(ANCS_DATA_SOURCE);
					if (charac == null) {
						System.out.println("charc is null,it't not send notification to device");
						return;
					}
					/*System.out.println("value is " + ret);
					StringBuilder value = new StringBuilder(ret.length);
					for (byte b : ret) {
						value.append(b);
					}
					System.out.println(value.toString());*/
					//charac.setValue(ret);
		
					//mGattServer.notifyCharacteristicChanged(mDevice, charac, false);
					try {
						
						if (!gattServerNotifyCharacteristicChanged(mDevice, charac, value, false)) {
							System.out.println("notify failed...");
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case EVENT_NOTIFICATION_RECEIVED:
				
				System.out.println("Got Notification");
				if (mService == null || mDevice == null) {
					return;
				}
				Bundle data = msg.getData();
				ANCSNotification sbn = new ANCSNotification();
				//sbn.setNotification((Notification) data.getParcelable(ANCSNotification.NOTIFICATION));
				sbn.setTitle(data.getString(ANCSNotification.TITLE));
				sbn.setText(data.getString(ANCSNotification.TEXT));
				sbn.setPackageName(data.getString(ANCSNotification.PACKAGE_NAME));
				sbn.setPostTime(data.getLong(ANCSNotification.POST_TIME));
				if (!mAttributes.containsKey(sbn.getPackageName())) {
					// 过滤其他不支持的应用通知
					System.out.println("Notificaiton From Not Supported App");
					return;
				}
				System.out.println("title is " + sbn.getTitle());
				System.out.println("text is " + sbn.getText());
				System.out.println("PackageName is " + sbn.getPackageName());
				System.out.println("PostTime is " + sbn.getPostTime());
				//System.out.println("action is " + intent.getAction());
				if (msg.arg1 == 1 && (System.currentTimeMillis() - lastNotificationTime) > 500) {
					/*if (sbn.getPackageName().equals("com.google.android.dialer") && dialerNotiTimes != 1) {
						dialerNotiTimes++;
						return;
					}*/
					if (sbn.getPackageName().equals("com.google.android.dialer")) {
						sbn.setPostTime(INCOMMING_CALL_POST_TIME);
					}
					dialerNotiTimes = 0;
					lastNotificationTime = System.currentTimeMillis();
					System.out.println("adding sbn to array...");
					if (addNotificationToArray(sbn)) {
						System.out.println("sending add notification");
						sendAddNotification(sbn);
					} else {
						System.out.println("add failed");
					}
				} else if (msg.arg1 == 2){
					System.out.println("removing sbn to array...");
					
					if (sbn.getPackageName().equals("com.google.android.dialer")) {
						/*ANCSNotification notification = null;
						for (ANCSNotification noti : notifications) {
							if (noti.getPackageName().equals("com.google.android.dialer")) {
								System.out.println("Got Dialer!!!!!!");
								sbn.setPostTime(noti.getPostTime());
								notification = noti;
								break;
							}
						}
						if (notification != null) {
							removeNotificationFromArray(notification);
						}*/
						sbn.setPostTime(INCOMMING_CALL_POST_TIME);
					}
					if (removeNotificationFromArray(sbn)) {
						System.out.println("sending remove notification");
						//sbn.set
						sendRemoveNotification(sbn);
					}
				}
				break;
			case EVENT_BATTERY_NOTIFICATION_RECEIVED:

					//int percentage = (level*100)/scale;
					
					sendBatteryNotification(batteryPercentage);
				break;
			}
			
			
		}
		
	};
	int batteryPercentage = 0;
	private void generateAttributeMap() throws XmlPullParserException, IOException {
		XmlResourceParser parser = mContext.getResources().getXml(R.xml.attribute);
		int eventType = parser.getEventType();
		Attribute attribute = null;
		HashMap<String, Attribute> attributes = null;
		while (/*(*/eventType/* = parser.getEventType())*/ != XmlResourceParser.END_DOCUMENT) {
			switch (eventType) {
			case XmlResourceParser.START_DOCUMENT:
				
				break;
			case XmlResourceParser.START_TAG:
				String name = parser.getName();
				if (name.equalsIgnoreCase("AttributeSet")) {
					attributes = new HashMap<String, Attribute>();
				}else if (name.equalsIgnoreCase("attribute")) {
					attribute = new Attribute();
					attribute.setName(parser.getAttributeValue(null, "name"));
				} else if (attribute != null) {

					if (name.equalsIgnoreCase("appIdentifier")) {
						attribute.setAppIdentifier(parser.getAttributeValue(
								null, "name"));/*
												 * parser.nextText());//
												 * 如果后面是Text元素,即返回它的值
												 */
					} else if (name.equalsIgnoreCase("categoryID")) {
						attribute.setCategoryID(Integer.valueOf(parser.getAttributeValue(
								null, "name")));
					}
				}
				break;
			case XmlResourceParser.END_TAG:
				if (parser.getName().equalsIgnoreCase("attribute") && attribute != null) {
					String []names = attribute.getName().split("\\|");
					for (String attributeName : names) {
						Attribute newAttribute = new Attribute(attribute);
						attributes.put(attributeName, newAttribute);
					}
					//attributes.put(attribute.getName(), attribute);
					attribute = null;
				break;
				}
			}
			eventType = parser.next();
		}
		mAttributes = attributes;
	}
	
	private void registerBoradcastReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("ANCSAddNotification");
		intentFilter.addAction("ANCSRemoveNotification");
		intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		
		// 接电话的广播
		//intentFilter.addAction(Intent.ACTION_ANSWER);
		
		/* 三星来电的广播
		 * intentFilter.addAction("android.intent.action.WB_AMR");
		intentFilter.addAction("android.intent.action.pcmclkctrl");
			三星未接来电的广播
			intentFilter.addAction("android.intent.action.PHONE_EXSTATE_CHANGED");
		*/
		// 注册广播
		intentFilter.addAction(SMS_ACTION);
		mContext.registerReceiver(mReceiver, intentFilter);
		
		IntentFilter phoneStateFilter = new IntentFilter();
		phoneStateFilter.addAction("android.intent.action.PHONE_STATE");
		mContext.registerReceiver(mPhoneStateReceiver, phoneStateFilter);
	}
	private void unregisterBoradcastReceiver() {
		mContext.unregisterReceiver(mReceiver);
		mContext.unregisterReceiver(mPhoneStateReceiver);
	}
	private BluetoothGattCharacteristic getCharacteristic(UUID serviceUuid, UUID characteristicUuid) {
		if (mGattServer != null) {
			return null;
		}
		
		BluetoothGattService service = mGattServer.getService(serviceUuid);
		if (service == null) {
			System.out.println("service is null");
			return null;
		}
		BluetoothGattCharacteristic charac = service.getCharacteristic(characteristicUuid);
		return charac;
	}
	private BluetoothGattDescriptor getDescriptor(UUID serviceUuid, UUID characteristicUuid, UUID descriptorUuid) {
		BluetoothGattCharacteristic charac = getCharacteristic(serviceUuid, characteristicUuid);
		if (charac == null) {
			return null;
		}
		BluetoothGattDescriptor desc = charac.getDescriptor(descriptorUuid);
		return desc;
	}
	private boolean isEnableNotification(UUID serviceUuid, UUID characteristicUuid) {
		BluetoothGattDescriptor desc = getDescriptor(serviceUuid, characteristicUuid, CCC);
		if (desc == null) {
			return false;
		}
		return byteArrayEquals(desc.getValue(), BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
	}
	public void connect(BluetoothDevice device) {
		if (mGattServer != null && device != null)
			mGattServer.connect(device, false);
	}
	public void disconnect(BluetoothDevice device) {
		if (mGattServer != null && device != null)
			mGattServer.cancelConnection(device);
	}
	
	public void sendAddNotification(ANCSNotification sbn) {
		System.out.println("generating notification");
		if (mGattServer == null) {
			return;
		}
		byte []notification = generateNotificaitonSource(sbn, 0, 1 << 1);
		BluetoothGattCharacteristic notificationSource =
				mGattServer.getService(ANCS_SERVICE).getCharacteristic(ANCS_NOTIFICATION_SOURCE);
		System.out.println("mDevice is null?" + (mDevice==null) + " and notificationSource is null?" + (notificationSource==null));
		if (mDevice != null && notificationSource != null && notification.length > 0 && byteArrayEquals(notificationSource.getDescriptor(CCC).getValue(), BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
			System.out.println("notify...");
			notificationSource.setValue(notification);
			System.out.println("notify successful?" + mGattServer.notifyCharacteristicChanged(mDevice, notificationSource, false));
		}

	}
	public void sendRemoveNotification(ANCSNotification sbn) {
		System.out.println("generating notification");
		if (mGattServer == null) {
			return;
		}
		byte []notification = generateNotificaitonSource(sbn, 2, 1 << 1);
		BluetoothGattCharacteristic notificationSource =
				mGattServer.getService(ANCS_SERVICE).getCharacteristic(ANCS_NOTIFICATION_SOURCE);
		System.out.println("mDevice is null?" + (mDevice==null) + " and notificationSource is null?" + (notificationSource==null));
		if (mDevice != null && notificationSource != null && notification.length > 0 && byteArrayEquals(notificationSource.getDescriptor(CCC).getValue(), BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
			System.out.println("notify...");
			notificationSource.setValue(notification);
			mGattServer.notifyCharacteristicChanged(mDevice, notificationSource, false);
		}

	}
	public byte []generateNotificaitonSource(/*ANCSNotification ancsn*/ANCSNotification sbn, int eventID, int eventFlag) {
		byte []value = new byte[8];
		value[0] = (byte) eventID;
		value[1] = (byte) eventFlag;
		value[2] = (byte) getCatergory(sbn);
		System.out.println("categoryID is " + value[2]);
		//value[2] = (byte) getCatergory(ancsn);
		value[3] = 1;//Catergory Count
		int notificationUID = (int) sbn.getPostTime();
		for (int i = 4; i < 8; i++) {
			value[i] = (byte) ((notificationUID >> ((i - 4) * 8)) & 0xFF);
		}
		System.out.println("the value is " + value);
		StringBuilder sb = new StringBuilder(value.length);
		for (byte b : value) {
			sb.append(b + " ");
		}
		System.out.println(sb.toString());
		return value;
		
		/*byte []value = new byte[8];
		value[0] = 0;
		value[1] = 1 << 1;
		value[2] = 6;
		value[3] = 1;
		value[4] = 0;
		value[5] = 0;
		value[6] = 0;
		value[7] = 0;
		return value;*/
	}
	protected int getCatergory(ANCSNotification sbn) {
		//sbn.getPackageName().contains()
		String packageName = sbn.getPackageName();
		/*if (packageName.equals("com.android.phone")) {
			//if (sbn.getNotification().tickerText.toString().length() > 11) {
			if (sbn.getText().toString().length() > 11) {
				// 未接来电
				return 2;
			} else {
				// 正在来电
				return 1;
			}
		}*/
		if (packageName.equals("com.bde.ancs.androidancs")) {
			return 1;
			//return 4;
		}
		//System.out.println("categoryIDs" + categoryIDs);
		System.out.println("packageName is" + packageName);
		//Integer i = categoryIDs.get(packageName);	
		Attribute attribute = mAttributes.get(packageName);
		if (attribute == null) {
			return 0;
		}
		int i = attribute.getCategoryID();
		return i;
	}
	protected void initGattServer() {
		final BluetoothManager bluetoothManager =
		        (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		if (mGattServer == null) {
			mGattServer = bluetoothManager.openGattServer(mContext, mServerCallback);
		}
	}

	static int times = 0;
	protected void generateServer() {
		System.out.println("add Service time is " + (++times));
		BluetoothGattService ancsService = generateANCSService();
		BluetoothGattService batteryService = generateBatteryService();
		BluetoothGattService currentTimeService = generateTimeService();
		if (mGattServer != null) {
			//ancsService.addService(batteryService);
			mGattServer.addService(ancsService);
			mGattServer.addService(batteryService);
			mGattServer.addService(currentTimeService);
			
		}
	}

	protected BluetoothGattService generateANCSService() {
		BluetoothGattService service = new BluetoothGattService(ANCS_SERVICE,
				BluetoothGattService.SERVICE_TYPE_PRIMARY);

		/*
		 * BluetoothGattService batteryService = new
		 * BluetoothGattService(BATTERY_SERVICE,
		 * BluetoothGattService.SERVICE_TYPE_PRIMARY);
		 */

		BluetoothGattDescriptor notificationCCC = new BluetoothGattDescriptor(
				CCC, BluetoothGattDescriptor.PERMISSION_READ
						| BluetoothGattDescriptor.PERMISSION_WRITE);
		notificationCCC.setValue(new byte[]{0x00, 0x00});
		BluetoothGattDescriptor dataSourceCCC = new BluetoothGattDescriptor(
				CCC, BluetoothGattDescriptor.PERMISSION_READ
						| BluetoothGattDescriptor.PERMISSION_WRITE);
		notificationCCC.setValue(new byte[]{0x00, 0x00});


		BluetoothGattCharacteristic notificationSource = new BluetoothGattCharacteristic(
				ANCS_NOTIFICATION_SOURCE,
				BluetoothGattCharacteristic.PROPERTY_NOTIFY,
				BluetoothGattCharacteristic.PERMISSION_READ
						| BluetoothGattCharacteristic.PERMISSION_WRITE);
		notificationSource.addDescriptor(notificationCCC);
		notificationSource.setValue(new byte[]{0x00});

		BluetoothGattCharacteristic controlPoint = new BluetoothGattCharacteristic(
				ANCS_CONTROL_POINT, BluetoothGattCharacteristic.PROPERTY_WRITE,
				BluetoothGattCharacteristic.PERMISSION_WRITE);
		controlPoint.setValue(new byte[]{0x00});
		
		BluetoothGattCharacteristic dataSource = new BluetoothGattCharacteristic(
				ANCS_DATA_SOURCE, BluetoothGattCharacteristic.PROPERTY_NOTIFY,
				BluetoothGattCharacteristic.PERMISSION_READ	|
				BluetoothGattCharacteristic.PERMISSION_WRITE);
		dataSource.setValue(new byte[]{0x00});
		
		dataSource.addDescriptor(dataSourceCCC);

		/*mCharacteristics.add(notificationSource);
		mCharacteristics.add(controlPoint);
		mCharacteristics.add(dataSource);*/

		service.addCharacteristic(notificationSource);
		service.addCharacteristic(controlPoint);
		service.addCharacteristic(dataSource);

		/*
		 * if (mGattServer != null) { mGattServer.addService(service); }
		 */
		return service;
	}

	protected BluetoothGattService generateBatteryService() {
		/*BluetoothGattService batteryService = new BluetoothGattService(
				BATTERY_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);

		BluetoothGattDescriptor batteryLevelCCC = new BluetoothGattDescriptor(CCC,
				BluetoothGattDescriptor.PERMISSION_READ
						| BluetoothGattDescriptor.PERMISSION_WRITE);

		BluetoothGattCharacteristic batteryLevel = new BluetoothGattCharacteristic(
				BATTERY_LEVEL, BluetoothGattCharacteristic.PROPERTY_NOTIFY,
				BluetoothGattCharacteristic.PERMISSION_READ
						| BluetoothGattCharacteristic.PERMISSION_WRITE);

		batteryLevel.addDescriptor(batteryLevelCCC);

		mCharacteristics.add(batteryLevel);

		batteryService.addCharacteristic(batteryLevel);

		
		 * if (mGattServer != null) { mGattServer.addService(batteryService); }
		 */
		
		
		
		
		BluetoothGattService batteryService = new BluetoothGattService(BATTERY_SERVICE,
				BluetoothGattService.SERVICE_TYPE_PRIMARY);

		BluetoothGattDescriptor batteryCCC = new BluetoothGattDescriptor(
				CCC, BluetoothGattDescriptor.PERMISSION_READ
						| BluetoothGattDescriptor.PERMISSION_WRITE);
		batteryCCC.setValue(new byte[]{0x00, 0x00});
		
		BluetoothGattCharacteristic batteryLevel = new BluetoothGattCharacteristic(
				BATTERY_LEVEL,
				BluetoothGattCharacteristic.PROPERTY_NOTIFY |
				BluetoothGattCharacteristic.PROPERTY_READ,
				BluetoothGattCharacteristic.PERMISSION_READ);
		batteryLevel.addDescriptor(batteryCCC);
		batteryLevel.setValue(new byte[]{0x00});
		
		//mCharacteristics.add(batteryLevel);

		batteryService.addCharacteristic(batteryLevel);
		
		
		
		return batteryService;
	}
	private BluetoothGattService generateTimeService() {
		BluetoothGattService currentTimeService = new BluetoothGattService(CURRENT_TIME_SERVICE,
				BluetoothGattService.SERVICE_TYPE_PRIMARY);

		BluetoothGattDescriptor currentTimeCCC = new BluetoothGattDescriptor(
				CCC, BluetoothGattDescriptor.PERMISSION_READ
						| BluetoothGattDescriptor.PERMISSION_WRITE);
		currentTimeCCC.setValue(new byte[]{0x00, 0x00});
		
		BluetoothGattCharacteristic currentTime = new BluetoothGattCharacteristic(
				CURRENT_TIME,
				BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_READ,
				BluetoothGattCharacteristic.PERMISSION_READ
						/*| BluetoothGattCharacteristic.PERMISSION_WRITE*/);
		currentTime.addDescriptor(currentTimeCCC);
		currentTime.setValue(new byte[]{0x00});

		currentTimeService.addCharacteristic(currentTime);
		
		BluetoothGattCharacteristic localTimeInfo = new BluetoothGattCharacteristic(
				LOCAL_TIME_INFO,
				BluetoothGattCharacteristic.PROPERTY_READ,
				BluetoothGattCharacteristic.PERMISSION_READ
						/*| BluetoothGattCharacteristic.PERMISSION_WRITE*/);
		localTimeInfo.setValue(new byte[]{0x00});
		
		BluetoothGattCharacteristic referenceTimeInfo = new BluetoothGattCharacteristic(
				REFERENCE_TIME_INFO,
				BluetoothGattCharacteristic.PROPERTY_READ,
				BluetoothGattCharacteristic.PERMISSION_READ
						/*| BluetoothGattCharacteristic.PERMISSION_WRITE*/);
		referenceTimeInfo.setValue(new byte[]{0x00});
		
		Calendar cal = Calendar.getInstance(Locale.getDefault());
		int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
		int zone=zoneOffset/60/60/1000;
		byte []local = new byte[2];
		local[0] = (byte)((zone * 4) & 0xff);
		local[1] = 0;
		localTimeInfo.setValue(local);
		
		currentTimeService.addCharacteristic(localTimeInfo);
		currentTimeService.addCharacteristic(referenceTimeInfo);
		
		return currentTimeService;
	}
	protected byte []doCommand(byte []value/*, StatusBarNotification sbn1*/) throws NameNotFoundException {
		if (value.length >= 6) {
			int command = value[0];
			ByteArrayBuffer ret = new ByteArrayBuffer(9);
			ret.append(command);
			NotificationAttribute attribute = new NotificationAttribute();
			switch (command) {
			case 0:
				System.out.println("Command is Get Notification Attribute");
				
				int notificationUID = (value[1] & 0xFF);
				notificationUID |= (value[2] & 0xFF) << 8;
				notificationUID |= (value[3] & 0xFF) << 16;
				notificationUID |= (value[4] & 0xFF) << 24;
				for (int i = 1; i < 5; i++) {
					ret.append(value[i]);
				}
				System.out.println("Notification UID is " + notificationUID);
				
				ANCSNotification sbn = null;
				for (ANCSNotification every : notifications) {
					if ((int)(every.getPostTime()) == notificationUID) {
						sbn = every;
						System.out.println("Found StatusBarNotification");
						break;
					}
				}
				if (sbn == null) {
					return null;
				}
				for (int i = 5; i < value.length; i++) {
					attribute.setAttributeID(value[i]);
					// attribute ID
					ret.append(value[i++]);
					byte []parameter = null;
					if (attribute.isNeededParameter()) {
						parameter = new byte[2];
						parameter[0] = value[i++];
						parameter[1] = value[i++];
					}
					System.out.println("doCommand...");
					byte[] attributeValue = null;
					try {
						attributeValue = attribute.doCommand(parameter, sbn, mAttributes);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// attribute value
					if (attributeValue == null) {
						ret.append(0x00);
						ret.append(0x00);
					} else {
						ret.append((attributeValue.length & 0xFF));
						ret.append(((attributeValue.length & 0xFF00) >> 8));
						ret.append(attributeValue, 0, attributeValue.length);
					}
					/*ret.append((attributeValue.length & 0xFF));
					ret.append(((attributeValue.length & 0xFF00) >> 8));*/
					
				}
				break;
			case 1:
				StringBuilder builder = new StringBuilder();
				int i = 1;
				for (; value[i] != 0; i++) {
					builder.append(value[i]);
					ret.append(value[i]);
				}
				// 以NULL结尾
				ret.append(0);
				switch (value[i]) {
				case 0:
					// attributeID 0 is AppAttributeIDDisplayName
					String displayName = mContext.getPackageManager().getPackageInfo(
							builder.toString(), PackageManager.GET_ACTIVITIES).applicationInfo
							.loadLabel(mContext.getPackageManager()).toString();
					ret.append(displayName.getBytes(), 0, displayName.length());
					break;
				default:
					System.out.println("unkonwn get app attribute command");
					break;
				}
				break;
			}
			byte []log = ret.buffer();
			System.out.println("doCommand ret is " + log);
			StringBuilder sb = new StringBuilder(log.length);
			for (byte b : log) {
				sb.append(b + " ");
			}
			System.out.println(sb.toString());
			
			return ret.toByteArray();
		}
		return null;
	}
	public void sendBatteryNotification(int percentage) {
		if (mGattServer == null) {
			return;
		}
		/*BluetoothGattCharacteristic batteryLevel =
				mGattServer.getService(BATTERY_SERVICE).getCharacteristic(BATTERY_LEVEL);*/
		//BluetoothGattCharacteristic batteryLevel = getCharacteristic(BATTERY_SERVICE, BATTERY_LEVEL);
		BluetoothGattService service = mGattServer.getService(BATTERY_SERVICE);
		if (service == null) {
			System.out.println("service is null");
			return;
		}
		BluetoothGattCharacteristic batteryLevel = service.getCharacteristic(BATTERY_LEVEL);
		if (batteryLevel == null) {
			System.out.println("batteryLevel is null");
			return;
		}
		
		
		
		
		System.out.println("mDevice is null?" + (mDevice==null) + " and batteryLevel is null?" + (batteryLevel==null));
		if (mDevice != null && batteryLevel != null) {
			System.out.println("notify...");
			byte []value = {(byte) (percentage & 0xFF)};
			batteryLevel.setValue(value);
			//BluetoothGattDescriptor ccc = batteryLevel.getDescriptor(CCC);
			
			//if (ccc != null && byteArrayEquals(ccc.getValue(), BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
				//sysoutByte(ccc.getValue());
				System.out.println("do it now");
				mGattServer.notifyCharacteristicChanged(mDevice, batteryLevel, false);
			//}
		}
	}
	private void sendTimeNotification() {
		if (mGattServer == null) {
			return;
		}
		BluetoothGattService service = mGattServer.getService(CURRENT_TIME_SERVICE);
		if (service == null) {
			System.out.println("service is null");
			return;
		}
		BluetoothGattCharacteristic currentTime = service.getCharacteristic(CURRENT_TIME);
		if (currentTime == null) {
			System.out.println("batteryLevel is null");
			return;
		}
		
		
		
		
		System.out.println("mDevice is null?" + (mDevice==null) + " and currentTime is null?" + (currentTime==null));
		if (mDevice != null && currentTime != null) {
			System.out.println("notify...");
			/*byte []value = {(byte) (percentage & 0xFF)};
			batteryLevel.setValue(value);*/
			BluetoothGattDescriptor ccc = currentTime.getDescriptor(CCC);
			Log.i(TAG, "CCC's value is");
			printByteArray(ccc.getValue());
			if (ccc != null && byteArrayEquals(ccc.getValue(), BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
				//sysoutByte(ccc.getValue());
				currentTime.setValue(getCurrentTimeData());
				System.out.println("do it now");
				mGattServer.notifyCharacteristicChanged(mDevice, currentTime, false);
			}
		}
	}
	private void printByteArray(byte []value) {
		StringBuilder sb = new StringBuilder(value.length);
		for (byte b : value) {
			if ((b & 0xff) < 0x0f) {
				sb.append("0");
			}
			sb.append(b + " ");
		}
		Log.i(TAG, sb.toString());
	}
	private byte[] getCurrentTimeData() {
		Calendar c = Calendar.getInstance();
		ByteArrayBuffer bab = new ByteArrayBuffer(10);
		int year = c.get(Calendar.YEAR);
		bab.append(year & 0xff);
		bab.append((year & 0xff00) >> 8);
		int month = c.get(Calendar.MONTH);
		bab.append(((++month) % 13) & 0xff);
		int day = c.get(Calendar.DAY_OF_MONTH);
		bab.append(day & 0xff);
		int hours = c.get(Calendar.HOUR_OF_DAY);
		bab.append(hours & 0xff);
		int minutes = c.get(Calendar.MINUTE);
		bab.append(minutes & 0xff);
		int seconds = c.get(Calendar.SECOND);
		bab.append(seconds & 0xff);
		
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		dayOfWeek = (dayOfWeek - 1) == 0 ? 7 : (dayOfWeek - 1);
		bab.append(dayOfWeek & 0xff);
		
		long time = new Date().getTime();
		long nextSecond = ((time / 1000) + 1) * 1000;
		int fractions256 = (int) ((nextSecond - time) / 4);
		bab.append(fractions256 & 0xff);
		bab.append(0);
		return bab.toByteArray();
	}
	private boolean byteArrayEquals(byte []value1, final byte []value2) {
		if (value1 == null || value2 == null) {
			return false;
		}
		if (value1.length != value2.length) {
			return false;
		}
		for (int i = 0; i < value1.length; i++) {
			if (value1[i] != value2[i]) {
				return false;
			}
		}
		return true;
	}
	private void sysoutByte(byte []value) {
		StringBuilder sb = new StringBuilder(value.length);
		for (byte b : value) {
			sb.append(b);
			
		}
		System.out.println(sb);
	}
	long lastNotificationTime = 0;
	int dialerNotiTimes = 0;
	//String smsDisplayName;
	public static boolean IS_TRIAL = false;
	public static int NOTIFICATION_PER_DAY = 10;
	// 短信特定的notificationUID，
	public static final int SMS_POST_TIME = 100;
	String SMS_ACTION = "android.provider.Telephony.SMS_RECEIVED";
	private/*public static*/ class NotificationReceiver extends BroadcastReceiver {
		int isTheSameDay (int currentYear, int currentMonth, int currentDay, int year, int month, int day) {
			int deltaYear = currentYear - year;
			int deltaMonth = currentMonth - month;
			int deltaDay = currentDay - day;
			
			return (deltaYear * 365 + deltaMonth * 30 + deltaDay);
		}
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			System.out.println("NotificationReceiver with action" + intent.getAction());
			Bundle extras = intent.getExtras();
			if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
				//获取当前电量
				int level = intent.getIntExtra("level", 0);
				//电量的总刻度
				int scale = intent.getIntExtra("scale", 100);
				//把它转成百分比
				batteryPercentage = (level*100)/scale;
				Message msg = mHandler.obtainMessage(EVENT_BATTERY_NOTIFICATION_RECEIVED);
				msg.sendToTarget();
				//sendBatteryNotification(percentage);
				return;
			} else if (intent.getAction().equals(SMS_ACTION)) { 
				notifications.clear();
		            StringBuffer SMSAddress = new StringBuffer(); 
		            StringBuffer SMSContent = new StringBuffer(); 
		            Bundle bundle = intent.getExtras();
		            if (bundle != null) { 
		                Object[] pdusObjects = (Object[]) bundle.get("pdus"); 
		                SmsMessage[] messages = new SmsMessage[pdusObjects.length]; 
		                for (int i = 0; i < pdusObjects.length; i++) { 
		                    messages[i] = SmsMessage 
		                            .createFromPdu((byte[]) pdusObjects[i]); 
		                } 
		                for (SmsMessage message : messages) { 
		                    SMSAddress.append(message.getDisplayOriginatingAddress()); 
		                    SMSContent.append(message.getDisplayMessageBody()); 
		                } 
		            }
		            /*String realNumber = SMSAddress.toString();
		            if (SMSAddress.length() > 11) {
		            	realNumber = SMSAddress.substring(SMSAddress.length() - 11);
		            }*/
		            System.out.println("Phone number is " + SMSAddress.toString());
		            String name = "";
		            if ((name = getPeopleFromContacts(SMSAddress.toString())).equals("")) {
		            	name = SMSAddress.toString();
		            }
		            
		            // 当sms通知在notificationservice中产生时
		            //smsDisplayName = name;
		            
		            
		            Bundle data = new Bundle();
		            data.putString(ANCSNotification.TITLE, name);
		    		data.putString(ANCSNotification.TEXT, SMSContent.toString());
		    		data.putLong(ANCSNotification.POST_TIME, SMS_POST_TIME);
		    		data.putString(ANCSNotification.PACKAGE_NAME, "com.android.mms");
		    		
		    		Message msg = mHandler.obtainMessage(EVENT_NOTIFICATION_RECEIVED);
					msg.setData(data);
					msg.arg1 = 1;
					msg.sendToTarget();
					return;
		        }
			Message msg = mHandler.obtainMessage(EVENT_NOTIFICATION_RECEIVED);
			/*String packageName = extras.getString(ANCSNotification.PACKAGE_NAME, "");
			if (packageName != null && !packageName.equals("")) {
				if (NotificationService.SMS.contains(packageName)) {
					extras.putString(ANCSNotification.TITLE, smsDisplayName);
				}
			}*/
			
			msg.setData(extras);
			if (intent.getAction().equals("ANCSAddNotification")) {
				msg.arg1 = 1;
			} else if (intent.getAction().equals("ANCSRemoveNotification")) {
				msg.arg1 = 2;
			}
			msg.sendToTarget();
			/*if (intent.getAction().equals("android.intent.action.WB_AMR") || intent.getAction().equals("android.intent.action.pcmclkctrl")) {
				System.out.println("samsung");
			}
			if (extras != null) {
				System.out.println("has extras");
			}*/
			/*if (IS_TRIAL) {
				SharedPreferences sp = context.getSharedPreferences("trial", Context.MODE_PRIVATE);
				Calendar c = Calendar.getInstance();
				int year = c.get(Calendar.YEAR);
				int month = c.get(Calendar.MONTH);
				int day = c.get(Calendar.DAY_OF_MONTH);
				
				int currentYear, currentMonth, currentDay;
				
				currentYear = sp.getInt("year", 0);
				currentMonth = sp.getInt("month", 0);
				currentDay = sp.getInt("day", 0);
				int currentNotificationNum = sp.getInt("currentNotificationNum", 1);
				
				if (isTheSameDay(currentYear, currentMonth, currentDay, year, month, day) < 0) {
					//currentNotificationNum = 1;
					System.out.println("this is the next day");
					Editor editor = sp.edit();
					editor.putInt("year", year);
					editor.putInt("month", month);
					editor.putInt("day", day);
					editor.putInt("currentNotificationNum", 1);
					editor.commit();
				} else if (currentNotificationNum < NOTIFICATION_PER_DAY) {
					currentNotificationNum++;
					sp.edit().putInt("currentNotificationNum", currentNotificationNum).commit();
					System.out.println("currentNotificationNum is " + currentNotificationNum);
				} else {
					System.out.println("currentNotificationNum is over " + NOTIFICATION_PER_DAY + " : " + currentNotificationNum);
					return;
				}
				if (mCallback != null) {
					mCallback.trialTime(currentNotificationNum);
				}
			}
			if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
				//获取当前电量
				int level = intent.getIntExtra("level", 0);
				//电量的总刻度
				int scale = intent.getIntExtra("scale", 100);
				//把它转成百分比
				int percentage = (level*100)/scale;
				
				sendBatteryNotification(percentage);
				return;
			}
			System.out.println("Got Notification");
			if (mService == null || mDevice == null) {
				return;
			}
			Bundle data = intent.getExtras();
			ANCSNotification sbn = new ANCSNotification();
			//sbn.setNotification((Notification) data.getParcelable(ANCSNotification.NOTIFICATION));
			sbn.setTitle(data.getString(ANCSNotification.TITLE));
			sbn.setText(data.getString(ANCSNotification.TEXT));
			sbn.setPackageName(data.getString(ANCSNotification.PACKAGE_NAME));
			sbn.setPostTime(data.getLong(ANCSNotification.POST_TIME));
			if (!mAttributes.containsKey(sbn.getPackageName())) {
				return;
			}
			System.out.println("title is " + sbn.getTitle());
			System.out.println("text is " + sbn.getText());
			System.out.println("PackageName is " + sbn.getPackageName());
			System.out.println("PostTime is " + sbn.getPostTime());
			System.out.println("action is " + intent.getAction());
			if (intent.getAction().equals("ANCSAddNotification") && (System.currentTimeMillis() - lastNotificationTime) > 400) {
				if (sbn.getPackageName().equals("com.google.android.dialer") && dialerNotiTimes != 1) {
					dialerNotiTimes++;
					return;
				}
				dialerNotiTimes = 0;
				lastNotificationTime = System.currentTimeMillis();
				System.out.println("adding sbn to array...");
				addNotificationToArray(sbn);
				sendAddNotification(sbn);
			} else if (intent.getAction().equals("ANCSRemoveNotification")){
				removeNotificationFromArray(sbn);
				sendRemoveNotification(sbn);
			}*/
		}
	}

	public static final int NUMBER_CUT_BIT = 7;
	public class PhoneStateReceiver extends BroadcastReceiver{  
        
        private static final String TAG = "PhoneStatReceiver";  
          
//        private static MyPhoneStateListener phoneListener = new MyPhoneStateListener();  
          
        private boolean incomingFlag = false;  
          
        private String incoming_number = null;  
        private String displayName = null;
  
        @Override  
        public void onReceive(Context context, Intent intent) {  
                //如果是拨打电话  
                if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){                          
                        incomingFlag = false;  
                        String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);          
                        Log.i(TAG, "call OUT:"+phoneNumber);                          
                }else{                          
                        //如果是来电  
                        TelephonyManager tm =   
                            (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);                          
                          
                        switch (tm.getCallState()) {  
                        case TelephonyManager.CALL_STATE_RINGING: 
                        	notifications.clear();
                                incomingFlag = true;//标识当前是来电  
                                incoming_number = intent.getStringExtra("incoming_number"); 
                                displayName = getPeopleFromContacts(incoming_number);
                                if (displayName.equals("")) {
                                	displayName = incoming_number;
                                }
                                
                                Bundle data = new Bundle();
            		            data.putString(ANCSNotification.TITLE, displayName);
            		    		data.putString(ANCSNotification.TEXT, "来电");
            		    		data.putLong(ANCSNotification.POST_TIME, INCOMMING_CALL_POST_TIME);
            		    		data.putString(ANCSNotification.PACKAGE_NAME, "com.google.android.dialer");
            		    		
            		    		Message msg = mHandler.obtainMessage(EVENT_NOTIFICATION_RECEIVED);
            					msg.setData(data);
            					msg.arg1 = 1;
            					msg.sendToTarget();
                                Log.i(TAG, "RINGING :"+ incoming_number);  
                                break;  
                        case TelephonyManager.CALL_STATE_OFFHOOK:                                  
                                if(incomingFlag){  
                                        Log.i(TAG, "incoming ACCEPT :"+ incoming_number); 
                                        System.out.println("answered the phone");
                    		        	
                    		        	data = new Bundle();
                    		            data.putString(ANCSNotification.TITLE, "");
                    		    		data.putString(ANCSNotification.TEXT, "");
                    		    		data.putLong(ANCSNotification.POST_TIME, INCOMMING_CALL_POST_TIME);
                    		    		data.putString(ANCSNotification.PACKAGE_NAME, "com.google.android.dialer");
                    		    		
                    		        	msg = mHandler.obtainMessage(EVENT_NOTIFICATION_RECEIVED);
                    					msg.setData(data);
                    					msg.arg1 = 2;
                    					msg.sendToTarget();
                    					
                                }  
                                incomingFlag = false;
                                break;  
                          
                        case TelephonyManager.CALL_STATE_IDLE:                                  
                                if(incomingFlag){  
                                	// 未接来电
                                        Log.i(TAG, "未接来电");  
                                        // 先remove incomming call
                                        Log.i(TAG, "未接来电 :"+ incoming_number); 
                                        System.out.println("missing call");
                    		        	
                    		        	data = new Bundle();
                    		            data.putString(ANCSNotification.TITLE, "");
                    		    		data.putString(ANCSNotification.TEXT, "");
                    		    		data.putLong(ANCSNotification.POST_TIME, INCOMMING_CALL_POST_TIME);
                    		    		data.putString(ANCSNotification.PACKAGE_NAME, "com.google.android.dialer");
                    		    		
                    		        	msg = mHandler.obtainMessage(EVENT_NOTIFICATION_RECEIVED);
                    					msg.setData(data);
                    					msg.arg1 = 2;
                    					msg.sendToTarget();
                                        
                    					// 再添加未接来电
                                        data = new Bundle();
                    		            data.putString(ANCSNotification.TITLE, displayName);
                    		    		data.putString(ANCSNotification.TEXT, "未接来电");
                    		    		data.putLong(ANCSNotification.POST_TIME, MISSING_CALL_POST_TIME);
                    		    		data.putString(ANCSNotification.PACKAGE_NAME, "com.android.phone");
                    		    		
                    		        	msg = mHandler.obtainMessage(EVENT_NOTIFICATION_RECEIVED);
                    					msg.setData(data);
                    					msg.arg1 = 1;
                    					msg.sendToTarget();
                                }  else {
                                	// 已接来电，可是已经挂电话了
                                }
                                incomingFlag = false;
                                break;  
                        }   
                }  
        }  
}  
	private String getPeople(String number) {  
		logToFile("\nnumber", "getPeople() is Called");
		logToFile("number", "original number is " + number);
		if (number.length() > NUMBER_CUT_BIT) {
			number = number.substring(number.length() - NUMBER_CUT_BIT);
		}
		logToFile("number", "the number after cut is " + number);
		
		
        String[] projection = { ContactsContract.PhoneLookup.DISPLAY_NAME,  
                                ContactsContract.CommonDataKinds.Phone.NUMBER};  
  
        Log.d(TAG, "getPeople ---------");  
          
        // 将自己添加到 msPeers 中  
        Cursor cursor = mContext.getContentResolver().query(  
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,  
                projection,    // Which columns to return.  
                ContactsContract.CommonDataKinds.Phone.NUMBER + " like '%" + number + "'", // WHERE clause.  
                null,          // WHERE clause value substitution  
                null);   // Sort order.  
  
        if( cursor == null ) {  
        	logToFile("number", "cursor is null");
            Log.d(TAG, "getPeople null");  
            return "";  
        }  
        logToFile("number", "cursor is not null");
        Log.d(TAG, "getPeople cursor.getCount() = " + cursor.getCount());  
        logToFile("number", "cursor.getCount is " + cursor.getCount());
        String name = "";
        for( int i = 0; i < cursor.getCount(); i++ )  
        {  
            cursor.moveToPosition(i);  
              
            // 取得联系人名字  
            int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);     
            name = cursor.getString(nameFieldColumnIndex);  
            logToFile("number", "name " + i + " is " + name);
        }
        logToFile("number", "return name is " + name);
        return name;
	}
	
	private String getPeopleSearch(String number) {
		logToFile("getPeopleSearch()", "Start Searching name using number=" + number);
		String name = "";
		Cursor cur = mContext.getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI,
				null,
				null,
				null,
				ContactsContract.Contacts.DISPLAY_NAME
						+ " COLLATE LOCALIZED ASC");
		boolean isMatch = false;
		// 循环遍历
		if (cur.moveToFirst()) {
			int idColumn = cur.getColumnIndex(ContactsContract.Contacts._ID);

			int displayNameColumn = cur
					.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

			do {
				// 获得联系人的ID号
				String contactId = cur.getString(idColumn);
				// 获得联系人姓名
				String disPlayName = cur.getString(displayNameColumn);
				
				// 查看该联系人有多少个电话号码。如果没有这返回值为0
				int phoneCount = cur
						.getInt(cur
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
				//Log.i("username", disPlayName);
				logToFile("username", disPlayName);
				if (phoneCount > 0) {
					// 获得联系人的电话号码
					Cursor phones = mContext.getContentResolver().query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ " = " + contactId, null, null);
					if (phones.moveToFirst()) {
						do {
							// 遍历所有的电话号码
							String phoneNumber = phones
									.getString(phones
											.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
							String phoneType = phones
									.getString(phones
											.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
							/*Log.i("phoneNumber", phoneNumber);
							Log.i("phoneType", phoneType);
							Log.i("compare", "compare number and phoneNumber");*/
							logToFile("phoneNumber", phoneNumber);
							logToFile("phoneType", phoneType);
							logToFile("compare", "compare number and phoneNumber");
							if (PhoneNumberUtils.compare(number, phoneNumber)) {
								Log.i("compare", "match");
								logToFile("compare", "match");
								name = disPlayName;
								isMatch = true;
								break;
							}
						} while (phones.moveToNext());
						
					}
					phones.close();
					if (isMatch) {
						break;
					}
				}
			} while (cur.moveToNext());
		}
		return name;
	}
	private String getPeopleFromContacts(String numberNeedToBeMatched) {
		logToFile("getPeopleFromContacts", "Start Searching name using number=" + numberNeedToBeMatched);
		Set<String> displayNames = contacts.keySet();
		for (String displayName : displayNames) {
			logToFile("matching", "displayName is " + displayName);
			ArrayList<String> numbers = contacts.get(displayName);
			for (String number : numbers) {
				logToFile("matching", "the number of this displayName is" + number);
				if (PhoneNumberUtils.compare(number, numberNeedToBeMatched)) {
					logToFile("matching", "match!");
					return displayName;
				}
			}
		}
		return "";
	}
	public void getAllContacts() {
		logToFile("getAllContacts()", "Get All Contacts...");
				// 获得所有的联系人
				Cursor cur = mContext.getContentResolver().query(
						ContactsContract.Contacts.CONTENT_URI,
						null,
						null,
						null,
						ContactsContract.Contacts.DISPLAY_NAME
								+ " COLLATE LOCALIZED ASC");
				// 循环遍历
				if (cur.moveToFirst()) {
					int idColumn = cur.getColumnIndex(ContactsContract.Contacts._ID);

					int displayNameColumn = cur
							.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

					do {
						// 获得联系人的ID号
						String contactId = cur.getString(idColumn);
						// 获得联系人姓名
						String disPlayName = cur.getString(displayNameColumn);
						
						// 查看该联系人有多少个电话号码。如果没有这返回值为0
						int phoneCount = cur
								.getInt(cur
										.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
						//Log.i("username", disPlayName);
						logToFile("username", disPlayName);
						ArrayList<String> numbers = new ArrayList<String>();
						if (phoneCount > 0) {
							// 获得联系人的电话号码
							Cursor phones = mContext.getContentResolver().query(
									ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
									null,
									ContactsContract.CommonDataKinds.Phone.CONTACT_ID
											+ " = " + contactId, null, null);
							if (phones.moveToFirst()) {
								do {
									// 遍历所有的电话号码
									String phoneNumber = phones
											.getString(phones
													.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
									String phoneType = phones
											.getString(phones
													.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
									/*Log.i("phoneNumber", phoneNumber);
									Log.i("phoneType", phoneType);*/
									logToFile("phoneNumber", phoneNumber);
									logToFile("phoneType", phoneType);
									numbers.add(phoneNumber);
								} while (phones.moveToNext());
								
							}
							if (numbers.size() > 0) {
								contacts.put(disPlayName, numbers);
							}
							phones.close();
						}

						/*// 获取该联系人邮箱
						Cursor emails = mContext.getContentResolver().query(
								ContactsContract.CommonDataKinds.Email.CONTENT_URI,
								null,
								ContactsContract.CommonDataKinds.Phone.CONTACT_ID
										+ " = " + contactId, null, null);
						if (emails.moveToFirst()) {
							do {
								// 遍历所有的电话号码
								String emailType = emails
										.getString(emails
												.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
								String emailValue = emails
										.getString(emails
												.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
								
								Log.i("emailType", emailType);
								Log.i("emailValue", emailValue);
								logToFile("emailType", emailType);
								logToFile("emailValue", emailValue);
							} while (emails.moveToNext());
						}
						emails.close();

						// 获取该联系人IM
						Cursor IMs = mContext.getContentResolver().query(
								Data.CONTENT_URI,
								new String[] { Data._ID, Im.PROTOCOL, Im.DATA },
								Data.CONTACT_ID + "=?" + " AND " + Data.MIMETYPE + "='"
										+ Im.CONTENT_ITEM_TYPE + "'",
								new String[] { contactId }, null);
						if (IMs.moveToFirst()) {
							do {
								String protocol = IMs.getString(IMs
										.getColumnIndex(Im.PROTOCOL));
								String date = IMs
										.getString(IMs.getColumnIndex(Im.DATA));
								Log.i("protocol", protocol);
								Log.i("date", date);
								logToFile("protocol", protocol);
								logToFile("date", date);
							} while (IMs.moveToNext());
						}
						IMs.close();

						// 获取该联系人地址
						Cursor address = mContext.getContentResolver()
								.query(
										ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
										null,
										ContactsContract.CommonDataKinds.Phone.CONTACT_ID
												+ " = " + contactId, null, null);
						if (address.moveToFirst()) {
							do {
								// 遍历所有的地址
								String street = address
										.getString(address
												.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
								String city = address
										.getString(address
												.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
								String region = address
										.getString(address
												.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
								String postCode = address
										.getString(address
												.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
								String formatAddress = address
										.getString(address
												.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
								Log.i("street", street);
								Log.i("city", city);
								Log.i("region", region);
								Log.i("postCode", postCode);
								Log.i("formatAddress", formatAddress);
								
								logToFile("street", street);
								logToFile("city", city);
								logToFile("region", region);
								logToFile("postCode", postCode);
								logToFile("formatAddress", formatAddress);
							} while (address.moveToNext());
						}
						address.close();
						
						// 获取该联系人组织
						Cursor organizations = mContext.getContentResolver().query(
								Data.CONTENT_URI,
								new String[] { Data._ID, Organization.COMPANY,
										Organization.TITLE },
								Data.CONTACT_ID + "=?" + " AND " + Data.MIMETYPE + "='"
										+ Organization.CONTENT_ITEM_TYPE + "'",
								new String[] { contactId }, null);
						if (organizations.moveToFirst()) {
							do {
								String company = organizations.getString(organizations
										.getColumnIndex(Organization.COMPANY));
								String title = organizations.getString(organizations
										.getColumnIndex(Organization.TITLE));
								Log.i("company", company);
								Log.i("title", title);
							} while (organizations.moveToNext());
						}
						organizations.close();

						// 获取备注信息
						Cursor notes = mContext.getContentResolver().query(
								Data.CONTENT_URI,
								new String[] { Data._ID, Note.NOTE },
								Data.CONTACT_ID + "=?" + " AND " + Data.MIMETYPE + "='"
										+ Note.CONTENT_ITEM_TYPE + "'",
								new String[] { contactId }, null);
						if (notes.moveToFirst()) {
							do {
								String noteinfo = notes.getString(notes
										.getColumnIndex(Note.NOTE));
								Log.i("noteinfo", noteinfo);
								logToFile("noteinfo", noteinfo);
							} while (notes.moveToNext());
						}
						notes.close();

						// 获取nickname信息
						Cursor nicknames = mContext.getContentResolver().query(
								Data.CONTENT_URI,
								new String[] { Data._ID, Nickname.NAME },
								Data.CONTACT_ID + "=?" + " AND " + Data.MIMETYPE + "='"
										+ Nickname.CONTENT_ITEM_TYPE + "'",
								new String[] { contactId }, null);
						if (nicknames.moveToFirst()) {
							do {
								String nickname_ = nicknames.getString(nicknames
										.getColumnIndex(Nickname.NAME));
								Log.i("nickname_", nickname_);
								logToFile("nickname_", nickname_);
							} while (nicknames.moveToNext());
						}
						nicknames.close();*/

					} while (cur.moveToNext());

				}
				cur.close();

	}
	Callback mCallback;
	public void setCallback(Callback callback) {
		mCallback = callback;
	}
	public interface Callback {
		public void trialTime(int trialNum);
	}
	/*@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mMessenger.getBinder();
	}*/
	private void logToFile(String tag, String msg) {
		
		try {
			String sdCardPath = Environment.getExternalStorageDirectory().getPath();
			//FileOutputStream outStream=mContext.openFileOutput(sdCardPath + "/log.txt",Context.MODE_APPEND);
			FileOutputStream outStream = new FileOutputStream(sdCardPath + "/log.txt", true);
			StringBuilder sb = new StringBuilder();
			sb.append(tag + " " + ((msg == null) ? "NULL" : msg) + "\n");
			outStream.write(sb.toString().getBytes());
			outStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void startNewLog() {
		try {
			String sdCardPath = Environment.getExternalStorageDirectory().getPath();
			//FileOutputStream outStream=mContext.openFileOutput(sdCardPath + "/log.txt",Context.MODE_APPEND);
			FileOutputStream outStream = new FileOutputStream(sdCardPath + "/log.txt", true);
			StringBuilder sb = new StringBuilder();
			sb.append("\n\n New Log\n");
			outStream.write(sb.toString().getBytes());
			outStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
