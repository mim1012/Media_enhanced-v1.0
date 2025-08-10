package com.kakao.taxi.auto.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val _serviceRunning = MutableLiveData<Boolean>(false)
    val serviceRunning: LiveData<Boolean> = _serviceRunning

    private val _locationPermissionGranted = MutableLiveData<Boolean>(false)
    val locationPermissionGranted: LiveData<Boolean> = _locationPermissionGranted

    private val _overlayPermissionGranted = MutableLiveData<Boolean>(false)
    val overlayPermissionGranted: LiveData<Boolean> = _overlayPermissionGranted

    private val _accessibilityServiceEnabled = MutableLiveData<Boolean>(false)
    val accessibilityServiceEnabled: LiveData<Boolean> = _accessibilityServiceEnabled

    private val _allPermissionsGranted = MutableLiveData<Boolean>(false)
    val allPermissionsGranted: LiveData<Boolean> = _allPermissionsGranted

    private val _accessibilityServiceRequired = MutableLiveData<Boolean>(false)
    val accessibilityServiceRequired: LiveData<Boolean> = _accessibilityServiceRequired

    private val _permissionCheckRequested = MutableLiveData<Unit>()
    val permissionCheckRequested: LiveData<Unit> = _permissionCheckRequested

    private val _snackbarMessage = MutableLiveData<String>()
    val snackbarMessage: LiveData<String> = _snackbarMessage

    private val _navigateToSettings = MutableLiveData<Boolean>(false)
    val navigateToSettings: LiveData<Boolean> = _navigateToSettings

    fun refreshPermissionStatus(
        locationGranted: Boolean,
        overlayGranted: Boolean,
        accessibilityEnabled: Boolean
    ) {
        _locationPermissionGranted.value = locationGranted
        _overlayPermissionGranted.value = overlayGranted
        _accessibilityServiceEnabled.value = accessibilityEnabled
        
        val allGranted = locationGranted && overlayGranted && accessibilityEnabled
        _allPermissionsGranted.value = allGranted
        
        if (!accessibilityEnabled) {
            _accessibilityServiceRequired.value = true
        }
    }

    fun onAllPermissionsGranted() {
        _allPermissionsGranted.value = true
        _accessibilityServiceRequired.value = false
        showMessage("모든 권한이 허용되었습니다. 서비스를 시작할 수 있습니다.")
    }

    fun setAccessibilityServiceRequired(required: Boolean) {
        _accessibilityServiceRequired.value = required
    }

    fun toggleService() {
        val currentState = _serviceRunning.value ?: false
        _serviceRunning.value = !currentState
        
        if (!currentState) {
            // Start service
            showMessage("자동화 서비스를 시작합니다...")
        } else {
            // Stop service
            showMessage("자동화 서비스를 중지합니다...")
        }
    }

    fun requestPermissionCheck() {
        _permissionCheckRequested.value = Unit
    }

    fun openSettings() {
        _navigateToSettings.value = true
    }

    private fun showMessage(message: String) {
        _snackbarMessage.value = message
    }
}