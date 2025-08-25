package com.media.player.service.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.media.player.service.ui.theme.MediaPlayerTheme
import com.media.player.service.auth.AuthManager
import com.media.player.service.MainActivity

/**
 * 인증 화면 - 최초 1회 인증 또는 차단 화면
 */
class AuthActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MediaPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthScreen()
                }
            }
        }
        
        // 인증 시작
        performAuthentication()
    }
    
    @Composable
    fun AuthScreen() {
        val currentAuthState by authState
        val currentMessage by authMessage
        val currentUserType by userType
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 로고
            Text(
                text = "🎵",
                fontSize = 64.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Media Player",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            when (currentAuthState) {
                "checking" -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = currentMessage,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
                
                "success" -> {
                    Text(
                        text = "✅",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "인증 완료!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = currentMessage,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(2000)
                        // 메인 화면으로 이동
                        startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                        finish()
                    }
                }
                
                "failed" -> {
                    Text(
                        text = "🚫",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "접근 거부",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = currentMessage,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(20.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = { 
                            finish()
                            android.os.Process.killProcess(android.os.Process.myPid())
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("앱 종료")
                    }
                }
            }
        }
        
        // 인증 상태 업데이트를 위한 LaunchedEffect
        LaunchedEffect(currentAuthState, currentMessage, currentUserType) {
            // 상태 변경 시 UI 업데이트
        }
    }
    
    // 인증 상태 변수들
    private var authState = mutableStateOf("checking")
    private var authMessage = mutableStateOf("인증 확인 중...")
    private var userType = mutableStateOf("")
    
    /**
     * 인증 수행
     */
    private fun performAuthentication() {
        AuthManager.checkAuthentication(this, object : AuthManager.AuthCallback {
            override fun onSuccess(userType: String, message: String) {
                runOnUiThread {
                    authState.value = "success"
                    authMessage.value = message
                    this@AuthActivity.userType.value = userType
                }
            }
            
            override fun onFailure(message: String) {
                runOnUiThread {
                    authState.value = "failed"
                    authMessage.value = message
                }
            }
        })
    }
}