package com.kakao.taxi.auto;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.kakao.taxi.auto.databinding.ActivityMainBinding;
import com.kakao.taxi.auto.utils.Constants;
import com.kakao.taxi.auto.utils.SharedData;
import com.kakao.taxi.auto.utils.Template;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private static final int PERMISSION_REQUEST_CODE = 100;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // 설정 로드
        SharedData.loadConfig(this);
        
        // UI 초기화
        initializeUI();
        
        // 권한 체크
        checkPermissions();
        
        // 접근성 서비스 체크
        checkAccessibilityService();
    }
    
    private void initializeUI() {
        // 거리 설정 ChipGroup 초기화
        setupDistanceChips();
        
        // 전체콜/부분콜 모드 스위치
        binding.switchCallMode.setChecked(SharedData.bFullCallMode);
        binding.switchCallMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedData.bFullCallMode = isChecked;
            updateCallModeUI(isChecked);
            SharedData.saveConfig(this);
        });
        
        // 자동 거절 스위치
        binding.switchAutoDeny.setChecked(SharedData.bAutoDeny);
        binding.switchAutoDeny.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedData.bAutoDeny = isChecked;
            SharedData.saveConfig(this);
        });
        
        // 볼륨 컨트롤 스위치
        binding.switchVolumeControl.setChecked(SharedData.bEnableVolume);
        binding.switchVolumeControl.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedData.bEnableVolume = isChecked;
            SharedData.saveConfig(this);
        });
        
        // 키워드 필터 입력
        binding.editKeywords.setText(SharedData.parseArray2Str(SharedData.aKeywordFilterList));
        
        // 도착지 설정 버튼
        binding.btnDestinationSettings.setOnClickListener(v -> openDestinationSettings());
        
        // 제외지 설정 버튼
        binding.btnExclusionSettings.setOnClickListener(v -> openExclusionSettings());
        
        // 템플릿 버튼들
        binding.btnSaveTemplate.setOnClickListener(v -> saveTemplate());
        binding.btnLoadTemplate.setOnClickListener(v -> loadTemplate());
        
        // 서비스 시작/중지 버튼
        binding.btnToggleService.setOnClickListener(v -> toggleService());
        
        // 초기 UI 상태 업데이트
        updateCallModeUI(SharedData.bFullCallMode);
        updateServiceStatus();
    }
    
    private void setupDistanceChips() {
        ChipGroup chipGroup = binding.chipGroupDistance;
        chipGroup.removeAllViews();
        
        for (String distance : SharedData.DISTANCE_PRESETS) {
            Chip chip = new Chip(this);
            chip.setText(distance);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(true);
            
            if (distance.equals(SharedData.sDistancePreset)) {
                chip.setChecked(true);
            }
            
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    SharedData.sDistancePreset = distance;
                    if (distance.equals("무제한")) {
                        SharedData.nCallDistance = 0;
                    } else {
                        String distStr = distance.replace("km", "").trim();
                        try {
                            float dist = Float.parseFloat(distStr);
                            SharedData.nCallDistance = (int)(dist * 1000);
                        } catch (NumberFormatException e) {
                            SharedData.nCallDistance = 1000;
                        }
                    }
                    SharedData.saveConfig(this);
                    updateDistanceDisplay();
                }
            });
            
            chipGroup.addView(chip);
        }
        
        chipGroup.setSingleSelection(true);
    }
    
    private void updateDistanceDisplay() {
        String distanceText = SharedData.sDistancePreset;
        if (!distanceText.equals("무제한")) {
            distanceText += " (" + SharedData.nCallDistance + "m)";
        }
        binding.tvCurrentDistance.setText("현재 거리: " + distanceText);
    }
    
    private void updateCallModeUI(boolean isFullCallMode) {
        if (isFullCallMode) {
            binding.tvCallModeStatus.setText("전체콜 모드 - 거리 제한 없음");
            binding.cardDistanceSettings.setAlpha(0.5f);
            binding.cardFilterSettings.setAlpha(0.5f);
            binding.chipGroupDistance.setEnabled(false);
            binding.editKeywords.setEnabled(false);
        } else {
            binding.tvCallModeStatus.setText("부분콜 모드 - 조건에 따라 수락");
            binding.cardDistanceSettings.setAlpha(1.0f);
            binding.cardFilterSettings.setAlpha(1.0f);
            binding.chipGroupDistance.setEnabled(true);
            binding.editKeywords.setEnabled(true);
        }
    }
    
    private void saveTemplate() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_save_template, null);
        TextInputEditText editName = dialogView.findViewById(R.id.edit_template_name);
        
        builder.setTitle("템플릿 저장")
                .setView(dialogView)
                .setPositiveButton("저장", (dialog, which) -> {
                    String name = editName.getText().toString().trim();
                    if (!name.isEmpty()) {
                        // 키워드 저장
                        String keywords = binding.editKeywords.getText().toString();
                        SharedData.aKeywordFilterList = SharedData.parseStr2Array(keywords);
                        
                        Template template = Template.fromCurrentSettings(name);
                        if (SharedData.aTemplateList.size() >= 5) {
                            SharedData.aTemplateList.remove(0);
                        }
                        SharedData.aTemplateList.add(template);
                        SharedData.saveConfig(this);
                        Toast.makeText(this, "템플릿이 저장되었습니다", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }
    
    private void loadTemplate() {
        if (SharedData.aTemplateList.isEmpty()) {
            Toast.makeText(this, "저장된 템플릿이 없습니다", Toast.LENGTH_SHORT).show();
            return;
        }
        
        List<String> templateNames = new ArrayList<>();
        for (Template template : SharedData.aTemplateList) {
            templateNames.add(template.name);
        }
        
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("템플릿 불러오기")
                .setItems(templateNames.toArray(new String[0]), (dialog, which) -> {
                    Template selected = SharedData.aTemplateList.get(which);
                    selected.applyToSettings();
                    SharedData.saveConfig(this);
                    initializeUI();  // UI 갱신
                    Toast.makeText(this, selected.name + " 템플릿을 불러왔습니다", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", null)
                .show();
    }
    
    private void openDestinationSettings() {
        // 도착지 설정 액티비티 실행
        Intent intent = new Intent(this, DestinationSettingsActivity.class);
        startActivity(intent);
    }
    
    private void openExclusionSettings() {
        // 제외지 설정 액티비티 실행
        Intent intent = new Intent(this, ExclusionSettingsActivity.class);
        startActivity(intent);
    }
    
    private void toggleService() {
        if (isAccessibilityServiceEnabled()) {
            SharedData.bAuto = !SharedData.bAuto;
            updateServiceStatus();
            Toast.makeText(this, SharedData.bAuto ? "서비스 시작됨" : "서비스 중지됨", 
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "먼저 접근성 서비스를 활성화해주세요", Toast.LENGTH_LONG).show();
            openAccessibilitySettings();
        }
    }
    
    private void updateServiceStatus() {
        if (SharedData.bAuto) {
            binding.btnToggleService.setText("서비스 중지");
            binding.btnToggleService.setBackgroundColor(getColor(R.color.red));
            binding.tvServiceStatus.setText("서비스 실행 중");
            binding.tvServiceStatus.setTextColor(getColor(R.color.green));
        } else {
            binding.btnToggleService.setText("서비스 시작");
            binding.btnToggleService.setBackgroundColor(getColor(R.color.green));
            binding.tvServiceStatus.setText("서비스 중지됨");
            binding.tvServiceStatus.setTextColor(getColor(R.color.red));
        }
    }
    
    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW) 
                != PackageManager.PERMISSION_GRANTED) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(intent);
            }
        }
        
        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, 
                    permissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }
    
    private void checkAccessibilityService() {
        if (!isAccessibilityServiceEnabled()) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle("접근성 서비스 활성화")
                    .setMessage("이 앱을 사용하려면 접근성 서비스를 활성화해야 합니다.")
                    .setPositiveButton("설정으로 이동", (dialog, which) -> openAccessibilitySettings())
                    .setNegativeButton("나중에", null)
                    .show();
        }
    }
    
    private boolean isAccessibilityServiceEnabled() {
        String service = getPackageName() + "/" + MyAccessibilityService.class.getCanonicalName();
        String enabledServices = Settings.Secure.getString(getContentResolver(), 
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return enabledServices != null && enabledServices.contains(service);
    }
    
    private void openAccessibilitySettings() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateServiceStatus();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // 키워드 저장
        String keywords = binding.editKeywords.getText().toString();
        SharedData.aKeywordFilterList = SharedData.parseStr2Array(keywords);
        SharedData.saveConfig(this);
    }
}