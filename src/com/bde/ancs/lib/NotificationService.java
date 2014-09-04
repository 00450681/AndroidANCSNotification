package com.bde.ancs.lib;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.R.raw;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.widget.RemoteViews;
import android.widget.Toast;

public class NotificationService extends NotificationListenerService {
	//AndroidANCSService mANCS;
	public static final String SMS = "com.android.mms|com.google.android.talk|com.htc.sense.mms";
	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		// TODO Auto-generated method stub
		
		System.out.println("onNotificationPosted + when = " + sbn.getNotification().when + " tricker text = " + sbn.getNotification().tickerText);
		System.out.println("notification " + sbn.getNotification().contentView.toString());
		System.out.println("packagename is " + sbn.getPackageName());
		//return;
		Intent intent = new Intent("ANCSAddNotification");
		Bundle data = new Bundle();
		Map<String, Object> info = getNotiInfo(sbn.getNotification());
		data.putString(ANCSNotification.TITLE, (String) info.get("title"));
		data.putString(ANCSNotification.TEXT, (String) info.get("text"));
		data.putLong(ANCSNotification.POST_TIME, sbn.getPostTime());
		data.putString(ANCSNotification.PACKAGE_NAME, sbn.getPackageName());
		
		if (sbn.getPackageName().equals("com.android.phone") /*&& sbn.getNotification().tickerText != null防止三星的来电通知进入*/) {
			if (sbn.getNotification().tickerText != null /*!android.os.Build.MODEL.equals("Nexus 5")*/) {
				// 未接来电
				// 未接来电不在这里产生了
				return;
				// 未接来电，修改title为电话号码或联系人
				/*String rawText = sbn.getNotification().tickerText.toString();
				int start = -1;
				int end = rawText.length();
				for (int i = 0; i < rawText.length(); i++) {
					char character = rawText.charAt(i);
					System.out.println("character is " + character);
					if (character >= '0' && character <= '9') {
						if (start == -1) {
							start = i;
							end = start + 1;
						} else {
							end++;
						}		
					} else if (character == ' ') {
						end++;
					} else if (start != -1) {
						end = i;
						break;
					}
					
					
				}*/
				
				/*if (rawText.contains("来自")) {
					// google nexus 5
					start = 2;
					end = rawText.indexOf("的");
				} else {
					start = 5;
					end = rawText.length();
				}*/
				
				/*System.out.println("the real title is " + rawText.substring(start, end));
				data.putString(ANCSNotification.TITLE, rawText.substring(start, end));*/
				
			} else {
				// 三星的已接来电会进入，修改packageName以符合google nexus 5
				// note3的拨打电话也会进入
				data.putString(ANCSNotification.PACKAGE_NAME, "com.google.android.dialer");
				// ANCS需要title是联系人或者是号码
				data.putString(ANCSNotification.TITLE, (String) info.get("text"));
				return;
			}
			
			
		} else if (SMS.contains(sbn.getPackageName())) {
			//return;
		} else if (sbn.getPackageName().equals("com.google.android.dialer")) {
			// 来电通知不在这里产生了
			return;
		}
		return;
		/*intent.putExtras(data);
		sendBroadcast(intent);
		System.out.println("Broadcast send success");*/
		
		
		
