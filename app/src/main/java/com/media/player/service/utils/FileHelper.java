package com.media.player.service.utils;

import android.content.Context;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

/**
 * 파일 헬퍼 (원본: FileUtils)
 */
public class FileHelper {
    
    public static String loadExclusionFile() {
        return loadTextFile("exclusion.txt");
    }
    
    public static String loadPlaylistFile() {
        return loadTextFile("playlist.txt");
    }
    
    public static void saveExclusionFile(String content) {
        saveTextFile("exclusion.txt", content);
    }
    
    public static void savePlaylistFile(String content) {
        saveTextFile("playlist.txt", content);
    }
    
    private static String loadTextFile(String fileName) {
        StringBuilder sb = new StringBuilder();
        try {
            File file = new File(android.os.Environment.getDataDirectory() 
                + "/data/com.media.player.service/files/" + fileName);
            
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
                fis.close();
            }
        } catch (Exception e) {
            Logger.log("파일 읽기 실패: " + fileName + " - " + e.getMessage());
        }
        return sb.toString();
    }
    
    private static void saveTextFile(String fileName, String content) {
        try {
            File dir = new File(android.os.Environment.getDataDirectory() 
                + "/data/com.media.player.service/files/");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            File file = new File(dir, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.close();
            
        } catch (Exception e) {
            Logger.log("파일 저장 실패: " + fileName + " - " + e.getMessage());
        }
    }
    
    // 데이터베이스 파일 존재 여부 확인
    public static boolean isDatabaseExists(Context context) {
        File dbFile = context.getDatabasePath("address.db");
        return dbFile.exists();
    }
    
    // 데이터베이스 파일 복사 (assets에서)
    public static void copyDatabaseFromAssets(Context context) {
        try {
            File dbFile = context.getDatabasePath("address.db");
            if (!dbFile.exists()) {
                dbFile.getParentFile().mkdirs();
                
                // assets에서 복사
                java.io.InputStream is = context.getAssets().open("address.db");
                java.io.OutputStream os = new FileOutputStream(dbFile);
                
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                
                os.flush();
                os.close();
                is.close();
            }
        } catch (Exception e) {
            Logger.log("DB 복사 실패: " + e.getMessage());
        }
    }
}