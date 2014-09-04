package com.bde.ancs.lib;

import java.io.Serializable;

import android.app.Notification;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;

public class ANCSNotification {

	public static final String NOTIFICATION = "notification";
	public static final String POST_TIME = "postTime";
	public static final String PACKAGE_NAME = "packageName";
	public static final String TITLE = "title";
	public static final String TEXT = "text";
	
	//private Notification notification;
	private String packageName, title, text;
	private long PostTime;
	
	public ANCSNotification(StatusBarNotification sbn) {
		//notification = sbn.getNotification();
		packageName = sbn.getPackageName();
		PostTime = sbn.getPostTime();
	}
	
	public ANCSNotification() {

	}

	/*public Notification getNotification() {
		return notification;
	}

	public void setNotification(Notification notification) {
		this.notification = notification;
	}*/

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public long getPostTime() {
		return PostTime;
	}

	public void setPostTime(long postTime) {
		PostTime = postTime;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	
}
