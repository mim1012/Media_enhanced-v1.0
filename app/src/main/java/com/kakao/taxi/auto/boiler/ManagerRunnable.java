package com.kakao.taxi.auto.boiler;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityNodeInfo;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import com.kakao.taxi.auto.utils.Helper;
import com.kakao.taxi.auto.utils.LogUtils;
import com.kakao.taxi.auto.utils.SharedData;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 리스트 형태의 콜 목록을 관리하는 매니저 클래스
 * 여러 콜을 동시에 처리하고 스크롤을 통해 추가 콜을 확인합니다.
 */
public class ManagerRunnable implements Runnable {
    private final Context mContext;
    private final AccessibilityNodeInfo mListWindowNode;

    public ManagerRunnable(AccessibilityNodeInfo listWindowNode, Context context) {
        this.mListWindowNode = listWindowNode;
        this.mContext = context;
    }

    @Override
    public void run() {
        try {
            try {
                doWork();
                doScroll();
            } catch (Exception e) {
                LogUtils.uploadLog(e);
            }
        } finally {
            SharedData.bManagerThreadRunning = false;
        }
    }

    /**
     * 현재 화면에 표시된 콜 목록을 처리
     */
    private void doWork() throws ExecutionException, InterruptedException {
        ArrayList<Future> futureList = new ArrayList<>();
        
        // tv_destination을 가진 모든 노드를 찾아서 처리
        for (AccessibilityNodeInfo destNode : Helper.getNodeListByViewId(this.mListWindowNode, "tv_destination")) {
            AccessibilityNodeInfo parentNode = destNode.getParent();
            if (parentNode != null) {
                AccessibilityNodeInfo grandParentNode = parentNode.getParent();
                if (grandParentNode != null) {
                    // 거리 정보와 수락 버튼 노드 찾기
                    AccessibilityNodeInfo distanceNode = Helper.getNodeByViewId(grandParentNode, "tv_origin_label_distance");
                    AccessibilityNodeInfo acceptBtnNode = Helper.getNodeByViewId(grandParentNode, "ll_accept_btn");
                    
                    if (distanceNode != null && acceptBtnNode != null) {
                        // 중복 처리 방지를 위한 키 생성
                        String callKey = distanceNode.getText().toString() + " : " + destNode.getText().toString();
                        
                        if (!SharedData.aProcessedCallList.contains(callKey)) {
                            SharedData.addProcessedCallText(callKey);
                            // BoilerCallable을 사용하여 비동기 처리
                            futureList.add(SharedData.tWorkThreadPool.submit(
                                new BoilerCallable(destNode, distanceNode, acceptBtnNode, this.mContext)));
                        }
                    }
                }
            }
        }
        
        // 모든 비동기 작업 완료 대기
        for (Future future : futureList) {
            future.get();
        }
    }

    /**
     * 리스트를 스크롤하여 추가 콜 확인
     */
    private void doScroll() {
        if (mListWindowNode != null && 
            mListWindowNode.getActionList().contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)) {
            
            int recycleHeight = Helper.getRecycleHeight(mListWindowNode);
            final Bundle scrollBundle = new Bundle();
            scrollBundle.putInt(AccessibilityNodeInfoCompat.ACTION_ARGUMENT_MOVE_WINDOW_Y, recycleHeight);
            
            // 메인 스레드에서 스크롤 실행
            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    mListWindowNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD, scrollBundle);
                } catch (Exception e) {
                    LogUtils.uploadLog("스크롤 중 오류: " + e.getMessage());
                } finally {
                    if (mListWindowNode != null) {
                        mListWindowNode.recycle();
                    }
                }
            });
        } else {
            // 스크롤할 수 없으면 노드 리사이클
            if (mListWindowNode != null) {
                mListWindowNode.recycle();
            }
        }
    }

    /**
     * 개별 콜을 처리하는 Callable 클래스
     * 각 콜을 별도 스레드에서 분석하고 처리합니다.
     */
    private static class BoilerCallable implements Runnable {
        private final AccessibilityNodeInfo destNode;
        private final AccessibilityNodeInfo distanceNode;
        private final AccessibilityNodeInfo acceptBtnNode;
        private final Context context;

        public BoilerCallable(AccessibilityNodeInfo destNode, AccessibilityNodeInfo distanceNode, 
                             AccessibilityNodeInfo acceptBtnNode, Context context) {
            this.destNode = destNode;
            this.distanceNode = distanceNode;
            this.acceptBtnNode = acceptBtnNode;
            this.context = context;
        }

        @Override
        public void run() {
            try {
                // Helper를 사용하여 Call 객체 생성
                com.kakao.taxi.auto.utils.Call call = Helper.getKakaoListCall(destNode, distanceNode, acceptBtnNode);
                
                if (call != null) {
                    // BoilerRunnable을 사용하여 콜 처리
                    new BoilerRunnable(call, context).run();
                } else {
                    LogUtils.uploadLog("콜 파싱 실패: 노드 정보 부족");
                }
            } catch (Exception e) {
                LogUtils.uploadLog("BoilerCallable 실행 중 오류: " + e.getMessage());
            }
        }
    }
}