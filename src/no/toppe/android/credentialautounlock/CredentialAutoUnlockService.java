package no.toppe.android.credentialautounlock;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class CredentialAutoUnlockService extends Service {
	/** Called when the activity is first created. */
	public String LOGTAG = "CredUnlockService";

	private NotificationManager mNotificationManager;
	private static final int HELLO_ID = 1;
	KeyStore store;
	
	/**
	 * Delay until first execution of the Log task.
	 */
	private final long mDelay = 0;
	/**
	 * Period of the Log task.
	 */
	private final long mPeriod = 2000;

	/**
	 * Timer to schedule the service.
	 */
	private Timer mTimer;

	/**
	 * Implementation of the timer task.
	 */
	
	protected boolean unlock()
	{		
		boolean isUnlocked = store.isUnlocked();
		String unlockText = "Keystore state is: "
			+ (isUnlocked ? "Unlocked" : "Locked");
		Log.d(LOGTAG, "Periodic Check. " + unlockText);
		
		if ( !isUnlocked )
		{
			Notification notification = new Notification(R.drawable.icon,
					"Unlocking Credential Storage", System.currentTimeMillis());
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			
			store.unlock(this);
			
			isUnlocked = store.isUnlocked();
			
			Intent notificationIntent = new Intent(this,
						CredentialAutoUnlockService.class);
	
			String notificationText = "Keystore is locked. Press to unlock";
			PendingIntent contentIntent = PendingIntent.getService(this, 0,
					notificationIntent, 0);
			notification.setLatestEventInfo(getApplicationContext(),
					"Credential Storage Status", notificationText,
					contentIntent);
			mNotificationManager.notify(HELLO_ID, notification);
	
			//Toast.makeText(this, unlockText, 4000).show();
		} else {
			mNotificationManager.cancel(HELLO_ID);
			mTimer.cancel();
			Log.d(LOGTAG, "Stopping periodic check");
			stopService(new Intent(this, CredentialAutoUnlockService.class));
		}
		
		return isUnlocked;
	}
	
	private class LogTask extends TimerTask {
		public void run() {
			unlock();
		}
	}

	private LogTask mLogTask;

	@Override
	public IBinder onBind(final Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(LOGTAG, "created");
		
		store = new KeyStore(this);
		
		// Get the notification manager service.
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		mTimer = new Timer();
		mLogTask = new LogTask();
	}

	@Override
	public void onStart(final Intent intent, final int startId) {
		super.onStart(intent, startId);
		Log.i(LOGTAG, "started");
		mTimer.schedule(mLogTask, mDelay, mPeriod);
	}
}

class KeyStore extends AbstractWrapper {

	public String TAG = "CredUnlock";
	public static final String UNLOCK_ACTION = "android.credentials.UNLOCK";
	private static final int NO_ERROR = 1;

	/*
	 * private static final int LOCKED = 2; private static final int
	 * UNINITIALIZED = 3; private static final int SYSTEM_ERROR = 4; private
	 * static final int PROTOCOL_ERROR = 5; private static final int
	 * PERMISSION_DENIED = 6; private static final int KEY_NOT_FOUND = 7;
	 * private static final int VALUE_CORRUPTED = 8; private static final int
	 * UNDEFINED_ACTION = 9; private static final int WRONG_PASSWORD = 10;
	 */

	public KeyStore(final Context ctx) {
		super(ctx, "android.security.KeyStore");
	}

	@Override
	protected Object createStubObject(final Class<?> clazz) throws Exception {
		return getStubInstance();
	}

	private Object getStubInstance() throws Exception {
		Method method = getStubClass().getMethod("getInstance");
		return method.invoke(null);
	}

	public boolean put(final String key, final String value) {
		return this.<Boolean> invokeStubMethod("put", key, value);
	}

	public boolean delete(final String key) {
		return this.<Boolean> invokeStubMethod("delete", key);
	}

	public void unlock(final Context ctx) {
		try {
			Intent intent = new Intent(UNLOCK_ACTION);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			ctx.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "unlock credentials failed", e);
		}
	}

	public boolean isUnlocked() {
		int err = this.<Integer> invokeStubMethod("test");
		Log.d(TAG, "KeyStore.test result is: " + err);
		return err == NO_ERROR;
	}
}