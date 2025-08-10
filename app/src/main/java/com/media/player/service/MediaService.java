package com.media.player.service;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.media.player.service.worker.WorkerThread;
import com.media.player.service.worker.ListWorker;
import com.media.player.service.utils.MediaItem;
import com.media.player.service.utils.Config;
import com.media.player.service.utils.Helper;
import com.media.player.service.utils.Logger;
import com.media.player.service.utils.DataStore;
import java.util.List;

/**
 * 미디어 보조 서비스
 */
public class MediaService extends AccessibilityService {
    private MediaItem lastItem = null;

    @Override
    public void onInterrupt() {
        // 서비스 중단 처리
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        try {
            AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
            if (rootInActiveWindow != null) {
                if (DataStore.bEnabled && accessibilityEvent.getPackageName().toString().equals(Config.TARGET_PACKAGE)) {
                    processMedia(rootInActiveWindow);
                } else {
                    clearLastItem();
                }
                // 메모리 최적화 - 사용 후 즉시 recycle (원본과 동일)
                rootInActiveWindow.recycle();
            }
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    private void processMedia(AccessibilityNodeInfo accessibilityNodeInfo) {
        MediaItem mediaItem;
        MediaItem item;
        AccessibilityNodeInfo nodeByViewId;
        try {
            // UI 요소 감지
            List<AccessibilityNodeInfo> nodeListByViewId = Helper.getNodeListByViewId(accessibilityNodeInfo, "v_arrow");
            List<AccessibilityNodeInfo> nodeListByViewId2 = Helper.getNodeListByViewId(accessibilityNodeInfo, "lv_call_list");
            
            if (!Helper.isValidNodes(nodeListByViewId) && !Helper.isValidNodes(nodeListByViewId2)) {
                clearLastItem();
            } else if (Helper.isValidNodes(accessibilityNodeInfo.findAccessibilityNodeInfosByText("배차가 완료된 콜입니다.")) 
                    && (nodeByViewId = Helper.getNodeByViewId(accessibilityNodeInfo, "tv_btn_delete_completed")) != null) {
                clearLastItem();
                Helper.delegateButtonClick(nodeByViewId);
            } else {
                // 개별 항목 처리
                if (Helper.isValidNodes(nodeListByViewId) && (mediaItem = Helper.getMediaItem(accessibilityNodeInfo)) != null 
                        && ((item = this.lastItem) == null || !Helper.equalItems(item, mediaItem))) {
                    setLastItem(mediaItem);
                    DataStore.tThreadPool.submit(new WorkerThread(mediaItem, this));
                }
                // 목록 처리
                if (Helper.isValidNodes(nodeListByViewId2)) {
                    clearLastItem();
                    if (DataStore.nMode == Config.MODE_ADVANCED || DataStore.bWorkerRunning) {
                        return;
                    }
                    DataStore.bWorkerRunning = true;
                    DataStore.tThreadPool.submit(new ListWorker(nodeListByViewId2.get(0), this));
                }
            }
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    private void setLastItem(MediaItem item) {
        this.lastItem = item;
    }

    private void clearLastItem() {
        this.lastItem = null;
    }
}