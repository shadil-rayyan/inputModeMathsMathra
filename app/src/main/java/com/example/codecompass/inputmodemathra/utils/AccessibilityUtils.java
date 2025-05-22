package com.example.codecompass.inputmodemathra.utils;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import com.example.codecompass.inputmodemathra.utils.MathsManthraAccessibilityService;

import java.util.List;

public class AccessibilityUtils {

    // Check if TalkBack (Explore by Touch) is enabled
    public boolean isSystemExploreByTouchEnabled(Context context) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager != null) {
            return accessibilityManager.isEnabled() && accessibilityManager.isTouchExplorationEnabled();
        }
        return false;
    }

    // ✅ NEW METHOD - Check if MathsManthraAccessibilityService is enabled in Accessibility Settings
    public static boolean isMathsManthraAccessibilityServiceEnabled(Context context) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager == null) {
            return false;
        }

        List<AccessibilityServiceInfo> enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo serviceInfo : enabledServices) {
            // Important: Match your package + service class name
            if (serviceInfo.getId().contains("com.zendalona.mathmantra/.utils.MathsManthraAccessibilityService")) {
                return true;
            }
        }
        return false;
    }

    // ⚠️ Optional - This is less reliable. You can keep or remove this.
    public static boolean isMathsManthraAccessibilityServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MathsManthraAccessibilityService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // Announce message to TalkBack/Screen Reader
    public static void sendTextToScreenReader(Context context, String message) {
        AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT);
        event.getText().add(message);

        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager != null && accessibilityManager.isEnabled()) {
            accessibilityManager.sendAccessibilityEvent(event);
        }
    }

    // Redirect user to Accessibility settings
    public static void redirectToAccessibilitySettings(Context context) {
        Toast.makeText(context, "Please enable MathsManthra Accessibility Service", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


}
