package com.kakao.taxi.auto

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.kakao.taxi.auto.databinding.ActivityMainBinding
import com.kakao.taxi.auto.service.KakaoTaxiAccessibilityService
import com.kakao.taxi.auto.utils.PermissionUtils
import com.kakao.taxi.auto.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted || coarseLocationGranted) {
            checkOverlayPermission()
        } else {
            showSnackbar(getString(R.string.location_permission_required))
        }
    }

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Settings.canDrawOverlays(this)) {
            checkAccessibilityService()
        } else {
            showSnackbar(getString(R.string.overlay_permission_required))
        }
    }

    private val accessibilitySettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (PermissionUtils.isAccessibilityServiceEnabled(this, KakaoTaxiAccessibilityService::class.java)) {
            viewModel.onAllPermissionsGranted()
        } else {
            showSnackbar(getString(R.string.accessibility_service_required))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setupObservers()
        setupClickListeners()
        
        // Check permissions on startup
        checkPermissions()
    }

    private fun setupObservers() {
        viewModel.permissionCheckRequested.observe(this) {
            checkPermissions()
        }
        
        viewModel.snackbarMessage.observe(this) { message ->
            if (message.isNotEmpty()) {
                showSnackbar(message)
            }
        }
        
        viewModel.navigateToSettings.observe(this) { navigate ->
            if (navigate) {
                // Navigate to settings activity
                // startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnGrantPermissions.setOnClickListener {
            checkPermissions()
        }
        
        binding.btnEnableAccessibility.setOnClickListener {
            openAccessibilitySettings()
        }
    }

    private fun checkPermissions() {
        when {
            !hasLocationPermission() -> requestLocationPermission()
            !hasOverlayPermission() -> checkOverlayPermission()
            !hasAccessibilityService() -> checkAccessibilityService()
            else -> viewModel.onAllPermissionsGranted()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || 
        ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun hasAccessibilityService(): Boolean {
        return PermissionUtils.isAccessibilityServiceEnabled(this, KakaoTaxiAccessibilityService::class.java)
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun checkOverlayPermission() {
        if (!hasOverlayPermission()) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        } else {
            checkAccessibilityService()
        }
    }

    private fun checkAccessibilityService() {
        if (!hasAccessibilityService()) {
            viewModel.setAccessibilityServiceRequired(true)
        } else {
            viewModel.onAllPermissionsGranted()
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        accessibilitySettingsLauncher.launch(intent)
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        // Refresh permission status
        viewModel.refreshPermissionStatus(
            hasLocationPermission(),
            hasOverlayPermission(),
            hasAccessibilityService()
        )
    }
}