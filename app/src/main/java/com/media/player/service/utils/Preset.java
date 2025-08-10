package com.media.player.service.utils;

import org.json.JSONObject;

/**
 * 프리셋 클래스 (템플릿의 이름 변경 버전)
 */
public class Preset {
    public String name;
    public String qualityPreset;
    public boolean fullMode;
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
        preset.filters = DataStore.parseArray2Str(DataStore.aFilterList);
        preset.playlist = DataStore.sPlaylist;
        preset.exclusionList = DataStore.sExclusionList;
        preset.autoSkip = DataStore.bAutoSkip;
        return preset;
    }
    
    public void applyToSettings() {
        DataStore.sQualityPreset = this.qualityPreset;
        DataStore.bFullMode = this.fullMode;
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