package no.toppe.android.credentialautounlock;

import java.lang.reflect.Method;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class CredentialAutoUnlockActivity extends Activity {
    /** Called when the activity is first created. */
	public String TAG = "CredUnlock";
	
	private NotificationManager mNotificationManager;
	private TextView InfoText;
	private static final int HELLO_ID = 1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Get the notification manager service.
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Notification notification = new Notification(R.drawable.icon, "Unlocking Credential Storage", System.currentTimeMillis());
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        InfoText = (TextView) findViewById(R.id.InfoText);
        InfoText.setText("Unlocking keystore. Please enter key");
        
        KeyStore store = new KeyStore(this);
        store.unlock(this);

        boolean isUnlocked = store.isUnlocked();
        String unlockText = "Keystore state is: " + ( isUnlocked ? "Unlocked" : "Locked");
        InfoText.setText(unlockText);

        Intent notificationIntent;
        if ( isUnlocked )
        {
        	notificationIntent = new Intent();
        } else {
        	notificationIntent = new Intent(this, CredentialAutoUnlockActivity.class);
        	
        	String notificationText = "Keystore is locked. Press to unlock";
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            notification.setLatestEventInfo(getApplicationContext(), "Credential Storage Status", notificationText, contentIntent);
            mNotificationManager.notify(HELLO_ID, notification);
        }
        
        Toast.makeText(this, unlockText , 4000).show();
		finish();
    }
}

class KeyStore extends AbstractWrapper {

	public String TAG = "CredUnlock";
    public static final String UNLOCK_ACTION = "android.credentials.UNLOCK";
    private static final int NO_ERROR = 1;

    /*
    private static final int LOCKED = 2;
    private static final int UNINITIALIZED = 3;
    private static final int SYSTEM_ERROR = 4;
    private static final int PROTOCOL_ERROR = 5;
    private static final int PERMISSION_DENIED = 6;
    private static final int KEY_NOT_FOUND = 7;
    private static final int VALUE_CORRUPTED = 8;
    private static final int UNDEFINED_ACTION = 9;
    private static final int WRONG_PASSWORD = 10;
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

    public void unlock(final Activity ctx) {
        try {
            Intent intent = new Intent(UNLOCK_ACTION);
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