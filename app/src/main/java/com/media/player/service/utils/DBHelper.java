package com.media.player.service.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.media.player.service.db.DatabaseHelper;
import com.media.player.service.model.ItemInfo;
import java.util.ArrayList;

/**
 * 데이터베이스 헬퍼 (원본: DBUtils)
 * 핵심 비즈니스 로직 - 지역 코드 조회
 */
public class DBHelper {
    
    // 행정동 코드 조회 (도착지 정보 기반)
    public static String findHjdongCodeListByDestInfo(Context context, ArrayList<Integer> arrayList, ItemInfo itemInfo) {
        String logText = "\t: DB 조회 시작 (도착지 정보)\n";
        
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            
            String query = "SELECT hjdong_code FROM address_hjdongs WHERE dong_name = ? AND sigungu_name LIKE ?";
            String[] selectionArgs = new String[]{itemInfo.sDongName, "%" + itemInfo.sSigunguName + "%"};
            
            Cursor cursor = db.rawQuery(query, selectionArgs);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int code = cursor.getInt(0);
                    arrayList.add(code);
                    logText += "\t: 행정동 코드 찾음: " + code + "\n";
                }
                cursor.close();
            }
            db.close();
            
        } catch (Exception e) {
            logText += "\t: DB 조회 실패: " + e.getMessage() + "\n";
        }
        
        return logText;
    }
    
    // 행정동 코드 조회 (행정동명 기반)
    public static String findHjdongCodeListByHjdongName(Context context, ArrayList<Integer> arrayList, ItemInfo itemInfo) {
        String logText = "\t: DB 조회 시작 (행정동명)\n";
        
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            
            String query = "SELECT hjdong_code FROM address_hjdongs WHERE hjdong_name = ? AND sigungu_name LIKE ?";
            String[] selectionArgs = new String[]{itemInfo.sDongName, "%" + itemInfo.sSigunguName + "%"};
            
            Cursor cursor = db.rawQuery(query, selectionArgs);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int code = cursor.getInt(0);
                    arrayList.add(code);
                    logText += "\t: 행정동 코드 찾음: " + code + "\n";
                }
                cursor.close();
            }
            db.close();
            
        } catch (Exception e) {
            logText += "\t: DB 조회 실패: " + e.getMessage() + "\n";
        }
        
        return logText;
    }
    
    // 법정동 코드 조회
    public static String findHjdongCodeListByBjdongName(Context context, ArrayList<Integer> arrayList, ItemInfo itemInfo) {
        String logText = "\t: DB 조회 시작 (법정동명)\n";
        
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            
            String query = "SELECT hjdong_code FROM address_hjdongs WHERE bjdong_name = ? AND sigungu_name LIKE ?";
            String[] selectionArgs = new String[]{itemInfo.sDongName, "%" + itemInfo.sSigunguName + "%"};
            
            Cursor cursor = db.rawQuery(query, selectionArgs);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int code = cursor.getInt(0);
                    arrayList.add(code);
                    logText += "\t: 법정동 코드 찾음: " + code + "\n";
                }
                cursor.close();
            }
            db.close();
            
        } catch (Exception e) {
            logText += "\t: DB 조회 실패: " + e.getMessage() + "\n";
        }
        
        return logText;
    }
    
    // 좌표 조회 (장거리 모드용)
    public static double[] getCoordinates(Context context, String dongName, String sigunguName) {
        double[] coords = new double[]{0.0, 0.0};
        
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            
            String query = "SELECT lat, lng FROM address_hjdongs WHERE hjdong_name = ? AND sigungu_name LIKE ? LIMIT 1";
            String[] selectionArgs = new String[]{dongName, "%" + sigunguName + "%"};
            
            Cursor cursor = db.rawQuery(query, selectionArgs);
            if (cursor != null && cursor.moveToFirst()) {
                coords[0] = cursor.getDouble(0);  // latitude
                coords[1] = cursor.getDouble(1);  // longitude
                cursor.close();
            }
            db.close();
            
        } catch (Exception e) {
            Logger.log("좌표 조회 실패: " + e.getMessage());
        }
        
        return coords;
    }
}