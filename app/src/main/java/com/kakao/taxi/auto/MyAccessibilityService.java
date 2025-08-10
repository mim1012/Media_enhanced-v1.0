package com.kakao.taxi.auto;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.kakao.taxi.auto.boiler.BoilerRunnable;
import com.kakao.taxi.auto.boiler.ManagerRunnable;
import com.kakao.taxi.auto.utils.Call;
import com.kakao.taxi.auto.utils.Constants;
import com.kakao.taxi.auto.utils.Helper;
import com.kakao.taxi.auto.utils.LogUtils;
import com.kakao.taxi.auto.utils.SharedData;
import java.util.List;

/**
 * 연구 목적 접근성 서비스
 * 택시 기사 업무 보조를 위한 콜 정보 읽기 및 알림 강화
 */
public class MyAccessibilityService extends AccessibilityService {
    private Call lastGeneralCall = null;

    @Override
    public void onInterrupt() {
        // 서비스 중단시 처리
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        try {
            AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
            if (rootInActiveWindow != null) {
                if (SharedData.bAuto && accessibilityEvent.getPackageName().toString().equals(Constants.TAXI_DRIVER_APP_PACKAGE)) {
                    processKakao(rootInActiveWindow);
                } else {
                    clearLastGeneralCall();
                }
                rootInActiveWindow.recycle();
            }
        } catch (Exception e) {
            LogUtils.uploadLog(e);
        }
    }

    private void processKakao(AccessibilityNodeInfo accessibilityNodeInfo) {
        Call kakaoGeneralCall;
        Call call;
        AccessibilityNodeInfo nodeByViewId;
        try {
            // 개별 콜 화면 감지
            List<AccessibilityNodeInfo> nodeListByViewId = Helper.getNodeListByViewId(accessibilityNodeInfo, "v_arrow");
            // 콜 목록 화면 감지
            List<AccessibilityNodeInfo> nodeListByViewId2 = Helper.getNodeListByViewId(accessibilityNodeInfo, "lv_call_list");
            
            if (!Helper.isValidNodes(nodeListByViewId) && !Helper.isValidNodes(nodeListByViewId2)) {
                clearLastGeneralCall();
            } else if (Helper.isValidNodes(accessibilityNodeInfo.findAccessibilityNodeInfosByText("배차가 완료된 콜입니다.")) 
                    && (nodeByViewId = Helper.getNodeByViewId(accessibilityNodeInfo, "tv_btn_delete_completed")) != null) {
                clearLastGeneralCall();
                Helper.delegateButtonClick(nodeByViewId);
            } else {
                // 개별 콜 처리
                if (Helper.isValidNodes(nodeListByViewId) && (kakaoGeneralCall = Helper.getKakaoGeneralCall(accessibilityNodeInfo)) != null 
                        && ((call = this.lastGeneralCall) == null || !Helper.equalGeneralCalls(call, kakaoGeneralCall))) {
                    setLastGeneralCall(kakaoGeneralCall);
                    SharedData.tWorkThreadPool.submit(new BoilerRunnable(kakaoGeneralCall, this));
                }
                // 콜 목록 처리
                if (Helper.isValidNodes(nodeListByViewId2)) {
                    clearLastGeneralCall();
                    if (SharedData.nWorkMode == Constants.MODE_LONGDISTANCE || SharedData.bManagerThreadRunning) {
                        return;
                    }
                    SharedData.bManagerThreadRunning = true;
                    SharedData.tWorkThreadPool.submit(new ManagerRunnable(nodeListByViewId2.get(0), this));
                }
            }
        } catch (Exception e) {
            LogUtils.uploadLog(e);
        }
    }

    private void setLastGeneralCall(Call call) {
        this.lastGeneralCall = call;
    }

    private void clearLastGeneralCall() {
        this.lastGeneralCall = null;
    }
}