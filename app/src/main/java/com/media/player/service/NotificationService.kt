package com.media.player.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.media.player.service.utils.Config
import com.media.player.service.utils.DataStore

/**
 * 상태창 알림 서비스 - 음악 플레이어 스타일 위젯
 */
object NotificationService {
    
    private const val NOTIFICATION_ID = 1001
    private const val CHANNEL_ID = "media_service_channel"
    
    // 액션 상수
    private const val ACTION_PLAY = "action_play"
    private const val ACTION_PAUSE = "action_pause" 
    private const val ACTION_STOP = "action_stop"
    
    /**
     * 알림 채널 생성 (Android 8.0+)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Media Player Service",
                NotificationManager.IMPORTANCE_LOW  // 조용한 알림
            ).apply {
                description = "음악 재생 상태 표시"
                setSound(null, null)  // 무음
                enableVibration(false)  // 진동 끄기
                setShowBadge(false)  // 뱃지 끄기
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 실행 중 알림 표시 (파란색) - 미디어 플레이어로 위장
     */
    fun showRunningNotification(context: Context) {
        val notification = createNotification(
            context = context,
            title = "🎵 Media Player",
            content = "자동 재생 중 - ${getDisguisedStatusMessage()}",
            isRunning = true
        )
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    /**
     * 중지됨 알림 표시 (빨간색) - 미디어 플레이어로 위장
     */
    fun showStoppedNotification(context: Context) {
        val notification = createNotification(
            context = context,
            title = "🎵 Media Player",
            content = "자동 재생 중지됨 - 터치로 시작",
            isRunning = false
        )
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    /**
     * 알림 제거
     */
    fun hideNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
    
    /**
     * 알림 생성 (음악 플레이어 스타일)
     */
    private fun createNotification(
        context: Context,
        title: String, 
        content: String,
        isRunning: Boolean
    ): Notification {
        
        // 메인 액티비티로 이동하는 인텐트
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context, 0, mainIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 시작/중지 액션 버튼
        val actionIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = if (isRunning) ACTION_PAUSE else ACTION_PLAY
        }
        val actionPendingIntent = PendingIntent.getBroadcast(
            context, 0, actionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 완전 중지 액션
        val stopIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_media_play)  // 안전한 기본 아이콘
            .setContentIntent(mainPendingIntent)
            .setOngoing(isRunning)  // 실행 중일 때는 지울 수 없게
            .setAutoCancel(!isRunning)  // 중지 중일 때는 터치로 제거 가능
            .setColor(if (isRunning) 0xFF2196F3.toInt() else 0xFFF44336.toInt())  // 파랑/빨강
            .addAction(
                if (isRunning) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isRunning) "중지" else "시작",
                actionPendingIntent
            )
            .addAction(
                android.R.drawable.ic_delete,
                "종료", 
                stopPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    /**
     * 현재 상태 메시지 생성
     */
    private fun getStatusMessage(): String {
        return when {
            DataStore.bFullMode -> "전체콜 모드 - 모든 콜 수락"
            DataStore.nMode == Config.MODE_DEST -> "부분콜 모드 - 조건부 수락"
            else -> "대기 모드"
        } + " | 거리: ${DataStore.sQualityPreset}"
    }
    
    /**
     * 위장된 상태 메시지 (미디어 플레이어처럼)
     */
    private fun getDisguisedStatusMessage(): String {
        return when {
            DataStore.bFullMode -> "전체 재생모드"
            DataStore.nMode == Config.MODE_DEST -> "선택 재생모드"
            else -> "대기 상태"
        } + " | 거리: ${DataStore.sQualityPreset}"
    }
}

/**
 * 알림 버튼 액션 처리
 */
class NotificationActionReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "action_play" -> {
                // 시작 - 앱 버튼과 완전히 동일한 동작
                DataStore.bEnabled = true
                
                // nMode 설정 (앱과 동일)
                when {
                    DataStore.bFullMode -> DataStore.nMode = Config.MODE_ALL
                    else -> DataStore.nMode = Config.MODE_DEST  // 기본적으로 부분콜 모드
                }
                
                NotificationService.showRunningNotification(context)
                
                // 카카오택시 자동 실행
                try {
                    val kakaoIntent = Intent().apply {
                        action = Intent.ACTION_MAIN
                        addCategory(Intent.CATEGORY_LAUNCHER)
                        component = android.content.ComponentName("com.kakao.taxi.driver", "com.kakao.taxi.driver.presentation.main.MainActivity")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    context.startActivity(kakaoIntent)
                } catch (e: Exception) {
                    // 카카오택시 실행 실패해도 계속 진행
                }
                
                // MainActivity에 시작 신호 전송
                val startIntent = Intent("com.media.player.service.ACTION_START")
                context.sendBroadcast(startIntent)
            }
            "action_pause" -> {
                // 일시정지 - 앱 버튼과 동일
                DataStore.bEnabled = false
                NotificationService.showStoppedNotification(context)
                
                // MainActivity에 중지 신호 전송
                val stopIntent = Intent("com.media.player.service.ACTION_STOP")
                context.sendBroadcast(stopIntent)
            }
            "action_stop" -> {
                // 완전 중지 - 앱 버튼과 동일
                DataStore.bEnabled = false
                NotificationService.hideNotification(context)
                
                // 백그라운드 서비스도 중지
                val serviceIntent = Intent(context, BackgroundService::class.java)
                context.stopService(serviceIntent)
            }
        }
    }
}