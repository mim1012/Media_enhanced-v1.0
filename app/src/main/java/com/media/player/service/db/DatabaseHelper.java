package com.media.player.service.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 데이터베이스 헬퍼 (원본: StealthDBHelper)
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "address.db";
    private static final int DATABASE_VERSION = 1;
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 시군구 테이블
        String createSigunguTable = "CREATE TABLE IF NOT EXISTS address_sigungus (" +
                "sigungu_code INTEGER PRIMARY KEY," +
                "sigungu_name TEXT NOT NULL" +
                ");";
        db.execSQL(createSigunguTable);
        
        // 행정동 테이블
        String createHjdongTable = "CREATE TABLE IF NOT EXISTS address_hjdongs (" +
                "hjdong_code INTEGER PRIMARY KEY," +
                "sigungu_code INTEGER," +
                "sigungu_name TEXT," +
                "hjdong_name TEXT," +
                "bjdong_name TEXT," +
                "dong_name TEXT," +
                "lat REAL," +
                "lng REAL," +
                "FOREIGN KEY(sigungu_code) REFERENCES address_sigungus(sigungu_code)" +
                ");";
        db.execSQL(createHjdongTable);
        
        // 인덱스 생성
        db.execSQL("CREATE INDEX idx_hjdong_name ON address_hjdongs(hjdong_name);");
        db.execSQL("CREATE INDEX idx_bjdong_name ON address_hjdongs(bjdong_name);");
        db.execSQL("CREATE INDEX idx_dong_name ON address_hjdongs(dong_name);");
        db.execSQL("CREATE INDEX idx_sigungu_name ON address_hjdongs(sigungu_name);");
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 업그레이드 로직
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS address_hjdongs");
            db.execSQL("DROP TABLE IF EXISTS address_sigungus");
            onCreate(db);
        }
    }
}