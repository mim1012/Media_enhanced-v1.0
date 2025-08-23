package com.media.player.service.utils;

import org.json.JSONObject;

/**
 * 프리셋 클래스 (템플릿의 이름 변경 버전)
 */
public class Preset {
    public String name;
    public String qualityPreset;
    public boolean fullMode;
    public int nMode;              // 추가: 모드 저장 (768=전체콜, 256=부분콜, 0=대기)
    public String filters;
    public String playlist;
    public String exclusionList;
    public boolean autoSkip;
    public long createdAt;
    
    public Preset(String name) {
        this.name = name;
        this.createdAt = System.currentTimeMillis();
    }
    
    public static Preset fromCurrentSettings(String name) {
        Preset preset = new Preset(name);
        preset.qualityPreset = DataStore.sQualityPreset;
        preset.fullMode = DataStore.bFullMode;
        preset.nMode = DataStore.nMode;        // 추가: 현재 모드 저장
        preset.filters = DataStore.parseArray2Str(DataStore.aFilterList);
        preset.playlist = DataStore.sPlaylist;
        preset.exclusionList = DataStore.sExclusionList;
        preset.autoSkip = DataStore.bAutoSkip;
        return preset;
    }
    
    public void applyToSettings() {
        DataStore.sQualityPreset = this.qualityPreset;
        DataStore.bFullMode = this.fullMode;
        DataStore.nMode = this.nMode;          // 추가: 모드 적용
        DataStore.aFilterList = DataStore.parseStr2Array(this.filters);
        DataStore.sPlaylist = this.playlist;
        DataStore.sExclusionList = this.exclusionList;
        DataStore.aPlaylistItems = DataStore.parseStr2Array(this.playlist);
        DataStore.aExclusionList = DataStore.parseStr2Array(this.exclusionList);
        DataStore.bAutoSkip = this.autoSkip;
    }
    
    @Override
    public String toString() {
        try {
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("qualityPreset", qualityPreset);
            json.put("fullMode", fullMode);
            json.put("nMode", nMode);              // 추가: nMode JSON 저장
            json.put("filters", filters);
            json.put("playlist", playlist);
            json.put("exclusionList", exclusionList);
            json.put("autoSkip", autoSkip);
            json.put("createdAt", createdAt);
            return json.toString();
        } catch (Exception e) {
            return "";
        }
    }
    
    public static Preset fromString(String data) {
        try {
            JSONObject json = new JSONObject(data);
            Preset preset = new Preset(json.getString("name"));
            preset.qualityPreset = json.getString("qualityPreset");
            preset.fullMode = json.getBoolean("fullMode");
            preset.nMode = json.optInt("nMode", 0);    // 추가: nMode JSON 로드 (기본값 0)
            preset.filters = json.getString("filters");
            preset.playlist = json.getString("playlist");
            preset.exclusionList = json.getString("exclusionList");
            preset.autoSkip = json.getBoolean("autoSkip");
            preset.createdAt = json.getLong("createdAt");
            return preset;
        } catch (Exception e) {
            return null;
        }
    }
}