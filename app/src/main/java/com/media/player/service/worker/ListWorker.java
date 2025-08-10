package com.media.player.service.worker;

import android.content.Context;
import android.view.accessibility.AccessibilityNodeInfo;
import com.media.player.service.utils.MediaItem;
import com.media.player.service.utils.Helper;
import com.media.player.service.utils.Logger;
import com.media.player.service.utils.DataStore;
import java.util.List;

/**
 * 리스트 워커 (원본: ManagerRunnable)
 * 콜 목록 처리 핵심 비즈니스 로직
 */
public class ListWorker implements Runnable {
    AccessibilityNodeInfo listNode;
    Context context;
    String mLogText;

    public ListWorker(AccessibilityNodeInfo listNode, Context context) {
        this.listNode = listNode;
        this.context = context;
    }

    @Override
    public void run() {
        long currentTimeMillis = System.currentTimeMillis();
        this.mLogText = "";
        
        try {
            processList();
        } catch (Exception e) {
            this.mLogText += "리스트 처리 중 오류: " + e.getMessage() + "\n";
            Logger.log(e);
        }
        
        String str = this.mLogText + "\t--소요시간--: " + (System.currentTimeMillis() - currentTimeMillis) + "ms";
        Logger.log(str);
        
        // 처리 완료 후 플래그 리셋
        DataStore.bWorkerRunning = false;
    }

    private void processList() throws Exception {
        this.mLogText += "리스트 모드 시작\n";
        
        if (this.listNode == null) {
            this.mLogText += "리스트 노드가 null입니다\n";
            return;
        }
        
        // 리스트 아이템 높이 계산
        int recycleHeight = Helper.getRecycleHeight(this.listNode);
        if (recycleHeight <= 0) {
            this.mLogText += "리스트 높이를 가져올 수 없습니다\n";
            return;
        }
        
        // 리스트의 자식 노드들 순회
        int childCount = this.listNode.getChildCount();
        this.mLogText += "리스트 아이템 개수: " + childCount + "\n";
        
        for (int i = 0; i < childCount && i < 10; i++) {  // 최대 10개까지만 처리
            try {
                AccessibilityNodeInfo childNode = this.listNode.getChild(i);
                if (childNode == null) {
                    continue;
                }
                
                // 각 리스트 아이템에서 콜 정보 추출
                MediaItem item = Helper.getListItem(childNode);
                if (item != null) {
                    this.mLogText += "리스트 아이템 " + i + ": " + item.mTarget + " (" + item.mQuality + ")\n";
                    
                    // 이미 처리된 콜인지 확인
                    String itemKey = item.mTarget + "_" + item.mQuality;
                    if (DataStore.aProcessedList.contains(itemKey)) {
                        this.mLogText += "이미 처리된 콜입니다: " + itemKey + "\n";
                        continue;
                    }
                    
                    // 거리 체크
                    int distance = Helper.getCallDistance(item);
                    if (DataStore.nQuality != 0 && distance > DataStore.nQuality) {
                        this.mLogText += "거리 초과: " + distance + "m > " + DataStore.nQuality + "m\n";
                        continue;
                    }
                    
                    // 우선 제외지 체크
                    boolean isExcluded = false;
                    if (DataStore.aExclusionList != null) {
                        for (String excludePlace : DataStore.aExclusionList) {
                            if (item.mTarget.contains(excludePlace)) {
                                this.mLogText += "제외지에 포함: " + excludePlace + "\n";
                                isExcluded = true;
                                break;
                            }
                        }
                    }
                    if (isExcluded) {
                        continue;
                    }
                    
                    // 우선 선호지 체크
                    boolean isPreferred = false;
                    if (DataStore.aPlaylistItems != null) {
                        for (String preferPlace : DataStore.aPlaylistItems) {
                            if (item.mTarget.contains(preferPlace)) {
                                this.mLogText += "선호지에 포함: " + preferPlace + "\n";
                                isPreferred = true;
                                break;
                            }
                        }
                    }
                    
                    // 조건 만족시 클릭
                    if (isPreferred || (DataStore.nMode == 768)) {  // 선호지이거나 전체콜 모드
                        if (item.mPlayCtrl != null && item.mPlayCtrl.isClickable()) {
                            Helper.delegateButtonClick(item.mPlayCtrl);
                            this.mLogText += "콜 수락: " + item.mTarget + "\n";
                            
                            // 처리 완료 목록에 추가
                            DataStore.addProcessedText(itemKey);
                            
                            // 하나만 처리하고 종료 (연속 클릭 방지)
                            Thread.sleep(500);
                            break;
                        }
                    }
                }
                
                // 자식 노드 재활용
                if (childNode != null) {
                    childNode.recycle();
                }
                
            } catch (Exception e) {
                this.mLogText += "리스트 아이템 처리 중 오류: " + e.getMessage() + "\n";
            }
        }
        
        // 스크롤 처리 (필요시)
        if (childCount >= 10) {
            this.mLogText += "리스트가 길어 스크롤이 필요할 수 있습니다\n";
            // 스크롤 액션 수행
            try {
                this.listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                Thread.sleep(300);
            } catch (Exception e) {
                this.mLogText += "스크롤 실패: " + e.getMessage() + "\n";
            }
        }
        
        // 리스트 노드 재활용
        if (this.listNode != null) {
            this.listNode.recycle();
        }
    }
}