package com.media.player.service.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.media.player.service.BackgroundService
import com.media.player.service.utils.Config
import com.media.player.service.utils.DataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context: Context = application.applicationContext
    
    // UI States
    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning
    
    private val _callMode = MutableStateFlow(CallMode.ALL)
    val callMode: StateFlow<CallMode> = _callMode
    
    private val _selectedDestinations = MutableStateFlow(setOf<String>())
    val selectedDestinations: StateFlow<Set<String>> = _selectedDestinations
    
    private val _distanceRange = MutableStateFlow(3f)
    val distanceRange: StateFlow<Float> = _distanceRange
    
    private val _acceptKeywords = MutableStateFlow(listOf<String>())
    val acceptKeywords: StateFlow<List<String>> = _acceptKeywords
    
    private val _rejectKeywords = MutableStateFlow(listOf<String>())
    val rejectKeywords: StateFlow<List<String>> = _rejectKeywords
    
    init {
        loadSettings()
        checkAccessibilityService()
        
        // 1초마다 접근성 서비스 상태 확인
        viewModelScope.launch {
            while (true) {
                checkAccessibilityService()
                kotlinx.coroutines.delay(1000)
            }
        }
    }
    
    private fun loadSettings() {
        DataStore.loadConfig(context)
        
        // Load saved settings
        _callMode.value = when {
            DataStore.bFullMode -> CallMode.ALL
            DataStore.bAutoSkip -> CallMode.PARTIAL  
            else -> CallMode.STANDBY
        }
        
        // nMode 초기화 - 매우 중요!
        when (_callMode.value) {
            CallMode.ALL -> DataStore.nMode = Config.MODE_ALL
            CallMode.PARTIAL -> DataStore.nMode = Config.MODE_DEST
            CallMode.STANDBY -> DataStore.nMode = Config.MODE_NONE
        }
        
        // Load distance setting from DataStore (안전한 로드)
        val savedPreset = DataStore.sQualityPreset
        val distance = when (savedPreset) {
            "무제한" -> 51f
            "" -> 3f  // 빈 문자열이면 기본값
            else -> {
                val parsed = savedPreset.replace("km", "").toFloatOrNull()
                if (parsed != null && parsed > 0) parsed else 3f
            }
        }
        _distanceRange.value = distance
        
        // 기본값인 경우에만 DataStore 업데이트
        if (savedPreset.isEmpty()) {
            DataStore.sQualityPreset = "3km"
            DataStore.nQuality = 3000
        }
        
        // 디버그 로그 추가
        android.util.Log.d("MainViewModel", "거리 로드: savedPreset='$savedPreset' → distance=$distance")
        
        // Load keywords from DataStore
        DataStore.aFilterList?.let {
            _acceptKeywords.value = it.toList()
        }
    }
    
    fun updateCallMode(mode: CallMode) {
        _callMode.value = mode
        
        // Update DataStore
        when (mode) {
            CallMode.ALL -> {
                DataStore.bFullMode = true
                DataStore.bAutoSkip = false
                DataStore.nMode = Config.MODE_ALL  // 전체콜 모드 설정
            }
            CallMode.PARTIAL -> {
                DataStore.bFullMode = false
                DataStore.bAutoSkip = true
                DataStore.nMode = Config.MODE_DEST  // 도착지 모드 설정
            }
            CallMode.STANDBY -> {
                DataStore.bFullMode = false
                DataStore.bAutoSkip = false
                DataStore.nMode = Config.MODE_NONE  // 대기 모드 설정
            }
        }
        
        DataStore.saveConfig(context)
    }
    
    fun updateDestinations(destinations: Set<String>) {
        _selectedDestinations.value = destinations
        DataStore.saveConfig(context)
    }
    
    fun updateDistance(distance: Float) {
        _distanceRange.value = distance
        // DataStore에 거리 값 저장 (정확한 형태로)
        val distanceKm = if (distance >= 51f) {
            "무제한"
        } else {
            // 0.8km 같은 정확한 형태로 저장
            if (distance == distance.toInt().toFloat()) {
                "${distance.toInt()}km"  // 3.0 → "3km"
            } else {
                "${distance}km"  // 0.8 → "0.8km"
            }
        }
        DataStore.sQualityPreset = distanceKm
        DataStore.nQuality = if (distance >= 51f) 0 else (distance * 1000).toInt()
        
        // 디버그 로그 추가
        android.util.Log.d("MainViewModel", "거리 저장: $distance → $distanceKm (nQuality: ${DataStore.nQuality})")
        
        DataStore.saveConfig(context)
    }
    
    fun addAcceptKeyword(keyword: String) {
        if (keyword.isNotBlank()) {
            _acceptKeywords.value = _acceptKeywords.value + keyword
            updateKeywordsInDataStore()
        }
    }
    
    fun removeAcceptKeyword(keyword: String) {
        _acceptKeywords.value = _acceptKeywords.value - keyword
        updateKeywordsInDataStore()
    }
    
    fun addRejectKeyword(keyword: String) {
        if (keyword.isNotBlank()) {
            _rejectKeywords.value = _rejectKeywords.value + keyword
            updateKeywordsInDataStore()
        }
    }
    
    fun removeRejectKeyword(keyword: String) {
        _rejectKeywords.value = _rejectKeywords.value - keyword
        updateKeywordsInDataStore()
    }
    
    private fun updateKeywordsInDataStore() {
        DataStore.aFilterList = ArrayList(_acceptKeywords.value)
        DataStore.saveConfig(context)
    }
    
    fun toggleService() {
        viewModelScope.launch {
            if (_isServiceRunning.value) {
                stopService()
            } else {
                startService()
            }
        }
    }
    
    fun updateServiceStatus(isRunning: Boolean) {
        _isServiceRunning.value = isRunning
    }
    
    private fun startService() {
        val intent = Intent(context, BackgroundService::class.java)
        context.startService(intent)
        _isServiceRunning.value = true
    }
    
    private fun stopService() {
        val intent = Intent(context, BackgroundService::class.java)
        context.stopService(intent)
        _isServiceRunning.value = false
    }
    
    private fun checkAccessibilityService() {
        // 접근성 서비스 활성화 상태 확인
        val prefString = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        val isAccessibilityEnabled = prefString?.contains(context.packageName + "/" + 
            "com.media.player.service.MediaService") == true
        
        // 접근성 서비스가 활성화되어 있고 DataStore.bEnabled가 true일 때만 실행 중
        _isServiceRunning.value = isAccessibilityEnabled && DataStore.bEnabled
    }
}

enum class CallMode {
    ALL,      // 전체 수락
    PARTIAL,  // 선택 수락
    STANDBY   // 대기
}