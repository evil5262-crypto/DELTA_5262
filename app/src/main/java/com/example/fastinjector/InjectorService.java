
package com.example.fastinjector;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class InjectorService extends AccessibilityService {

    private SharedPreferences prefs;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isInjecting = false;

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        prefs = getSharedPreferences("FastInjectorPrefs", MODE_PRIVATE);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_FOCUSED
                || event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {

            AccessibilityNodeInfo source = event.getSource();
            if (source != null && source.isEditable() && !isInjecting) {
                isInjecting = true;
                injectTextAndConfirm();
            }
        }
    }

    private void injectTextAndConfirm() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            isInjecting = false;
            return;
        }

        AccessibilityNodeInfo editNode = findEditText(root);
        if (editNode != null) {
            String text = prefs.getString("inject_text", "");
            android.os.Bundle args = new android.os.Bundle();
            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            editNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
        }

        handler.postDelayed(() -> {
            clickConfirmButton();
            isInjecting = false;
        }, 3);
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

    private void clickConfirmButton() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;
        AccessibilityNodeInfo confirmNode = findConfirmButton(root);
        if (confirmNode != null) {
            confirmNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
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
    public void onInterrupt() {
    }
}
