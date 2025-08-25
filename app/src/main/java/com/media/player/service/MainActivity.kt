package com.media.player.service

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import android.content.pm.ResolveInfo
import android.app.ActivityManager
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.media.player.service.ui.theme.MediaPlayerTheme
import com.media.player.service.ui.screen.*
import com.media.player.service.ui.screen.TemplateLoadScreen
import com.media.player.service.ui.dialog.SaveTemplateDialog
import com.media.player.service.utils.Config
import com.media.player.service.utils.DataStore
import com.media.player.service.utils.Preset
import com.media.player.service.viewmodel.MainViewModel
import com.media.player.service.viewmodel.CallMode
import com.media.player.service.auth.AuthManager
import com.media.player.service.utils.StatsReporter
import android.content.BroadcastReceiver
import android.content.IntentFilter

class MainActivity : ComponentActivity() {
    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
    
    private lateinit var notificationReceiver: BroadcastReceiver
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 인증 확인 (최초 1회)
        AuthManager.checkAuthentication(this, object : AuthManager.AuthCallback {
            override fun onSuccess(userType: String, message: String) {
                // 인증 성공 - 정상 초기화 진행
                initializeApp()
                
                // 앱 시작 통계 전송
                StatsReporter.reportAppStart(this@MainActivity)
            }
            
            override fun onFailure(message: String) {
                // 인증 실패 - AuthActivity로 리다이렉트
                val intent = Intent(this@MainActivity, com.media.player.service.ui.activity.AuthActivity::class.java)
                intent.putExtra("error_message", message)
                startActivity(intent)
                finish()
            }
        })
    }
    
    /**
     * 앱 초기화 (인증 성공 후)
     */
    private fun initializeApp() {
        // 설정 로드 (매우 안전한 초기화)
        try {
            DataStore.loadConfig(this)
            android.util.Log.d("MainActivity", "DataStore 로드 성공")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "DataStore 로드 실패", e)
            // 초기화 실패 시 최소한의 기본값만 설정
            DataStore.bEnabled = false
            DataStore.nMode = 0
            DataStore.bFullMode = false
            DataStore.nQuality = 3000
            DataStore.sQualityPreset = "3km"
            DataStore.aFilterList = ArrayList()
            DataStore.aPresetList = ArrayList()
        }
        
        // 알림 채널 생성
        NotificationService.createNotificationChannel(this)
        
        // BroadcastReceiver 다시 활성화 (안전한 버전)
        setupNotificationReceiver()
        
        // 백그라운드 서비스 자동 시작 (원본처럼)
        startBackgroundService()
        
        // 권한 체크
        checkPermissions()
        
        // 접근성 서비스 체크  
        checkAccessibilityService()
        
        setContent {
            MediaPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp()
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // 앱이 포그라운드로 올 때마다 서비스 상태 업데이트
        updateServiceStatus()
        
        // ViewModel에도 서비스 상태 업데이트
        val viewModel = (application as? android.app.Application)?.let {
            androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(it)
                .create(MainViewModel::class.java)
        }
        viewModel?.updateServiceStatus(isAccessibilityServiceEnabled())
    }
    
    @Composable
    fun MainApp() {
        val navController = rememberNavController()
        val viewModel: MainViewModel = viewModel()
        val callMode by viewModel.callMode.collectAsState()
        val isServiceRunning by viewModel.isServiceRunning.collectAsState()
        val selectedDestinations by viewModel.selectedDestinations.collectAsState()
        val distanceRange by viewModel.distanceRange.collectAsState()
        
        // 다이얼로그 상태
        var showSaveTemplateDialog by remember { mutableStateOf(false) }
        
        NavHost(
            navController = navController,
            startDestination = "main"
        ) {
            composable("main") {
                MainScreen(
                    onCallModeClick = { navController.navigate("callMode") },
                    onDestinationClick = { navController.navigate("destination") },
                    onDistanceClick = { navController.navigate("distance") },
                    onKeywordClick = { navController.navigate("keyword") },
                    onLoadDestinationClick = {
                        navController.navigate("templateLoad")
                    },
                    onSaveTemplateClick = {
                        showSaveTemplateDialog = true
                    },
                    onStartClick = {
                        if (isServiceRunning) {
                            // 일시정지 (실행 중 → 중지)
                            DataStore.bEnabled = false
                            viewModel.updateServiceStatus(false)
                            NotificationService.showStoppedNotification(this@MainActivity)
                            Toast.makeText(this@MainActivity, "⏸️ 자동 재생 일시정지", Toast.LENGTH_SHORT).show()
                        } else {
                            // 재생 시작 (중지 → 실행)
                            DataStore.bEnabled = true
                            when(callMode) {
                                CallMode.ALL -> DataStore.nMode = Config.MODE_ALL
                                CallMode.PARTIAL -> DataStore.nMode = Config.MODE_DEST
                                CallMode.STANDBY -> DataStore.nMode = Config.MODE_NONE
                            }
                            viewModel.updateServiceStatus(true)
                            NotificationService.showRunningNotification(this@MainActivity)
                            Toast.makeText(this@MainActivity, "▶️ 자동 재생 시작 ✅ [모드: ${when(callMode) {
                                CallMode.ALL -> "전체 재생모드"
                                CallMode.PARTIAL -> "선택 재생모드"
                                CallMode.STANDBY -> "대기"
                            }}]", Toast.LENGTH_SHORT).show()
                            startKakaoTaxiApp()
                        }
                    },
                    onStopClick = {
                        // 완전 중지
                        DataStore.bEnabled = false
                        viewModel.updateServiceStatus(false)
                        NotificationService.hideNotification(this@MainActivity)  // 알림 완전 제거
                        Toast.makeText(this@MainActivity, "⏹️ 완전 중지", Toast.LENGTH_SHORT).show()
                    },
                    currentDistance = if (distanceRange >= 51f) "무제한" else {
                        if (distanceRange == distanceRange.toInt().toFloat()) {
                            "${distanceRange.toInt()}km"  // 3.0 → "3km"
                        } else {
                            "${distanceRange}km"  // 0.8 → "0.8km"
                        }
                    },
                    currentCallMode = when(callMode) {
                        CallMode.ALL -> "전체 재생모드"
                        CallMode.PARTIAL -> "선택 재생모드"
                        CallMode.STANDBY -> "대기 모드"
                    },
                    destinationCount = selectedDestinations.size,
                    isServiceRunning = isServiceRunning  // ViewModel 상태 사용
                )
            }
            
            composable("callMode") {
                CallModeSelector(
                    selectedMode = when(callMode) {
                        com.media.player.service.viewmodel.CallMode.ALL -> com.media.player.service.ui.screen.CallMode.ALL
                        com.media.player.service.viewmodel.CallMode.PARTIAL -> com.media.player.service.ui.screen.CallMode.PARTIAL
                        com.media.player.service.viewmodel.CallMode.STANDBY -> com.media.player.service.ui.screen.CallMode.STANDBY
                    },
                    onModeSelected = { mode ->
                        when(mode) {
                            com.media.player.service.ui.screen.CallMode.ALL -> viewModel.updateCallMode(com.media.player.service.viewmodel.CallMode.ALL)
                            com.media.player.service.ui.screen.CallMode.PARTIAL -> viewModel.updateCallMode(com.media.player.service.viewmodel.CallMode.PARTIAL)
                            com.media.player.service.ui.screen.CallMode.STANDBY -> viewModel.updateCallMode(com.media.player.service.viewmodel.CallMode.STANDBY)
                        }
                    },
                    onSave = { mode ->
                        when(mode) {
                            com.media.player.service.ui.screen.CallMode.ALL -> viewModel.updateCallMode(com.media.player.service.viewmodel.CallMode.ALL)
                            com.media.player.service.ui.screen.CallMode.PARTIAL -> viewModel.updateCallMode(com.media.player.service.viewmodel.CallMode.PARTIAL)
                            com.media.player.service.ui.screen.CallMode.STANDBY -> viewModel.updateCallMode(com.media.player.service.viewmodel.CallMode.STANDBY)
                        }
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable("destination") {
                DestinationScreen(
                    onSave = { 
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() },
                    onSaveTemplate = {
                        showSaveTemplateDialog = true
                    }
                )
            }
            
            composable("distance") {
                DistanceScreen(
                    initialDistance = distanceRange,
                    onSave = { distance ->
                        viewModel.updateDistance(distance)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable("keyword") {
                KeywordScreen(
                    onSave = { 
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable("templateLoad") {
                TemplateLoadScreen(
                    onBack = { navController.popBackStack() },
                    onTemplateSelected = { preset ->
                        try {
                            // 선택된 템플릿 적용
                            preset.applyToSettings()
                            DataStore.saveConfig(this@MainActivity)
                            
                            // ViewModel 상태 업데이트
                            when {
                                preset.fullMode -> viewModel.updateCallMode(CallMode.ALL)
                                preset.nMode == Config.MODE_DEST -> viewModel.updateCallMode(CallMode.PARTIAL)  
                                else -> viewModel.updateCallMode(CallMode.STANDBY)
                            }
                            
                            // 거리 설정 업데이트
                            val distance = if (preset.qualityPreset == "무제한") 51f
                                         else preset.qualityPreset.replace("km", "").toFloatOrNull() ?: 3f
                            viewModel.updateDistance(distance)
                            
                            navController.popBackStack()
                            Toast.makeText(
                                this@MainActivity,
                                "템플릿 '${preset.name}' 적용완료 ✅",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@MainActivity,
                                "템플릿 적용 실패: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onTemplateDeleted = { preset ->
                        try {
                            DataStore.aPresetList.remove(preset)
                            DataStore.saveConfig(this@MainActivity)
                            Toast.makeText(
                                this@MainActivity,
                                "템플릿 '${preset.name}' 삭제완료 🗑️",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@MainActivity,
                                "템플릿 삭제 실패: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
        }
        
        // 템플릿 저장 다이얼로그
        if (showSaveTemplateDialog) {
            SaveTemplateDialog(
                onDismiss = { showSaveTemplateDialog = false },
                onSave = { templateName ->
                    try {
                        // 현재 설정을 템플릿으로 저장
                        val preset = Preset.fromCurrentSettings(templateName)
                        DataStore.aPresetList.add(preset)
                        DataStore.saveConfig(this@MainActivity)
                        
                        showSaveTemplateDialog = false
                        Toast.makeText(
                            this@MainActivity, 
                            "템플릿 '$templateName' 저장완료 ✅", 
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@MainActivity, 
                            "템플릿 저장 실패: ${e.message}", 
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                currentCallMode = when(callMode) {
                    CallMode.ALL -> "전체 재생모드"
                    CallMode.PARTIAL -> "선택 재생모드"
                    CallMode.STANDBY -> "설정 대기"
                },
                currentDistance = if (distanceRange >= 51f) "무제한" else {
                    if (distanceRange == distanceRange.toInt().toFloat()) {
                        "${distanceRange.toInt()}km"  // 3.0 → "3km"
                    } else {
                        "${distanceRange}km"  // 0.8 → "0.8km"
                    }
                }
            )
        }
    }
    
    /**
     * 상태창 알림과 앱 연동을 위한 BroadcastReceiver 설정
     */
    private fun setupNotificationReceiver() {
        try {
            notificationReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    try {
                        when (intent.action) {
                            "com.media.player.service.ACTION_START" -> {
                                // 상태바에서 시작 버튼 클릭됨 → 앱 상태 업데이트
                                DataStore.bEnabled = true
                                android.util.Log.d("MainActivity", "상태바에서 시작 - 앱 상태 업데이트")
                            }
                            "com.media.player.service.ACTION_STOP" -> {
                                // 상태바에서 중지 버튼 클릭됨 → 앱 상태 업데이트
                                DataStore.bEnabled = false
                                android.util.Log.d("MainActivity", "상태바에서 중지 - 앱 상태 업데이트")
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "BroadcastReceiver 에러", e)
                    }
                }
            }
            
            val filter = IntentFilter().apply {
                addAction("com.media.player.service.ACTION_START")
                addAction("com.media.player.service.ACTION_STOP")
            }
            registerReceiver(notificationReceiver, filter)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "BroadcastReceiver 등록 실패", e)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(notificationReceiver)
        } catch (e: Exception) {
            // 이미 해제된 경우 무시
        }
    }
    
    private fun startBackgroundService() {
        val intent = Intent(this, BackgroundService::class.java)
        startService(intent)
    }
    
    private fun updateServiceStatus() {
        // 접근성 서비스가 활성화되어 있으면 서비스가 동작 중인 것으로 간주
        val isRunning = isAccessibilityServiceEnabled()
        // DataStore의 bEnabled도 업데이트
        DataStore.bEnabled = isRunning
    }
    
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
    
    private fun checkPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO
        )
        
        // Android 13+ 알림 권한 추가
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        // Android 10+ 백그라운드 위치 권한 추가  
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            showPermissionGuideDialog(permissionsToRequest.toTypedArray())
        } else {
            // 모든 권한이 허용된 경우 접근성 서비스 체크
            checkAccessibilityService()
        }
    }
    
    private fun showPermissionGuideDialog(permissions: Array<String>) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("권한 허용 안내")
            .setMessage("앱이 정상 작동하려면 다음 권한들이 필요합니다:\n\n" +
                    "• 위치: 택시 콜 거리 계산\n" +
                    "• 알림: 서비스 상태 표시\n" +
                    "• 마이크: 접근성 서비스\n\n" +
                    "다음 화면에서 '항상 허용' 또는 '앱 사용 중에만 허용'을 선택해주세요.")
            .setPositiveButton("권한 허용") { _, _ ->
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
            }
            .setNegativeButton("나중에") { _, _ ->
                Toast.makeText(this, "권한 허용 후 이용 가능합니다.", Toast.LENGTH_LONG).show()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun checkAccessibilityService() {
        if (!isAccessibilityServiceEnabled()) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("🔧 접근성 서비스 설정")
                .setMessage("카카오 택시 자동 수락을 위해 접근성 서비스가 필요합니다.\n\n" +
                        "설정 방법:\n" +
                        "1. 접근성 설정 화면으로 이동\n" +
                        "2. 'Media Player' 찾기\n" +
                        "3. 사용 설정을 '켜기'로 변경\n" +
                        "4. 앱으로 돌아오기\n\n" +
                        "⚠️ 이 설정은 카카오 택시 앱에서만 작동합니다.")
                .setPositiveButton("설정하러 가기") { _, _ ->
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    Toast.makeText(this, "Media Player를 찾아서 켜주세요", Toast.LENGTH_LONG).show()
                }
                .setNegativeButton("나중에") { _, _ ->
                    Toast.makeText(this, "접근성 서비스 활성화 후 자동 수락이 가능합니다.", Toast.LENGTH_LONG).show()
                }
                .setCancelable(false)
                .show()
        }
    }
    
    private fun isAccessibilityServiceEnabled(): Boolean {
        val prefString = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return prefString?.contains(packageName + "/" + MediaService::class.java.name) == true
    }
    
    private fun startKakaoTaxiApp() {
        try {
            val kakaoPackage = "com.kakao.taxi.driver"
            val mainActivity = "com.kakao.taxi.driver.presentation.main.MainActivity"
            
            // 정확한 액티비티로 직접 실행
            val intent = Intent().apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = android.content.ComponentName(kakaoPackage, mainActivity)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            
            startActivity(intent)
            Toast.makeText(this, "카카오 택시 드라이버 실행", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            // 폴백: 패키지로 실행 시도
            try {
                val launchIntent = packageManager.getLaunchIntentForPackage("com.kakao.taxi.driver")
                if (launchIntent != null) {
                    startActivity(launchIntent)
                    Toast.makeText(this, "카카오 택시 드라이버 실행", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "카카오 택시 드라이버 앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (ex: Exception) {
                Toast.makeText(this, "앱 실행에 실패했습니다. 수동으로 실행해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val deniedPermissions = permissions.filterIndexed { index, _ ->
                grantResults[index] != PackageManager.PERMISSION_GRANTED
            }
            
            if (deniedPermissions.isNotEmpty()) {
                // 권한 거부시 설정으로 안내
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("권한 설정 필요")
                    .setMessage("일부 권한이 거부되었습니다.\n앱 정상 작동을 위해 설정에서 권한을 허용해주세요.\n\n" +
                            "설정 → 앱 → Media Player → 권한")
                    .setPositiveButton("설정으로 이동") { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", packageName, null)
                        }
                        startActivity(intent)
                    }
                    .setNegativeButton("확인") { _, _ ->
                        // 권한이 없어도 접근성 서비스 체크는 진행
                        checkAccessibilityService()
                    }
                    .show()
            } else {
                // 모든 권한이 허용된 경우
                Toast.makeText(this, "권한 설정이 완료되었습니다!", Toast.LENGTH_SHORT).show()
                checkAccessibilityService()
            }
        }
    }
}