package com.nhom11.innowave;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import com.nhom11.models.Review;
import com.nhom11.models.User;
import com.nhom11.models.OTPCode;
import com.nhom11.models.Address;
import com.nhom11.models.Order;
import com.nhom11.models.OrderItem;
import android.util.Log;

public class MyDatabase extends SQLiteOpenHelper {
    private static final String DB_NAME = "innowave_db.db";
    private static final int DB_VERSION = 3;
    private Context context;
    private String dbPath;

    public MyDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
        this.dbPath = context.getDatabasePath(DB_NAME).getPath();
        copyDatabaseIfNeeded();
    }

    private void copyDatabaseIfNeeded() {
        File dbFile = new File(dbPath);
        if (!dbFile.exists()) {
            dbFile.getParentFile().mkdirs();
            try (InputStream is = context.getAssets().open(DB_NAME);
                 FileOutputStream os = new FileOutputStream(dbFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng USER_SESSION nếu chưa tồn tại
        createUserSessionTable(db);
    }

    private void createUserSessionTable(SQLiteDatabase db) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS USER_SESSION (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "session_token TEXT NOT NULL," +
                "expires_at TEXT NOT NULL," +
                "created_at TEXT NOT NULL," +
                "FOREIGN KEY (user_id) REFERENCES USER(user_id)" +
                ")";
        db.execSQL(createTableSQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Tạo bảng USER_SESSION cho version mới
            createUserSessionTable(db);
        }
        // Đã có sẵn cột recipient_name trong csdl, không cần thêm nữa
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM product WHERE is_active = 1", null);
        if (cursor.moveToFirst()) {
            do {
                Product product = new Product(
                        cursor.getString(cursor.getColumnIndexOrThrow("product_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("category_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("default_variant_id")),
                        cursor.getBlob(cursor.getColumnIndexOrThrow("thumbnail_url")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("original_price")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("discounted_price")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("is_active")),
                        cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                        cursor.getString(cursor.getColumnIndexOrThrow("updated_at"))
                );
                products.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return products;
    }

    public List<Product> searchProductsByName(String query) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM product WHERE is_active = 1 AND name LIKE ?", new String[]{"%" + query + "%"});
        if (cursor.moveToFirst()) {
            do {
                Product product = new Product(
                        cursor.getString(cursor.getColumnIndexOrThrow("product_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("category_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("default_variant_id")),
                        cursor.getBlob(cursor.getColumnIndexOrThrow("thumbnail_url")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("original_price")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("discounted_price")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("is_active")),
                        cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                        cursor.getString(cursor.getColumnIndexOrThrow("updated_at"))
                );
                products.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return products;
    }

    public static class Category {
        public int category_id;
        public String name;
        public String description;
        public Category(int id, String name, String desc) {
            this.category_id = id;
            this.name = name;
            this.description = desc;
        }
    }

    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM CATEGORY", null);
        if (cursor.moveToFirst()) {
            do {
                categories.add(new Category(
                    cursor.getInt(cursor.getColumnIndexOrThrow("category_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description"))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return categories;
    }

    public Product getProductById(String productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM PRODUCT WHERE product_id=?", new String[]{productId});
        Product product = null;
        if (cursor.moveToFirst()) {
            product = new Product(
                cursor.getString(cursor.getColumnIndexOrThrow("product_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getInt(cursor.getColumnIndexOrThrow("category_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("default_variant_id")),
                cursor.getBlob(cursor.getColumnIndexOrThrow("thumbnail_url")),
                cursor.getString(cursor.getColumnIndexOrThrow("description")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("original_price")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("discounted_price")),
                cursor.getInt(cursor.getColumnIndexOrThrow("is_active")),
                cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                cursor.getString(cursor.getColumnIndexOrThrow("updated_at"))
            );
        }
        cursor.close();
        return product;
    }

    public static class ProductVariant {
        public String variant_id;
        public String product_id;
        public String variant_name;
        public int stock_quantity;
        public byte[] thumbnail_url;
        public String created_at;
        public String updated_at;
        public ProductVariant(String variant_id, String product_id, String variant_name, int stock_quantity, byte[] thumbnail_url, String created_at, String updated_at) {
            this.variant_id = variant_id;
            this.product_id = product_id;
            this.variant_name = variant_name;
            this.stock_quantity = stock_quantity;
            this.thumbnail_url = thumbnail_url;
            this.created_at = created_at;
            this.updated_at = updated_at;
        }
    }

    public List<ProductVariant> getVariantsByProductId(String productId) {
        List<ProductVariant> variants = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM PRODUCT_VARIANT WHERE product_id=?", new String[]{productId});
        while (cursor.moveToNext()) {
            variants.add(new ProductVariant(
                cursor.getString(cursor.getColumnIndexOrThrow("variant_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("product_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("variant_name")),
                cursor.getInt(cursor.getColumnIndexOrThrow("stock_quantity")),
                cursor.getBlob(cursor.getColumnIndexOrThrow("thumbnail_url")),
                cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                cursor.getString(cursor.getColumnIndexOrThrow("updated_at"))
            ));
        }
        cursor.close();
        return variants;
    }

    public ProductVariant getProductVariantById(String variantId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM PRODUCT_VARIANT WHERE variant_id=?", new String[]{variantId});
        ProductVariant variant = null;
        if (cursor.moveToFirst()) {
            variant = new ProductVariant(
                cursor.getString(cursor.getColumnIndexOrThrow("variant_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("product_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("variant_name")),
                cursor.getInt(cursor.getColumnIndexOrThrow("stock_quantity")),
                cursor.getBlob(cursor.getColumnIndexOrThrow("thumbnail_url")), // lấy đúng ảnh biến thể
                cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                cursor.getString(cursor.getColumnIndexOrThrow("updated_at"))
            );
        }
        cursor.close();
        db.close();
        return variant;
    }

    public static class Product {
        public String product_id;
        public String name;
        public int category_id;
        public String default_variant_id;
        public byte[] thumbnail_url;
        public String description;
        public double original_price;
        public double discounted_price;
        public int is_active;
        public String created_at;
        public String updated_at;
        public Product(String product_id, String name, int category_id, String default_variant_id, byte[] thumbnail_url, String description, double original_price, double discounted_price, int is_active, String created_at, String updated_at) {
            this.product_id = product_id;
            this.name = name;
            this.category_id = category_id;
            this.default_variant_id = default_variant_id;
            this.thumbnail_url = thumbnail_url;
            this.description = description;
            this.original_price = original_price;
            this.discounted_price = discounted_price;
            this.is_active = is_active;
            this.created_at = created_at;
            this.updated_at = updated_at;
        }
    }

    public List<Review> getTopReviewsByProductId(String productId, int limit) {
        List<Review> reviews = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM REVIEW WHERE product_id=? AND is_active=1 ORDER BY rating DESC, created_at DESC LIMIT ?", new String[]{productId, String.valueOf(limit)});
        if (cursor.moveToFirst()) {
            do {
                reviews.add(new Review(
                    cursor.getInt(cursor.getColumnIndexOrThrow("review_id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("product_id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("rating")),
                    cursor.getString(cursor.getColumnIndexOrThrow("comment")),
                    cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                    cursor.getString(cursor.getColumnIndexOrThrow("updated_at")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("is_active"))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return reviews;
    }

    public User getUserById(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM USER WHERE user_id=?", new String[]{String.valueOf(userId)});
        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(
                cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("full_name")),
                cursor.getString(cursor.getColumnIndexOrThrow("email")),
                cursor.getString(cursor.getColumnIndexOrThrow("phone_number")),
                cursor.getString(cursor.getColumnIndexOrThrow("password_hash")),
                cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                cursor.getString(cursor.getColumnIndexOrThrow("updated_at")),
                cursor.getString(cursor.getColumnIndexOrThrow("last_login_at")),
                cursor.getInt(cursor.getColumnIndexOrThrow("is_active"))
            );
        }
        cursor.close();
        db.close();
        return user;
    }

    public List<Review> getAllReviewsByProductId(String productId) {
        List<Review> reviews = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM REVIEW WHERE product_id=? AND is_active=1 ORDER BY created_at DESC", new String[]{productId});
        if (cursor.moveToFirst()) {
            do {
                reviews.add(new Review(
                    cursor.getInt(cursor.getColumnIndexOrThrow("review_id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("product_id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("rating")),
                    cursor.getString(cursor.getColumnIndexOrThrow("comment")),
                    cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                    cursor.getString(cursor.getColumnIndexOrThrow("updated_at")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("is_active"))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return reviews;
    }

    public List<Product> getRandomRelatedProducts(int categoryId, String excludeProductId, int limit) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT * FROM PRODUCT WHERE category_id=? AND product_id!=? AND is_active=1 ORDER BY RANDOM() LIMIT ?",
            new String[]{String.valueOf(categoryId), excludeProductId, String.valueOf(limit)}
        );
        if (cursor.moveToFirst()) {
            do {
                products.add(new Product(
                    cursor.getString(cursor.getColumnIndexOrThrow("product_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("category_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("default_variant_id")),
                    cursor.getBlob(cursor.getColumnIndexOrThrow("thumbnail_url")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("original_price")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("discounted_price")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("is_active")),
                    cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                    cursor.getString(cursor.getColumnIndexOrThrow("updated_at"))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return products;
    }

    // Kiểm tra số điện thoại đã tồn tại chưa
    public User getUserByPhone(String phone) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM USER WHERE phone_number=?", new String[]{phone});
        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(
                cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("full_name")),
                cursor.getString(cursor.getColumnIndexOrThrow("email")),
                cursor.getString(cursor.getColumnIndexOrThrow("phone_number")),
                cursor.getString(cursor.getColumnIndexOrThrow("password_hash")),
                cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                cursor.getString(cursor.getColumnIndexOrThrow("updated_at")),
                cursor.getString(cursor.getColumnIndexOrThrow("last_login_at")),
                cursor.getInt(cursor.getColumnIndexOrThrow("is_active"))
            );
        }
        cursor.close();
        db.close();
        return user;
    }

    // Tạo user mới với số điện thoại
    public long createUserWithPhone(String phone) {
        SQLiteDatabase db = getWritableDatabase();
        long userId = -1;
        try {
            db.execSQL("INSERT INTO USER (phone_number, is_active, created_at) VALUES (?, 1, datetime('now'))", new Object[]{phone});
            Cursor cursor = db.rawQuery("SELECT last_insert_rowid()", null);
            if (cursor.moveToFirst()) {
                userId = cursor.getLong(0);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
        return userId;
    }

    // Tạo user mới với số điện thoại và password hash
    public long createUserWithPhoneAndPassword(String phone, String passwordHash) {
        SQLiteDatabase db = getWritableDatabase();
        long userId = -1;
        try {
            db.execSQL("INSERT INTO USER (phone_number, password_hash, is_active, created_at) VALUES (?, ?, 1, datetime('now'))", new Object[]{phone, passwordHash});
            Cursor cursor = db.rawQuery("SELECT last_insert_rowid()", null);
            if (cursor.moveToFirst()) {
                userId = cursor.getLong(0);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
        return userId;
    }

    // Tạo OTP mới
    public void createOTP(int userId, String code, String expiresAt) {
        SQLiteDatabase db = getWritableDatabase();
        // Vô hiệu hóa tất cả mã OTP cũ của user này
        db.execSQL("UPDATE OTP_CODE SET is_used=1 WHERE user_id=?", new Object[]{userId});
        db.execSQL("INSERT INTO OTP_CODE (user_id, code, expires_at, is_used, created_at) VALUES (?, ?, ?, 0, datetime('now'))", new Object[]{userId, code, expiresAt});
        db.close();
    }

    // Lấy OTP mới nhất của user
    public OTPCode getLatestOTP(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM OTP_CODE WHERE user_id=? ORDER BY created_at DESC LIMIT 1", new String[]{String.valueOf(userId)});
        OTPCode otp = null;
        if (cursor.moveToFirst()) {
            otp = new OTPCode(
                cursor.getInt(cursor.getColumnIndexOrThrow("otp_id")),
                cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("code")),
                cursor.getString(cursor.getColumnIndexOrThrow("expires_at")),
                cursor.getInt(cursor.getColumnIndexOrThrow("is_used")),
                cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
            );
        }
        cursor.close();
        db.close();
        return otp;
    }

    // Xác thực OTP
    public boolean verifyOTP(int userId, String code) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM OTP_CODE WHERE user_id=? AND code=? AND is_used=0", new String[]{String.valueOf(userId), code});
        boolean valid = false;
        if (cursor.moveToFirst()) {
            String expiresAt = cursor.getString(cursor.getColumnIndexOrThrow("expires_at"));
            long expiresAtLong = Long.parseLong(expiresAt);
            long currentTime = System.currentTimeMillis();
            
            if (currentTime < expiresAtLong) {
                valid = true;
                int otpId = cursor.getInt(cursor.getColumnIndexOrThrow("otp_id"));
                db.execSQL("UPDATE OTP_CODE SET is_used=1 WHERE otp_id=?", new Object[]{otpId});
            }
        }
        cursor.close();
        db.close();
        return valid;
    }

    // Session management methods
    public void createSession(int userId, String sessionToken, String expiresAt) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Xóa session cũ của user này nếu có
        db.delete("USER_SESSION", "user_id = ?", new String[]{String.valueOf(userId)});
        
        // Tạo session mới
        String insertQuery = "INSERT INTO USER_SESSION (user_id, session_token, expires_at, created_at) VALUES (?, ?, ?, ?)";
        db.execSQL(insertQuery, new Object[]{userId, sessionToken, expiresAt, String.valueOf(System.currentTimeMillis())});
        db.close();
    }

    public boolean validateSession(int userId, String sessionToken) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM USER_SESSION WHERE user_id = ? AND session_token = ?", 
            new String[]{String.valueOf(userId), sessionToken});
        boolean isValid = false;
        if (cursor.moveToFirst()) {
            String expiresAt = cursor.getString(cursor.getColumnIndexOrThrow("expires_at"));
            long currentTime = System.currentTimeMillis();
            long expireTime = Long.parseLong(expiresAt);
            isValid = currentTime <= expireTime;
        }
        cursor.close();
        db.close();
        return isValid;
    }

    public void deleteSession(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("USER_SESSION", "user_id = ?", new String[]{String.valueOf(userId)});
        db.close();
    }

    public String getSessionToken(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT session_token FROM USER_SESSION WHERE user_id = ?", 
            new String[]{String.valueOf(userId)});
        String sessionToken = null;
        if (cursor.moveToFirst()) {
            sessionToken = cursor.getString(cursor.getColumnIndexOrThrow("session_token"));
        }
        cursor.close();
        db.close();
        return sessionToken;
    }

    public void cleanupExpiredSessions() {
        SQLiteDatabase db = this.getWritableDatabase();
        long currentTime = System.currentTimeMillis();
        db.delete("USER_SESSION", "expires_at < ?", new String[]{String.valueOf(currentTime)});
        db.close();
    }

    // Cart management methods
    public long createCartIfNotExists(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT cart_id FROM CART WHERE user_id = ?", new String[]{String.valueOf(userId)});
        long cartId = -1;
        if (cursor.moveToFirst()) {
            cartId = cursor.getLong(cursor.getColumnIndexOrThrow("cart_id"));
        } else {
            db.execSQL("INSERT INTO CART (user_id, updated_at) VALUES (?, datetime('now'))", new Object[]{userId});
            cursor = db.rawQuery("SELECT last_insert_rowid()", null);
            if (cursor.moveToFirst()) {
                cartId = cursor.getLong(0);
            }
        }
        cursor.close();
        db.close();
        return cartId;
    }

    public boolean addCartItem(long cartId, String variantId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;
        try {
            // Kiểm tra item đã tồn tại trong cart chưa
            Cursor cursor = db.rawQuery("SELECT cart_item_id, quantity FROM CART_ITEM WHERE cart_id = ? AND variant_id = ?", 
                new String[]{String.valueOf(cartId), variantId});
            if (cursor.moveToFirst()) {
                // Cập nhật số lượng
                int existingQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                int newQuantity = existingQuantity + quantity;
                int cartItemId = cursor.getInt(cursor.getColumnIndexOrThrow("cart_item_id"));
                db.execSQL("UPDATE CART_ITEM SET quantity = ?, updated_at = datetime('now') WHERE cart_item_id = ?", 
                    new Object[]{newQuantity, cartItemId});
                success = true;
            } else {
                // Thêm item mới
                db.execSQL("INSERT INTO CART_ITEM (cart_id, variant_id, quantity, created_at) VALUES (?, ?, ?, datetime('now'))", 
                    new Object[]{cartId, variantId, quantity});
                success = true;
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
        db.close();
        return success;
    }

    public long getCartIdByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT cart_id FROM CART WHERE user_id = ?", new String[]{String.valueOf(userId)});
        long cartId = -1;
        if (cursor.moveToFirst()) {
            cartId = cursor.getLong(cursor.getColumnIndexOrThrow("cart_id"));
        }
        cursor.close();
        db.close();
        return cartId;
    }

    public List<CartItem> getCartItemsByCartId(long cartId) {
        Log.d("PaymentImage", "Goi getCartItemsByCartId voi cartId=" + cartId);
        List<CartItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT ci.*, pv.product_id as product_id, p.name as product_name, p.discounted_price, pv.variant_name, pv.thumbnail_url " +
                      "FROM CART_ITEM ci " +
                      "JOIN PRODUCT_VARIANT pv ON ci.variant_id = pv.variant_id " +
                      "JOIN PRODUCT p ON pv.product_id = p.product_id " +
                      "WHERE ci.cart_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(cartId)});
        if (cursor.moveToFirst()) {
            do {
                byte[] thumb = cursor.getBlob(cursor.getColumnIndexOrThrow("thumbnail_url"));
                String variantId = cursor.getString(cursor.getColumnIndexOrThrow("variant_id"));
                String variantName = cursor.getString(cursor.getColumnIndexOrThrow("variant_name"));
                Log.d("PaymentImage", "CartItemByCartId: variantId=" + variantId + ", variantName=" + variantName + ", thumbnailUrl size=" + (thumb != null ? thumb.length : 0));
                items.add(new CartItem(
                    cursor.getInt(cursor.getColumnIndexOrThrow("cart_item_id")),
                    cursor.getLong(cursor.getColumnIndexOrThrow("cart_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("product_id")),
                    variantId,
                    cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                    cursor.getString(cursor.getColumnIndexOrThrow("product_name")),
                    variantName,
                    cursor.getDouble(cursor.getColumnIndexOrThrow("discounted_price")),
                    thumb,
                    cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return items;
    }

    public List<CartItem> getCartItems(int userId) {
        List<CartItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT ci.*, pv.product_id as product_id, p.name as product_name, p.discounted_price, pv.variant_name, pv.thumbnail_url " +
                      "FROM CART_ITEM ci " +
                      "JOIN PRODUCT_VARIANT pv ON ci.variant_id = pv.variant_id " +
                      "JOIN PRODUCT p ON pv.product_id = p.product_id " +
                      "JOIN CART c ON ci.cart_id = c.cart_id " +
                      "WHERE c.user_id = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        
        if (cursor.moveToFirst()) {
            do {
                byte[] thumb = cursor.getBlob(cursor.getColumnIndexOrThrow("thumbnail_url"));
                String variantId = cursor.getString(cursor.getColumnIndexOrThrow("variant_id"));
                String variantName = cursor.getString(cursor.getColumnIndexOrThrow("variant_name"));
                Log.d("PaymentImage", "CartItem: variantId=" + variantId + ", variantName=" + variantName + ", thumbnailUrl size=" + (thumb != null ? thumb.length : 0));
                items.add(new CartItem(
                    cursor.getInt(cursor.getColumnIndexOrThrow("cart_item_id")),
                    cursor.getLong(cursor.getColumnIndexOrThrow("cart_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("product_id")),
                    variantId,
                    cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                    cursor.getString(cursor.getColumnIndexOrThrow("product_name")),
                    variantName,
                    cursor.getDouble(cursor.getColumnIndexOrThrow("discounted_price")),
                    thumb,
                    cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return items;
    }

    public static class CartItem implements java.io.Serializable {
        public int cart_item_id;
        public long cart_id;
        public String product_id;
        public String variant_id;
        public int quantity;
        public String product_name;
        public String variant_name;
        public double price;
        public byte[] thumbnail_url;
        public String created_at;

        public CartItem(int cart_item_id, long cart_id, String product_id, String variant_id, int quantity, 
                       String product_name, String variant_name, double price, byte[] thumbnail_url, String created_at) {
            this.cart_item_id = cart_item_id;
            this.cart_id = cart_id;
            this.product_id = product_id;
            this.variant_id = variant_id;
            this.quantity = quantity;
            this.product_name = product_name;
            this.variant_name = variant_name;
            this.price = price;
            this.thumbnail_url = thumbnail_url;
            this.created_at = created_at;
        }
    }

    public boolean updateCartItemQuantity(int cartItemId, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("UPDATE CART_ITEM SET quantity = ?, updated_at = datetime('now') WHERE cart_item_id = ?", new Object[]{newQuantity, cartItemId});
            db.close();
            return true;
        } catch (Exception e) {
            db.close();
            return false;
        }
    }

    public boolean deleteCartItem(int cartItemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("DELETE FROM CART_ITEM WHERE cart_item_id = ?", new Object[]{cartItemId});
            db.close();
            return true;
        } catch (Exception e) {
            db.close();
            return false;
        }
    }

    // Address DAO
    public List<Address> getAddressesByUserId(int userId) {
        List<Address> addresses = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM ADDRESS WHERE user_id=? ORDER BY is_default DESC, address_id DESC", new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            do {
                addresses.add(new Address(
                    cursor.getInt(cursor.getColumnIndexOrThrow("address_id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("recipient_name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("phone_number")),
                    cursor.getString(cursor.getColumnIndexOrThrow("street_address")),
                    cursor.getString(cursor.getColumnIndexOrThrow("ward")),
                    cursor.getString(cursor.getColumnIndexOrThrow("district")),
                    cursor.getString(cursor.getColumnIndexOrThrow("province")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("is_default")),
                    cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                    cursor.getString(cursor.getColumnIndexOrThrow("updated_at"))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return addresses;
    }

    public long insertAddress(Address address) {
        SQLiteDatabase db = getWritableDatabase();
        long id = -1;
        try {
            db.execSQL("INSERT INTO ADDRESS (user_id, recipient_name, phone_number, street_address, ward, district, province, is_default, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, datetime('now'), datetime('now'))",
                new Object[]{address.getUserId(), address.getRecipientName(), address.getPhoneNumber(), address.getStreetAddress(), address.getWard(), address.getDistrict(), address.getProvince(), address.isDefault()});
            Cursor cursor = db.rawQuery("SELECT last_insert_rowid()", null);
            if (cursor.moveToFirst()) {
                id = cursor.getLong(0);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
        return id;
    }

    public boolean userHasAddress(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM ADDRESS WHERE user_id=?", new String[]{String.valueOf(userId)});
        boolean has = false;
        if (cursor.moveToFirst()) {
            has = cursor.getInt(0) > 0;
        }
        cursor.close();
        db.close();
        return has;
    }

    public void setDefaultAddress(int userId, int addressId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE ADDRESS SET is_default=0 WHERE user_id=?", new Object[]{userId});
        db.execSQL("UPDATE ADDRESS SET is_default=1 WHERE address_id=?", new Object[]{addressId});
        db.close();
    }

    public void deleteAddress(int addressId, int userId) {
        SQLiteDatabase db = getWritableDatabase();
        // Kiểm tra nếu là địa chỉ mặc định thì sau khi xóa phải set địa chỉ khác làm mặc định
        Cursor cursor = db.rawQuery("SELECT is_default FROM ADDRESS WHERE address_id=?", new String[]{String.valueOf(addressId)});
        boolean wasDefault = false;
        if (cursor.moveToFirst()) {
            wasDefault = cursor.getInt(0) == 1;
        }
        cursor.close();
        db.execSQL("DELETE FROM ADDRESS WHERE address_id=?", new Object[]{addressId});
        if (wasDefault) {
            // Lấy địa chỉ mới nhất còn lại để set mặc định
            Cursor c = db.rawQuery("SELECT address_id FROM ADDRESS WHERE user_id=? ORDER BY address_id DESC LIMIT 1", new String[]{String.valueOf(userId)});
            if (c.moveToFirst()) {
                int newDefaultId = c.getInt(0);
                setDefaultAddress(userId, newDefaultId);
            }
            c.close();
        }
        db.close();
    }

    public void updateAddress(Address address) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE ADDRESS SET recipient_name=?, phone_number=?, street_address=?, ward=?, district=?, province=?, updated_at=datetime('now') WHERE address_id=?",
            new Object[]{address.getRecipientName(), address.getPhoneNumber(), address.getStreetAddress(), address.getWard(), address.getDistrict(), address.getProvince(), address.getAddressId()});
        db.close();
    }

    public Address getAddressById(int addressId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM ADDRESS WHERE address_id=?", new String[]{String.valueOf(addressId)});
        Address address = null;
        if (cursor.moveToFirst()) {
            address = new Address(
                cursor.getInt(cursor.getColumnIndexOrThrow("address_id")),
                cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("recipient_name")),
                cursor.getString(cursor.getColumnIndexOrThrow("phone_number")),
                cursor.getString(cursor.getColumnIndexOrThrow("street_address")),
                cursor.getString(cursor.getColumnIndexOrThrow("ward")),
                cursor.getString(cursor.getColumnIndexOrThrow("district")),
                cursor.getString(cursor.getColumnIndexOrThrow("province")),
                cursor.getInt(cursor.getColumnIndexOrThrow("is_default")),
                cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                cursor.getString(cursor.getColumnIndexOrThrow("updated_at"))
            );
        }
        cursor.close();
        db.close();
        return address;
    }

    public List<Order> getOrdersByStatus(int userId, String status) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM ORDERS WHERE user_id=? AND status=? ORDER BY created_at DESC", new String[]{String.valueOf(userId), status});
        if (cursor.moveToFirst()) {
            do {
                Order order = new Order(
                    cursor.getInt(cursor.getColumnIndexOrThrow("order_id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("address_id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("voucher_id")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("shipping_fee")),
                    cursor.getString(cursor.getColumnIndexOrThrow("status")),
                    cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                    cursor.getString(cursor.getColumnIndexOrThrow("updated_at"))
                );
                android.util.Log.d("OrderDebug", "Order: id=" + order.orderId + ", user_id=" + order.userId + ", status=" + order.status + ", total=" + order.totalAmount);
                orders.add(order);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return orders;
    }

    public long insertOrder(int userId, int addressId, double totalAmount, double shippingFee, String status) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO ORDERS (user_id, address_id, total_amount, shipping_fee, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, datetime('now'), datetime('now'))",
            new Object[]{userId, addressId, totalAmount, shippingFee, status});
        Cursor cursor = db.rawQuery("SELECT last_insert_rowid()", null);
        long orderId = -1;
        if (cursor.moveToFirst()) orderId = cursor.getLong(0);
        cursor.close();
        db.close();
        return orderId;
    }

    public void insertOrderItem(long orderId, String variantId, int quantity, double unitPrice, double subTotal) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO ORDER_ITEM (order_id, variant_id, quantity, unit_price, sub_total) VALUES (?, ?, ?, ?, ?)",
            new Object[]{orderId, variantId, quantity, unitPrice, subTotal});
        db.close();
    }

    public List<OrderItem> getOrderItemsByOrderId(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM ORDER_ITEM WHERE order_id=?", new String[]{String.valueOf(orderId)});
        if (cursor.moveToFirst()) {
            do {
                items.add(new OrderItem(
                    cursor.getInt(cursor.getColumnIndexOrThrow("order_item_id")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("order_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("variant_id")), // Đổi sang getString
                    cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("unit_price")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("sub_total"))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return items;
    }

    public double[] getProductPriceByVariantId(String variantId) {
        SQLiteDatabase db = getReadableDatabase();
        double[] prices = new double[]{0, 0}; // [original_price, discounted_price]
        Cursor cursor = db.rawQuery("SELECT p.original_price, p.discounted_price FROM PRODUCT p JOIN PRODUCT_VARIANT v ON p.product_id = v.product_id WHERE v.variant_id=?", new String[]{variantId});
        if (cursor.moveToFirst()) {
            prices[0] = cursor.getDouble(cursor.getColumnIndexOrThrow("original_price"));
            prices[1] = cursor.getDouble(cursor.getColumnIndexOrThrow("discounted_price"));
        }
        cursor.close();
        db.close();
        return prices;
    }

    // Lấy Product theo variant_id
    public Product getProductByVariantId(String variantId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Product product = null;
        Cursor cursor = db.rawQuery("SELECT p.* FROM product p JOIN product_variant v ON p.product_id = v.product_id WHERE v.variant_id = ?", new String[]{variantId});
        if (cursor.moveToFirst()) {
            product = new Product(
                cursor.getString(cursor.getColumnIndexOrThrow("product_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getInt(cursor.getColumnIndexOrThrow("category_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("default_variant_id")),
                cursor.getBlob(cursor.getColumnIndexOrThrow("thumbnail_url")),
                cursor.getString(cursor.getColumnIndexOrThrow("description")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("original_price")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("discounted_price")),
                cursor.getInt(cursor.getColumnIndexOrThrow("is_active")),
                cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                cursor.getString(cursor.getColumnIndexOrThrow("updated_at"))
            );
        }
        cursor.close();
        db.close();
        return product;
    }

    // Hàm migrate: chuyển variant_id trong ORDER_ITEM từ INTEGER sang TEXT
    public void migrateOrderItemVariantIdToText() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // 1. Đổi tên bảng cũ
            db.execSQL("ALTER TABLE ORDER_ITEM RENAME TO ORDER_ITEM_OLD");
            // 2. Tạo bảng mới với variant_id là TEXT
            db.execSQL("CREATE TABLE ORDER_ITEM (order_item_id INTEGER PRIMARY KEY, order_id INTEGER, variant_id TEXT, quantity INTEGER, unit_price DECIMAL(12,2), sub_total DECIMAL(14,2))");
            // 3. Copy dữ liệu từ bảng cũ sang bảng mới
            db.execSQL("INSERT INTO ORDER_ITEM (order_item_id, order_id, variant_id, quantity, unit_price, sub_total) SELECT order_item_id, order_id, CAST(variant_id AS TEXT), quantity, unit_price, sub_total FROM ORDER_ITEM_OLD");
            // 4. Xóa bảng cũ
            db.execSQL("DROP TABLE ORDER_ITEM_OLD");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // Cập nhật mật khẩu mới cho user
    public boolean updateUserPassword(int userId, String newPasswordHash) {
        SQLiteDatabase db = this.getWritableDatabase();
        android.content.ContentValues values = new android.content.ContentValues();
        values.put("password_hash", newPasswordHash);
        int rows = db.update("USER", values, "user_id=?", new String[]{String.valueOf(userId)});
        db.close();
        return rows > 0;
    }
}
