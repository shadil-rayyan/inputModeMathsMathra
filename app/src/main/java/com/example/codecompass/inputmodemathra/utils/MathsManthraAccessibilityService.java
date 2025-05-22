package com.example.codecompass.inputmodemathra.utils;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.example.codecompass.inputmodemathra.MainActivity;

public class MathsManthraAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // This method is called when an accessibility event occurs
        Log.d("KKKK onAccessibilityEvent", "WINDOW_STATE_CHANGED " + event);

        // Check if the accessibility event is triggered due to a window state change
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName().toString();
            if ("com.zendalona.mathmantra".equals(packageName)) {
                Log.d("AAAA onAccessibilityEvent", "WINDOW_STATE_CHANGED " + event);
            }
        }

        // Handle the event when a view receives accessibility focus
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {
            String packageName = event.getPackageName().toString();
            if ("com.zendalona.mathmantra".equals(packageName)) {
                Log.d("AAAA onAccessibilityEvent", "TYPE_VIEW_ACCESSIBILITY_FOCUSED " + event);
                MainActivity.updateWindowState();
            }
        }
    }

    @Override
    public void onInterrupt() {
        // This method is called when the service is interrupted
        // Implement logic to handle interruptions, if needed
    }

    @Override
    protected void onServiceConnected() {
        // onServiceConnected() is called when the accessibility service is successfully enabled
        super.onServiceConnected();

        // Set this service in MainActivity for use
        MainActivity.setAccessibilityService(this);

        // Notify user that the accessibility service is activated
        Toast.makeText(this, "Accessibility Service Activated", Toast.LENGTH_SHORT).show();
    }


}
