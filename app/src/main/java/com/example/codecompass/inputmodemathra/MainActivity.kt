package com.example.codecompass.inputmodemathra

import android.content.Context
import android.content.Intent
import android.graphics.Region
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Display
import android.view.MotionEvent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.codecompass.inputmodemathra.databinding.ActivityMainBinding
import com.example.codecompass.inputmodemathra.ui.LandingPageFragment
import com.example.codecompass.inputmodemathra.utils.AccessibilityUtils
import com.example.codecompass.inputmodemathra.utils.FragmentNavigation
import com.example.codecompass.inputmodemathra.utils.MathsManthraAccessibilityService
import com.example.codecompass.inputmodemathra.utils.PermissionManager
import com.example.codecompass.inputmodemathra.utils.settings.LocaleHelper

class MainActivity : AppCompatActivity(), FragmentNavigation {

    private lateinit var binding: ActivityMainBinding
    private var permissionManager: PermissionManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set immersive full-screen mode
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationIconTint(getColor(com.example.codecompass.inputmodemathra.R.color.white))
        binding.toolbar.setNavigationContentDescription("Back Button")

        // Hide the Up button initially
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        if (savedInstanceState == null) {
            val landingFragment = LandingPageFragment()
            loadFragment(landingFragment, FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        }

        permissionManager = PermissionManager(this, object : PermissionManager.PermissionCallback {
            override fun onPermissionGranted() {
                Log.d("PermissionManager", "Granted!")
            }

            override fun onPermissionDenied() {
                Log.w("PermissionManager", "Denied!")
            }
        })

        permissionManager?.requestMicrophonePermission()

        checkAccessibilityService()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }


    override fun onSupportNavigateUp(): Boolean {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
        } else {
            loadFragment(LandingPageFragment(), FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
        }
        return true
    }

    override fun loadFragment(fragment: Fragment, transition: Int) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.setTransition(transition)
        fragmentTransaction.replace(binding.fragmentContainer.id, fragment)

        if (fragment is LandingPageFragment) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        } else {
            fragmentTransaction.addToBackStack(null)
        }

        fragmentTransaction.commit()
        updateUpButtonVisibility(fragment)
    }

    private fun updateUpButtonVisibility(fragment: Fragment) {
        val actionBar = supportActionBar
        val isHomePage = fragment is LandingPageFragment
        actionBar?.setDisplayHomeAsUpEnabled(!isHomePage)
        actionBar?.setHomeActionContentDescription("Back Button")
    }

    private fun checkAccessibilityService() {
        val isTalkBackOn = accessibilityUtils.isSystemExploreByTouchEnabled(this)
        val isServiceEnabled = AccessibilityUtils.isMathsManthraAccessibilityServiceEnabled(this)

        if (isTalkBackOn && !isServiceEnabled) {
            Log.w("AccessibilityCheck", "TalkBack is ON but service is OFF. Redirecting user.")
            showAccessibilityDialog()
        } else {
            Log.d("AccessibilityCheck", "Accessibility check passed.")
        }
    }

    private fun showAccessibilityDialog() {
        AlertDialog.Builder(this)
            .setTitle("Enable Accessibility Service")
            .setMessage("MathsManthra needs Accessibility Service to function properly. Would you like to enable it?")
            .setPositiveButton("Enable") { _, _ ->
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    fun disableExploreByTouch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && appAccessibilityService != null) {
            val fullScreenRegion = Region(
                0,
                0,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels
            )
            appAccessibilityService?.setTouchExplorationPassthroughRegion(Display.DEFAULT_DISPLAY, fullScreenRegion)
            appAccessibilityService?.setGestureDetectionPassthroughRegion(Display.DEFAULT_DISPLAY, fullScreenRegion)
        }
    }

    fun resetExploreByTouch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && appAccessibilityService != null) {
            val emptyRegion = Region()
            appAccessibilityService?.setTouchExplorationPassthroughRegion(Display.DEFAULT_DISPLAY, emptyRegion)
            appAccessibilityService?.setGestureDetectionPassthroughRegion(Display.DEFAULT_DISPLAY, emptyRegion)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.pointerCount == 2 && event.actionMasked == MotionEvent.ACTION_MOVE) {
            if (event.getY(0) < event.getHistoricalY(0, 0) && event.getY(1) < event.getHistoricalY(1, 0)) {
                onBackPressed()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    companion object {
        private var appAccessibilityService: MathsManthraAccessibilityService? = null
        private val accessibilityUtils: AccessibilityUtils = AccessibilityUtils()
        @JvmStatic
        fun setAccessibilityService(service: MathsManthraAccessibilityService?) {
            appAccessibilityService = service
        }
        @JvmStatic
        fun getAccessibilityUtils(): AccessibilityUtils = accessibilityUtils
        @JvmStatic
        fun updateWindowState() {
            Log.d("MainActivity", "Accessibility Event Triggered: Updating Window State")
        }
    }
}
