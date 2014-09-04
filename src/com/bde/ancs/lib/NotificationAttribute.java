package com.bde.ancs.lib;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Notification;
import android.service.notification.StatusBarNotification;
import android.widget.RemoteViews;

public class NotificationAttribute {

	
	
	
	private int mAttributeID;
	private boolean mIsNeededParameter = false;
	//private HashMap<String, Attribute> mAttributes;
	
	public int getmAttributeID() {
		return mAttributeID;
	}

	/*public void setAttributes(HashMap<String, Attribute> attributes) {
		mAttributes = attributes;
	}*/
	public void setAttributeID(int attributeID) {
		mAttributeID = attributeID;
		switch (mAttributeID) {
		case 0:
			mIsNeededParameter = false;
			break;
		case 1:
			mIsNeededParameter = true;
			break;
		case 2:
			mIsNeededParameter = true;
			break;
		case 3:
			mIsNeededParameter = true;
			break;
		case 4:
			mIsNeededParameter = false;
			break;
		case 5:
			mIsNeededParameter = false;
			break;
		default:
			mIsNeededParameter = false;
			break;
		}
	}

	public boolean isNeededParameter() {
		return mIsNeededParameter;
	}


	public byte[] doCommand(byte []parameter, /*StatusBarNotification sbn*/ANCSNotification sbn, HashMap<String, Attribute> attributes) throws UnsupportedEncodingException {
		int maxLength = 0;
		
		if (parameter != null && parameter.length > 0) {
			maxLength = (parameter[0] & 0xFF) | ((parameter[1] & 0xFF) << 8);
			System.out.println("Get parameter " + parameter);
		}
		if (mIsNeededParameter && maxLength <=0) {
			System.out.println("parameter is null! It's not allow...");
			return null;
		}
		byte []ret = null;
		switch (mAttributeID) {
		case 0:
			System.out.println("Get app identifier");
			String name = sbn.getPackageName();
			String appIdentifier = name;
			Attribute attribute = attributes.get(name);
			if (attribute != null) {
				appIdentifier = attribute.getAppIdentifier();
			}
			/*if (appIdentifier.equalsIgnoreCase("com.android.phone")) {
				appIdentifier = "com.apple.mobilephone";
			} else */if (appIdentifier.equalsIgnoreCase("com.bde.ancs.androidancs")) {
				appIdentifier = "com.apple.mobilephone";
				//appIdentifier = "com.apple.MobileSMS";
			}
			ret = appIdentifier.getBytes("UTF-8");
			
			break;
			
		case 1:
			System.out.println("Get Title");
			
			//String title = (String) getNotiInfo(sbn.getNotification()).get("title");
			String title = sbn.getTitle();
			ret = title.getBytes();
			if (ret.length > maxLength) {
				byte []temp = ret;
				ret = new byte[maxLength];
				for (int i = 0; i < maxLength;i++) {
					ret[i] = temp[i];
				}
			}
			break;
		case 2:
			System.out.println("Get Subtitle");
			break;
		case 3:
			System.out.println("Get Message");
			//String msg = (String) getNotiInfo(sbn.getNotification()).get("text");
			String msg = sbn.getText();
			ret = msg.getBytes("utf-8");

			//ret = msg.getBytes();
			if (ret.length > maxLength) {
				byte []temp = ret;
				ret = new byte[maxLength];
				for (int i = 0; i < maxLength;i++) {
					ret[i] = temp[i];
				}
			}
			break;
			
		case 4:
			System.out.println("Get MessageSize");
			//msg = (String) getNotiInfo(sbn.getNotification()).get("text");
			msg = sbn.getText();
			String messageSize = Integer.toString(msg.length());
			ret = messageSize.getBytes();
			break;
		case 5:
			System.out.println("Get Date");
			SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddThhmmss");     
		    String   date = sDateFormat.format(new java.util.Date());
		    ret = date.getBytes();
			break;
		}
		return ret;
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

	                if (type == 9 || type == 10) {
	                    //text.put(viewId, value.toString());
	                	if (key == 0)
	                		text.put("title", value.toString());
	                	else if (key == 1)
	                		text.put("text", value.toString());
	                	else 
	                		text.put(Integer.toString(key), value.toString());
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
	
	private Map<String, Object> getNotiInfo(StatusBarNotification sbn) {
		int key = 0;
		Notification notification = (Notification) sbn.getNotification();
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

	                if (type == 9 || type == 10) {
	                    //text.put(viewId, value.toString());
	                	if (key == 0)
	                		text.put("title", value.toString());
	                	else if (key == 1)
	                		text.put("text", value.toString());
	                	else 
	                		text.put(Integer.toString(key), value.toString());
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
}
