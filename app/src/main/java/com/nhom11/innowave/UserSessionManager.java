package com.nhom11.innowave;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;
    
    public UserSessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }
    
    // Lưu thông tin user khi đăng nhập thành công
    public void createLoginSession(int userId, String userName, String userPhone) {
        // Lưu vào SharedPreferences
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_PHONE, userPhone);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.commit();
        
        // Tạo session trong CSDL
        MyDatabase db = new MyDatabase(context);
        String sessionToken = generateSessionToken();
        String expiresAt = String.valueOf(System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000); // 30 ngày
        db.createSession(userId, sessionToken, expiresAt);
        db.close();
    }
    
    // Lấy user ID hiện tại
    public int getUserId() {
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }
    
    // Lấy tên user
    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "");
    }
    
    // Lấy số điện thoại user
    public String getUserPhone() {
        return sharedPreferences.getString(KEY_USER_PHONE, "");
    }
    
    // Kiểm tra user đã đăng nhập chưa
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    // Xóa session khi đăng xuất
    public void logout() {
        int userId = getUserId();
        
        // Xóa session trong CSDL
        if (userId != -1) {
            MyDatabase db = new MyDatabase(context);
            db.deleteSession(userId);
            db.close();
        }
        
        // Xóa SharedPreferences
        editor.clear();
        editor.commit();
    }
    
    // Cập nhật thông tin user
    public void updateUserInfo(String userName, String userPhone) {
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_PHONE, userPhone);
        editor.commit();
    }
    
    // Tạo session token ngẫu nhiên
    private String generateSessionToken() {
        java.util.Random random = new java.util.Random();
        StringBuilder token = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < 32; i++) {
            token.append(chars.charAt(random.nextInt(chars.length())));
        }
        return token.toString();
    }
    
    // Kiểm tra session có hợp lệ không
    public boolean isSessionValid() {
        if (!isLoggedIn()) return false;
        
        int userId = getUserId();
        if (userId == -1) return false;
        
        try {
            MyDatabase db = new MyDatabase(context);
            String sessionToken = db.getSessionToken(userId);
            if (sessionToken == null || sessionToken.isEmpty()) {
                db.close();
                return false;
            }
            boolean isValid = db.validateSession(userId, sessionToken);
            db.close();
            return isValid;
        } catch (Exception e) {
            android.util.Log.e("UserSessionManager", "Error validating session: " + e.getMessage());
            return false;
        }
    }
    
    // Refresh session (kéo dài thời gian hết hạn)
    public void refreshSession() {
        if (!isLoggedIn()) return;
        
        int userId = getUserId();
        if (userId == -1) return;
        
        try {
            MyDatabase db = new MyDatabase(context);
            String sessionToken = db.getSessionToken(userId);
            if (sessionToken != null && !sessionToken.isEmpty()) {
                String newExpiresAt = String.valueOf(System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000); // 30 ngày
                db.createSession(userId, sessionToken, newExpiresAt);
            }
            db.close();
        } catch (Exception e) {
            android.util.Log.e("UserSessionManager", "Error refreshing session: " + e.getMessage());
        }
    }
} 