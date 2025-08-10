package com.media.player.service;

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
import com.media.player.service.databinding.ActivityPlayerBinding;
import com.media.player.service.utils.Config;
import com.media.player.service.utils.DataStore;
import com.media.player.service.utils.Preset;
import java.util.ArrayList;
import java.util.List;

public class PlayerActivity extends AppCompatActivity {
    private ActivityPlayerBinding binding;
    private static final int PERMISSION_REQUEST_CODE = 100;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // 설정 로드
        DataStore.loadConfig(this);
        
        // UI 초기화
        initializeUI();
        
        // 권한 체크
        checkPermissions();
        
        // 접근성 서비스 체크
        checkAccessibilityService();
    }
    
    private void initializeUI() {
        // 품질 설정 ChipGroup 초기화
        setupQualityChips();
        
        // 전체/선택 모드 스위치
        binding.switchPlayMode.setChecked(DataStore.bFullMode);
        binding.switchPlayMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            DataStore.bFullMode = isChecked;
            updateModeUI(isChecked);
            DataStore.saveConfig(this);
        });
        
        // 자동 스킵 스위치
        binding.switchAutoSkip.setChecked(DataStore.bAutoSkip);
        binding.switchAutoSkip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            DataStore.bAutoSkip = isChecked;
            DataStore.saveConfig(this);
        });
        
        // 볼륨 컨트롤 스위치
        binding.switchVolumeControl.setChecked(DataStore.bVolumeControl);
        binding.switchVolumeControl.setOnCheckedChangeListener((buttonView, isChecked) -> {
            DataStore.bVolumeControl = isChecked;
            DataStore.saveConfig(this);
        });
        
        // 필터 입력
        binding.editFilters.setText(DataStore.parseArray2Str(DataStore.aFilterList));
        
        // 재생목록 설정 버튼
        binding.btnPlaylistSettings.setOnClickListener(v -> openPlaylistSettings());
        
        // 제외목록 설정 버튼
        binding.btnExclusionSettings.setOnClickListener(v -> openExclusionSettings());
        
        // 프리셋 버튼들
        binding.btnSavePreset.setOnClickListener(v -> savePreset());
        binding.btnLoadPreset.setOnClickListener(v -> loadPreset());
        
        // 서비스 시작/중지 버튼
        binding.btnToggleService.setOnClickListener(v -> toggleService());
        
        // 초기 UI 상태 업데이트
        updateModeUI(DataStore.bFullMode);
        updateServiceStatus();
    }
    
    private void setupQualityChips() {
        ChipGroup chipGroup = binding.chipGroupQuality;
        chipGroup.removeAllViews();
        
        for (String quality : DataStore.QUALITY_PRESETS) {
            Chip chip = new Chip(this);
            chip.setText(quality);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(true);
            
            if (quality.equals(DataStore.sQualityPreset)) {
                chip.setChecked(true);
            }
            
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    DataStore.sQualityPreset = quality;
                    if (quality.equals("최고 품질")) {
                        DataStore.nQuality = 0;
                    } else {
                        String qualStr = quality.replace("km", "").trim();
                        try {
                            float qual = Float.parseFloat(qualStr);
                            DataStore.nQuality = (int)(qual * 1000);
                        } catch (NumberFormatException e) {
                            DataStore.nQuality = 1000;
                        }
                    }
                    DataStore.saveConfig(this);
                    updateQualityDisplay();
                }
            });
            
            chipGroup.addView(chip);
        }
        
        chipGroup.setSingleSelection(true);
    }
    
    private void updateQualityDisplay() {
        String qualityText = DataStore.sQualityPreset;
        if (!qualityText.equals("최고 품질")) {
            qualityText += " (" + DataStore.nQuality + "m)";
        }
        binding.tvCurrentQuality.setText("현재 품질: " + qualityText);
    }
    
    private void updateModeUI(boolean isFullMode) {
        if (isFullMode) {
            binding.tvModeStatus.setText("전체 재생 모드");
            binding.cardQualitySettings.setAlpha(0.5f);
            binding.cardFilterSettings.setAlpha(0.5f);
            binding.chipGroupQuality.setEnabled(false);
            binding.editFilters.setEnabled(false);
        } else {
            binding.tvModeStatus.setText("선택 재생 모드");
            binding.cardQualitySettings.setAlpha(1.0f);
            binding.cardFilterSettings.setAlpha(1.0f);
            binding.chipGroupQuality.setEnabled(true);
            binding.editFilters.setEnabled(true);
        }
    }
    
    private void savePreset() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_save_preset, null);
        TextInputEditText editName = dialogView.findViewById(R.id.edit_preset_name);
        
        builder.setTitle("프리셋 저장")
                .setView(dialogView)
                .setPositiveButton("저장", (dialog, which) -> {
                    String name = editName.getText().toString().trim();
                    if (!name.isEmpty()) {
                        String filters = binding.editFilters.getText().toString();
                        DataStore.aFilterList = DataStore.parseStr2Array(filters);
                        
                        Preset preset = Preset.fromCurrentSettings(name);
                        if (DataStore.aPresetList.size() >= 5) {
                            DataStore.aPresetList.remove(0);
                        }
                        DataStore.aPresetList.add(preset);
                        DataStore.saveConfig(this);
                        Toast.makeText(this, "프리셋이 저장되었습니다", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }
    
    private void loadPreset() {
        if (DataStore.aPresetList.isEmpty()) {
            Toast.makeText(this, "저장된 프리셋이 없습니다", Toast.LENGTH_SHORT).show();
            return;
        }
        
        List<String> presetNames = new ArrayList<>();
        for (Preset preset : DataStore.aPresetList) {
            presetNames.add(preset.name);
        }
        
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("프리셋 불러오기")
                .setItems(presetNames.toArray(new String[0]), (dialog, which) -> {
                    Preset selected = DataStore.aPresetList.get(which);
                    selected.applyToSettings();
                    DataStore.saveConfig(this);
                    initializeUI();
                    Toast.makeText(this, selected.name + " 프리셋을 불러왔습니다", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", null)
                .show();
    }
    
    private void openPlaylistSettings() {
        Intent intent = new Intent(this, PlaylistActivity.class);
        startActivity(intent);
    }
    
    private void openExclusionSettings() {
        Intent intent = new Intent(this, ExclusionActivity.class);
        startActivity(intent);
    }
    
    private void toggleService() {
        if (isAccessibilityServiceEnabled()) {
            DataStore.bEnabled = !DataStore.bEnabled;
            updateServiceStatus();
            Toast.makeText(this, DataStore.bEnabled ? "재생 시작" : "재생 중지", 
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "먼저 보조 서비스를 활성화해주세요", Toast.LENGTH_LONG).show();
            openAccessibilitySettings();
        }
    }
    
    private void updateServiceStatus() {
        if (DataStore.bEnabled) {
            binding.btnToggleService.setText("재생 중지");
            binding.btnToggleService.setBackgroundColor(getColor(R.color.red));
            binding.tvServiceStatus.setText("재생 중");
            binding.tvServiceStatus.setTextColor(getColor(R.color.green));
        } else {
            binding.btnToggleService.setText("재생 시작");
            binding.btnToggleService.setBackgroundColor(getColor(R.color.green));
            binding.tvServiceStatus.setText("일시 정지");
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
            builder.setTitle("보조 서비스 활성화")
                    .setMessage("미디어 재생을 위해 보조 서비스를 활성화해야 합니다.")
                    .setPositiveButton("설정으로 이동", (dialog, which) -> openAccessibilitySettings())
                    .setNegativeButton("나중에", null)
                    .show();
        }
    }
    
    private boolean isAccessibilityServiceEnabled() {
        String service = getPackageName() + "/" + MediaService.class.getCanonicalName();
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
        String filters = binding.editFilters.getText().toString();
        DataStore.aFilterList = DataStore.parseStr2Array(filters);
        DataStore.saveConfig(this);
    }
}