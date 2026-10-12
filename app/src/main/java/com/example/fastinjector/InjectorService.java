package com.example.fastinjector;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;

public class InjectorService extends AccessibilityService {

    private WindowManager windowManager;
    private View floatingView;
    private SharedPreferences prefs;

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        prefs = getSharedPreferences("FastInjectorPrefs", MODE_PRIVATE);
        createFloatingButton();
    }

    private void createFloatingButton() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        Button button = new Button(this);
        button.setText("\uD83E\uDDA0");
        button.setTextSize(24);

        int layoutType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 100;

        button.setOnClickListener(v -> injectText());

        windowManager.addView(button, params);
        floatingView = button;
    }

    private void injectText() {
        AccessibilityNodeInfo focusNode = getRootInActiveWindow();
        if (focusNode == null) return;

        AccessibilityNodeInfo editNode = findEditText(focusNode);
        if (editNode != null) {
            String text = prefs.getString("inject_text", "");
            android.os.Bundle args = new android.os.Bundle();
            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            editNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
        }
    }

    private AccessibilityNodeInfo findEditText(AccessibilityNodeInfo node) {
        if (node == null) return null;
        if (node.isEditable()) return node;

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            AccessibilityNodeInfo result = findEditText(child);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null && windowManager != null) {
            windowManager.removeView(floatingView);
        }
    }
}
