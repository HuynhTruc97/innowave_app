package com.nhom11.innowave;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LogActivity {
    private Context context;
    private SQLiteDatabase database;
    
    public LogActivity(Context context) {
        this.context = context;
        database = context.openOrCreateDatabase("activity_logs.db", Context.MODE_PRIVATE, null);
        createLogTable();
    }
    
    private void createLogTable() {
        String createTable = "CREATE TABLE IF NOT EXISTS user_activity_logs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "activity_type TEXT," +
                "description TEXT," +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
        database.execSQL(createTable);
    }
    
    public void logActivity(int userId, String activityType, String description) {
        String insertQuery = "INSERT INTO user_activity_logs (user_id, activity_type, description) " +
                "VALUES (?, ?, ?)";
        database.execSQL(insertQuery, new Object[]{userId, activityType, description});
    }
    
    public void logAddToCart(int userId, int productId, int quantity) {
        String description = "Added product " + productId + " to cart with quantity " + quantity;
        logActivity(userId, "ADD_TO_CART", description);
    }
    
    public void logPurchase(int userId, String orderId, double totalAmount) {
        String description = "Made purchase with order " + orderId + " for amount " + totalAmount;
        logActivity(userId, "PURCHASE", description);
    }
    
    public void logViewProduct(int userId, int productId) {
        String description = "Viewed product " + productId;
        logActivity(userId, "VIEW_PRODUCT", description);
    }
    
    public void logSearch(int userId, String searchQuery) {
        String description = "Searched for: " + searchQuery;
        logActivity(userId, "SEARCH", description);
    }
    
    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
    }
} 