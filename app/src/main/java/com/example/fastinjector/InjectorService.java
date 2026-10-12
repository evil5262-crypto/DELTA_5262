package com.example.fastinjector;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;

public class InjectorService extends AccessibilityService {

    private WindowManager windowManager;
    private Button floatingButton;
    private SharedPreferences prefs;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isInjecting = false;

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        prefs = getSharedPreferences("FastInjectorPrefs", MODE_PRIVATE);
        createFloatingButton();
    }

    private void createFloatingButton() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatingButton = new Button(this);
        floatingButton.setText("\uD83E\uDDA0");
        floatingButton.setTextSize(20);
        floatingButton.setAlpha(0.7f);

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
        params.y = 300;

        floatingButton.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            private boolean isMoving = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        isMoving = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) (event.getRawX() - initialTouchX);
                        int dy = (int) (event.getRawY() - initialTouchY);
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            isMoving = true;
                        }
                        params.x = initialX + dx;
                        params.y = initialY + dy;
                        windowManager.updateViewLayout(floatingButton, params);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!isMoving) {
                            clickConfirmButtonOnly();
                        }
                        return true;
                }
                return false;
            }
        });

        windowManager.addView(floatingButton, params);
    }

    private void onEditTextClicked() {
        if (isInjecting) return;
        isInjecting = true;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null) {
            AccessibilityNodeInfo editNode = findEditText(root);
            if (editNode != null) {
                String text = prefs.getString("inject_text", "");
                android.os.Bundle args = new android.os.Bundle();
                args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
                editNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
            }
        }

        handler.postDelayed(() -> {
            floatingButton.performClick();
            isInjecting = false;
        }, 3);
    }

    private void clickConfirmButtonOnly() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        AccessibilityNodeInfo confirmNode = findConfirmButton(root);
        if (confirmNode != null) {
            confirmNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
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

    private AccessibilityNodeInfo findConfirmButton(AccessibilityNodeInfo node) {
        if (node == null) return null;

        CharSequence text = node.getText();
        if (text != null) {
            String t = text.toString();
            if (t.contains("\u062A\u0623\u06CC\u06CC\u062F") || t.contains("\u062A\u0627\u06CC\u06CC\u062F") || t.contains("OK") || t.contains("Confirm")) {
                return node;
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo result = findConfirmButton(node.getChild(i));
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            AccessibilityNodeInfo source = event.getSource();
            if (source != null && source.isEditable()) {
                onEditTextClicked();
            }
        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingButton != null && windowManager != null) {
            windowManager.removeView(floatingButton);
        }
    }
}

