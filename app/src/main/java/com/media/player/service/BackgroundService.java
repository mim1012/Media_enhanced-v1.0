package com.media.player.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import androidx.core.app.NotificationCompat;
import com.media.player.service.utils.Config;
import com.media.player.service.utils.DataStore;
import com.media.player.service.utils.Logger;

/**
 * 백그라운드 서비스 (원본: StealthBackgroundService)
 * 볼륨 버튼으로 은밀하게 제어
 */
public class BackgroundService extends Service {
    private static final int NOTIFICATION_ID = Config.NOTIFICATION_ID;
    private AudioManager audioManager;
    private int previousVolume = -1;
    private VolumeContentObserver volumeObserver;
    
    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        
        // 볼륨 변화 감지를 위한 ContentObserver 등록
        volumeObserver = new VolumeContentObserver(new Handler());
        getContentResolver().registerContentObserver(
            Settings.System.CONTENT_URI, true, volumeObserver
        );
        
        // 초기 볼륨 저장
        previousVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        
        // 포그라운드 서비스 시작
        startForegroundService();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;  // 서비스가 종료되어도 자동 재시작
    }
    
    private void startForegroundService() {
        createNotificationChannel();
        
        Intent notificationIntent = new Intent(this, PlayerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, 
            notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        
        Notification notification = new NotificationCompat.Builder(this, Config.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("재생 중")
            .setContentText("백그라운드에서 실행 중...")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build();
        
        startForeground(NOTIFICATION_ID, notification);
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                Config.NOTIFICATION_CHANNEL_ID,
                Config.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("미디어 재생 서비스");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    
    /**
     * 볼륨 변화 감지 - 핵심 은밀 제어 로직
     */
    private class VolumeContentObserver extends ContentObserver {
        public VolumeContentObserver(Handler handler) {
            super(handler);
        }
        
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            
            if (!DataStore.bVolumeControl) {
                return;  // 볼륨 컨트롤 비활성화 상태
            }
            
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            
            if (previousVolume == -1) {
                previousVolume = currentVolume;
                return;
            }
            
            // 볼륨 UP = 자동화 시작 (은밀한 제어)
            if (currentVolume > previousVolume && !DataStore.bEnabled) {
                DataStore.bEnabled = true;
                Logger.log("Volume UP detected - Service enabled");
                updateNotification("활성화됨");
            }
            // 볼륨 DOWN = 자동화 중지 (은밀한 제어)
            else if (currentVolume < previousVolume && DataStore.bEnabled) {
                DataStore.bEnabled = false;
                Logger.log("Volume DOWN detected - Service disabled");
                updateNotification("비활성화됨");
            }
            
            previousVolume = currentVolume;
        }
    }
    
    /**
     * 볼륨 버튼 이벤트 리시버 (대체 방법)
     */
    public class VolumeControlReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!DataStore.bVolumeControl) {
                return;
            }
            
            String action = intent.getAction();
            if ("android.media.VOLUME_CHANGED_ACTION".equals(action)) {
                int streamType = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1);
                if (streamType == AudioManager.STREAM_MUSIC) {
                    int newVolume = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", 0);
                    
                    // 볼륨 업 = 자동화 시작 (매우 창의적!)
                    if (newVolume > previousVolume) {
                        DataStore.bEnabled = true;
                        updateNotification("활성화됨");
                    } 
                    // 볼륨 다운 = 자동화 중지
                    else if (newVolume < previousVolume) {
                        DataStore.bEnabled = false;
                        updateNotification("비활성화됨");
                    }
                    
                    previousVolume = newVolume;
                }
            }
        }
    }
    
    private void updateNotification(String status) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        
        Intent notificationIntent = new Intent(this, PlayerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, 
            notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        
        Notification notification = new NotificationCompat.Builder(this, Config.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Media Player")
            .setContentText(status)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build();
        
        manager.notify(NOTIFICATION_ID, notification);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (volumeObserver != null) {
            getContentResolver().unregisterContentObserver(volumeObserver);
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}