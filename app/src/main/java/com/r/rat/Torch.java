package com.r.rat;
import android.content.Context;
import android.hardware.camera2.CameraManager;
public class Torch {
    private static CameraManager cm; private static String id;
    private static void init(Context c) throws Exception {
        if (cm == null) { cm = (CameraManager) c.getSystemService(Context.CAMERA_SERVICE); id = cm.getCameraIdList()[0]; }
    }
    public static void on(Context c) throws Exception { init(c); cm.setTorchMode(id, true); }
    public static void off(Context c) throws Exception { init(c); cm.setTorchMode(id, false); }
}