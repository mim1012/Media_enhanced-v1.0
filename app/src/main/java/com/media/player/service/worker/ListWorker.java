package com.media.player.service.worker;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityNodeInfo;
import com.media.player.service.utils.MediaItem;
import com.media.player.service.utils.Helper;
import com.media.player.service.utils.Logger;
import com.media.player.service.utils.DataStore;
import java.util.ArrayList;
import java.util.concurrent.Future;

/**
 * 리스트 워커 (원본: ManagerRunnable)
 * 콜 목록 처리 핵심 비즈니스 로직 - 토씨 하나 안 빼고 완전 이식
 */
public class ListWorker implements Runnable {
    private final Context mContext;
    private final AccessibilityNodeInfo mListWindowNode;

    public ListWorker(AccessibilityNodeInfo accessibilityNodeInfo, Context context) {
        this.mListWindowNode = accessibilityNodeInfo;
        this.mContext = context;
    }

    @Override
    public void run() {
        try {
            try {
                doWork();
                doScroll();
            } catch (Exception e) {
                Logger.log(e);
            }
        } finally {
            DataStore.bWorkerRunning = false;
        }
    }

    private void doWork() throws Exception {
        AccessibilityNodeInfo parent;
        ArrayList<Future> arrayList = new ArrayList();
        
        // 원본 ManagerRunnable.doWork() 로직 완전 이식
        for (AccessibilityNodeInfo accessibilityNodeInfo : Helper.getNodeListByViewId(this.mListWindowNode, "tv_destination")) {
            AccessibilityNodeInfo parent2 = accessibilityNodeInfo.getParent();
            if (parent2 != null && (parent = parent2.getParent()) != null) {
                AccessibilityNodeInfo nodeByViewId = Helper.getNodeByViewId(parent, "tv_origin_label_distance");
                AccessibilityNodeInfo nodeByViewId2 = Helper.getNodeByViewId(parent, "ll_accept_btn");
                if (nodeByViewId != null && nodeByViewId2 != null) {
                    String str = nodeByViewId.getText().toString() + " : " + accessibilityNodeInfo.getText().toString();
                    if (!DataStore.aProcessedList.contains(str)) {
                        DataStore.addProcessedText(str);
                        MediaItem item = Helper.getKakaoListCall(accessibilityNodeInfo, nodeByViewId, nodeByViewId2);
                        if (item != null) {
                            arrayList.add(DataStore.tThreadPool.submit(new WorkerThread(item, this.mContext)));
                        }
                    }
                }
            }
        }
        
        // 모든 Future 대기
        for (Future future : arrayList) {
            future.get();
        }
    }

    private void doScroll() {
        // 원본 ManagerRunnable.doScroll() 로직 완전 이식
        AccessibilityNodeInfo accessibilityNodeInfo = this.mListWindowNode;
        if (accessibilityNodeInfo != null && accessibilityNodeInfo.getActionList().contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)) {
            int recycleHeight = Helper.getRecycleHeight(this.mListWindowNode);
            final Bundle bundle = new Bundle();
            bundle.putInt("ACTION_ARGUMENT_MOVE_WINDOW_Y", recycleHeight);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mListWindowNode.performAction(4096, bundle);  // ACTION_SCROLL_FORWARD = 4096
                    mListWindowNode.recycle();
                }
            });
            return;
        }
        this.mListWindowNode.recycle();
    }
}