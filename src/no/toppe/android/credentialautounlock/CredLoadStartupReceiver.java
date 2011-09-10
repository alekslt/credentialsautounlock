package no.toppe.android.credentialautounlock;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CredLoadStartupReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, CredentialAutoUnlockService.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
   }
}