		/*if (mService != null) {
			System.out.println("sending message");
			Message message = Message.obtain(null, AndroidANCSService.NOTIFICATION_ADDED);
			message.replyTo = null;
			message.obj = sbn;
			try {
			    mService.send(message);
			} catch (RemoteException e) {
			    e.printStackTrace();
			}
		}*/
		
	}


	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		// TODO Auto-generated method stub

		//System.out.println("onNotificationRemoved");
		
		System.out.println("onNotificationRemoved + when = " + sbn.getNotification().when + " tricker text = " + sbn.getNotification().tickerText);
		//System.out.println("notification " + sbn.getNotification().contentView.toString());
		System.out.println("packagename is " + sbn.getPackageName());
		
		
		Intent intent = new Intent("ANCSRemoveNotification");
		Bundle data = new Bundle();
		ANCSNotification ancsn = new ANCSNotification(sbn);

		//data.putParcelable(ANCSNotification.NOTIFICATION, sbn.getNotification());
		data.putLong(ANCSNotification.POST_TIME, sbn.getPostTime());
		data.putString(ANCSNotification.PACKAGE_NAME, sbn.getPackageName());
		if (SMS.contains(sbn.getPackageName())) {
			data.putLong(ANCSNotification.POST_TIME, AndroidANCSService.SMS_POST_TIME);
			//data.putString(ANCSNotification.PACKAGE_NAME, sbn.getPackageName());
		}
		if (sbn.getPackageName().equals("com.android.phone")) {
			if (sbn.getNotification().tickerText == null) {
				// 正在来电 
				data.putLong(ANCSNotification.POST_TIME, AndroidANCSService.INCOMMING_CALL_POST_TIME);
			} else {
				// tickerText != null时为未接来电
				data.putLong(ANCSNotification.POST_TIME, AndroidANCSService.MISSING_CALL_POST_TIME);
			}
		}
		return;
		/*intent.putExtras(data);
		System.out.println("start Broadcast");
		this.sendBroadcast(intent);
		System.out.println("Broadcast send success");*/
		/*if (mService != null) {
			System.out.println("sending message");
			Message message = Message.obtain(null, AndroidANCSService.NOTIFICATION_ADDED);
			message.replyTo = null;
			message.obj = sbn;
			try {
			    mService.send(message);
			} catch (RemoteException e) {
			    e.printStackTrace();
			}
		}*/
		
	}


	/*@Override
	public StatusBarNotification[] getActiveNotifications() {
		// TODO Auto-generated method stub
		System.out.println("getActiveNotifications");
		Toast.makeText(this, "getActiveNotifications", Toast.LENGTH_SHORT).show();
		return super.getActiveNotifications();
	}*/
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		System.out.println("onBind");
		Toast.makeText(this, "onBind", Toast.LENGTH_SHORT).show();
		
		return super.onBind(intent);
		//return mMessenger.getBinder();
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		System.out.println("NotificationService Created...");
	}


	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//unbindService(conn);
		System.out.println("NotificationService Destroy...");
	}
	
	
	private Map<String, Object> getNotiInfo(Notification notification) {
		int key = 0;
		//Notification notification = (Notification) sbn.getNotification();
		Notification.Builder builder;
		if (notification == null)
			return null;
	    RemoteViews views = notification.contentView;
	    if (views == null)
	    	return null;
	    Class secretClass = views.getClass();

	    try {
	        Map<String, Object> text = new HashMap<String, Object>();

	        Field outerFields[] = secretClass.getDeclaredFields();
	        for (int i = 0; i < outerFields.length; i++) {
	            if (!outerFields[i].getName().equals("mActions")) continue;

	            outerFields[i].setAccessible(true);

	            ArrayList<Object> actions = (ArrayList<Object>) outerFields[i]
	                    .get(views);
	            for (Object action : actions) {
	                Field innerFields[] = action.getClass().getDeclaredFields();

	                Object value = null;
	                Integer type = null;
	                Integer viewId = null;
	                for (Field field : innerFields) {
	                    field.setAccessible(true);
	                    if (field.getName().equals("value")) {
	                        value = field.get(action);
	                    } else if (field.getName().equals("type")) {
	                        type = field.getInt(action);
	                    } else if (field.getName().equals("viewId")) {
	                        viewId = field.getInt(action);
	                    }
	                }
	                if (type != null && (type == 9 || type == 10)) {
	                    //text.put(viewId, value.toString());
	                	if (key == 0)
	                		text.put("title", value != null ? value.toString() : "");
	                	else if (key == 1)
	                		text.put("text", value != null ? value.toString() : "");
	                	else 
	                		text.put(Integer.toString(key), value != null ? value.toString() : null);
	                	key++;
	                	
	                }
	            }

	            /*System.out.println("title is: " + text.get(2130837542));
	            System.out.println("info is: " + text.get(2130837543));
	            System.out.println("text is: " + text.get(null));*/
	            
	            	System.out.println("value in text[title] is " + text.get("title") + "\n");
	            	System.out.println("value in text[text] is " + text.get("text") + "\n");
	            
	            key = 0;
	            
	        }
	        return text;
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
	private boolean isZh() {
        Locale locale = getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;
    }
}
