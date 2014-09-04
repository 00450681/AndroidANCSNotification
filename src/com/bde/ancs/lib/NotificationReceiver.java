package com.bde.ancs.lib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {

	private ReveiveCallback mCallback;
	public NotificationReceiver(ReveiveCallback callback) {
		
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		mCallback.doOnReceive(context, intent);
		
	}

	public interface ReveiveCallback {
		void doOnReceive(Context context, Intent intent);
	}
}
