package no.toppe.android.credentialautounlock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class CredentialStorageUnlockActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		KeyStore store;
		store = new KeyStore(this);
		boolean isUnlocked = store.isUnlocked();
		
		Log.d("CredUnlockActivity", "Trying to unlock keystore - stopping service first");
		stopService(new Intent(this, CredentialAutoUnlockService.class));
		
		if ( isUnlocked )
		{
			Toast.makeText(this, "Credentials Storage is already unlocked", 4000).show();
		} else {
			startService(new Intent(this, CredentialAutoUnlockService.class));
		}
		finish();
	}
}
