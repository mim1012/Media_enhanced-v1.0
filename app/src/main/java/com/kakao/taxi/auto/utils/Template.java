package com.kakao.taxi.auto.utils;

import org.json.JSONObject;

/**
 * 설정 템플릿 클래스
 * 자주 사용하는 설정을 저장하고 불러올 수 있는 템플릿
 */
public class Template {
    public String name;
    public String distancePreset;
    public boolean fullCallMode;
    public String keywords;
    public String acceptPlaces;
    public String exclusionPlaces;
    public boolean autoDeny;
    public long createdAt;
    
    public Template(String name) {
        this.name = name;
        this.createdAt = System.currentTimeMillis();
    }
    
    public static Template fromCurrentSettings(String name) {
        Template template = new Template(name);
        template.distancePreset = SharedData.sDistancePreset;
        template.fullCallMode = SharedData.bFullCallMode;
        template.keywords = SharedData.parseArray2Str(SharedData.aKeywordFilterList);
        template.acceptPlaces = SharedData.sPreferredAcceptPlaces;
        template.exclusionPlaces = SharedData.sPreferredExclusionPlaces;
        template.autoDeny = SharedData.bAutoDeny;
        return template;
    }
    
    public void applyToSettings() {
        SharedData.sDistancePreset = this.distancePreset;
        SharedData.bFullCallMode = this.fullCallMode;
        SharedData.aKeywordFilterList = SharedData.parseStr2Array(this.keywords);
        SharedData.sPreferredAcceptPlaces = this.acceptPlaces;
        SharedData.sPreferredExclusionPlaces = this.exclusionPlaces;
        SharedData.aPreferredAcceptPlaceList = SharedData.parseStr2Array(this.acceptPlaces);
        SharedData.aPreferredExclusionPlaceList = SharedData.parseStr2Array(this.exclusionPlaces);
        SharedData.bAutoDeny = this.autoDeny;
    }
    
    @Override
    public String toString() {
        try {
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("distancePreset", distancePreset);
            json.put("fullCallMode", fullCallMode);
            json.put("keywords", keywords);
            json.put("acceptPlaces", acceptPlaces);
            json.put("exclusionPlaces", exclusionPlaces);
            json.put("autoDeny", autoDeny);
            json.put("createdAt", createdAt);
            return json.toString();
        } catch (Exception e) {
            return "";
        }
    }
    
    public static Template fromString(String data) {
        try {
            JSONObject json = new JSONObject(data);
            Template template = new Template(json.getString("name"));
            template.distancePreset = json.getString("distancePreset");
            template.fullCallMode = json.getBoolean("fullCallMode");
            template.keywords = json.getString("keywords");
            template.acceptPlaces = json.getString("acceptPlaces");
            template.exclusionPlaces = json.getString("exclusionPlaces");
            template.autoDeny = json.getBoolean("autoDeny");
            template.createdAt = json.getLong("createdAt");
            return template;
        } catch (Exception e) {
            return null;
        }
    }
}