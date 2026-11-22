package com.nsmod.blockblast;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;

public class TapAccessibilityService extends AccessibilityService {
    private static TapAccessibilityService instance;
    public static TapAccessibilityService get() { return instance; }
    @Override public void onServiceConnected() { super.onServiceConnected(); instance=this; }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
    @Override public void onDestroy() { if(instance==this) instance=null; super.onDestroy(); }

    public void drag(float fromX, float fromY, float toX, float toY) {
        if (Build.VERSION.SDK_INT < 24) return;
        Path path = new Path();
        path.moveTo(fromX, fromY);
        path.lineTo(toX, toY);
        GestureDescription.StrokeDescription stroke =
            new GestureDescription.StrokeDescription(path, 0, 500);
        dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
    }
}
