package com.media.player.service.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 데이터베이스 헬퍼 (원본: StealthDBHelper)
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "address.db";
    private static final int DATABASE_VERSION = 2;  // 스키마 변경으로 버전 업
    private Context context;
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 시군구 테이블 - sido_name 추가
        String createSigunguTable = "CREATE TABLE IF NOT EXISTS address_sigungus (" +
                "sigungu_code INTEGER PRIMARY KEY," +
                "sido_name TEXT NOT NULL," +
                "sigungu_name TEXT NOT NULL," +
                "full_name TEXT NOT NULL" +
                ");";
        db.execSQL(createSigunguTable);
        
        // 행정동 테이블 - sido_name 추가
        String createHjdongTable = "CREATE TABLE IF NOT EXISTS address_hjdongs (" +
                "hjdong_code INTEGER PRIMARY KEY," +
                "sigungu_code INTEGER," +
                "sido_name TEXT," +
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
        db.execSQL("CREATE INDEX idx_sido_name ON address_hjdongs(sido_name);");
        
        // 초기 데이터 삽입
        insertInitialData(db);
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
    
    /**
     * 초기 데이터 삽입
     */
    private void insertInitialData(SQLiteDatabase db) {
        try {
            // 1. 시군구 데이터 삽입
            insertSigunguData(db);
            
            // 2. 행정동 데이터 삽입  
            insertHjdongData(db);
            
        } catch (Exception e) {
            // 로그 기록 및 안전한 처리
            android.util.Log.e("DatabaseHelper", "초기 데이터 삽입 실패 - 계속 진행", e);
            // 에러 발생해도 앱 크래시시키지 않음
        }
    }
    
    /**
     * 시군구 데이터 삽입
     */
    private void insertSigunguData(SQLiteDatabase db) throws IOException {
        InputStream inputStream = context.getAssets().open("sigungu_codes.csv");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        
        String line;
        boolean firstLine = true;  // 헤더 스킵
        
        db.beginTransaction();
        try {
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;  // 헤더 라인 스킵
                }
                
                String[] columns = line.split(",");
                if (columns.length >= 4) {
                    String sql = "INSERT OR REPLACE INTO address_sigungus " +
                            "(sigungu_code, sido_name, sigungu_name, full_name) VALUES (?, ?, ?, ?)";
                    db.execSQL(sql, new Object[]{
                        Integer.parseInt(columns[0]),  // sigungu_code
                        columns[1],                    // sido_name
                        columns[2],                    // sigungu_name
                        columns[3]                     // full_name
                    });
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            reader.close();
            inputStream.close();
        }
    }
    
    /**
     * 행정동 데이터 삽입
     */
    private void insertHjdongData(SQLiteDatabase db) throws IOException {
        InputStream inputStream = context.getAssets().open("hjdong_codes.csv");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        
        String line;
        boolean firstLine = true;  // 헤더 스킵
        
        db.beginTransaction();
        try {
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;  // 헤더 라인 스킵
                }
                
                String[] columns = line.split(",");
                if (columns.length >= 9) {
                    String sql = "INSERT OR REPLACE INTO address_hjdongs " +
                            "(hjdong_code, sigungu_code, sido_name, sigungu_name, hjdong_name, bjdong_name, dong_name, lat, lng) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    db.execSQL(sql, new Object[]{
                        Long.parseLong(columns[0]),    // hjdong_code
                        Integer.parseInt(columns[1]),  // sigungu_code
                        columns[2],                    // sido_name
                        columns[3],                    // sigungu_name
                        columns[4],                    // hjdong_name
                        columns[5],                    // bjdong_name
                        columns[6],                    // dong_name
                        Double.parseDouble(columns[7]), // lat
                        Double.parseDouble(columns[8])  // lng
                    });
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            reader.close();
            inputStream.close();
        }
    }
    
    /**
     * 데이터 초기화 여부 확인
     */
    public boolean isDataInitialized() {
        SQLiteDatabase db = this.getReadableDatabase();
        android.database.Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM address_sigungus", null);
        boolean hasData = false;
        if (cursor.moveToFirst()) {
            hasData = cursor.getInt(0) > 0;
        }
        cursor.close();
        return hasData;
    }
}