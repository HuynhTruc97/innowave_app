package com.nhom11.innowave;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.nhom11.innowave.databinding.ActivityProductDetailBinding;

import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.Iterator;
import android.graphics.Typeface;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.nhom11.models.Review;
import com.nhom11.models.User;
import com.nhom11.adapters.ReviewAdapter;
import java.util.HashMap;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.view.LayoutInflater;
import com.nhom11.innowave.LoginAlertDialog;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.widget.Toast;
import android.util.Log;
import com.nhom11.innowave.CartItemLite;
import com.nhom11.innowave.LoginRequiredDialog;
import com.nhom11.innowave.AddToCartDialog;

public class ProductDetailActivity extends AppCompatActivity {
    ActivityProductDetailBinding binding;
    private ImageView imvProduct;
    private TextView tvProductName, productColor, tvPrice, tvOriginalPrice, tvDiscount;
    private ChipGroup variantChipGroup;
    private TableLayout technicalSpecTable;
    private MyDatabase myDatabase;
    private MyDatabase.Product product;
    private List<MyDatabase.ProductVariant> variants;
    private MyDatabase.ProductVariant selectedVariant;
    private UserSessionManager sessionManager;
    private LogActivity logActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        myDatabase = new MyDatabase(this);
        sessionManager = new UserSessionManager(this);
        logActivity = new LogActivity(this);

        // Sự kiện nút back
        binding.icBack.setOnClickListener(v -> {
            android.util.Log.d("ProductDetail", "icBack clicked!");
            onBackPressed();
        });
        
        // Sự kiện nút cart
        binding.icCart.setOnClickListener(v -> {
            android.util.Log.d("ProductDetail", "icCart clicked!");
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("open_cart", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        
        // Sự kiện nút home
        binding.icHome.setOnClickListener(v -> {
            android.util.Log.d("ProductDetail", "icHome clicked!");
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("open_home", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        String productId = getIntent().getStringExtra("product_id");
        if (TextUtils.isEmpty(productId)) finish();
        product = myDatabase.getProductById(productId);
        if (product == null) finish();
        variants = myDatabase.getVariantsByProductId(productId);
        selectedVariant = null;
        for (MyDatabase.ProductVariant v : variants) {
            if (v.variant_id.equals(product.default_variant_id)) {
                selectedVariant = v;
                break;
            }
        }
        if (selectedVariant == null && !variants.isEmpty()) selectedVariant = variants.get(0);
        
        // Ghi log xem sản phẩm nếu user đã đăng nhập
        if (sessionManager.isLoggedIn()) {
            int userId = sessionManager.getUserId();
            int productIdHash = productId.hashCode();
            logActivity.logViewProduct(userId, productIdHash);
        }
        
        // Load UI
        loadProductInfo();
        loadVariantChips();
        loadSpecsTable();
        loadTopReviews(productId);
        loadRelatedProducts();

        // Logic xử lý nút số lượng
        int[] quantity = {1};
        binding.tvQuantity.setText(String.valueOf(quantity[0]));
        
        binding.btnMinus.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                binding.tvQuantity.setText(String.valueOf(quantity[0]));
            }
        });
        
        binding.btnPlus.setOnClickListener(v -> {
            quantity[0]++;
            binding.tvQuantity.setText(String.valueOf(quantity[0]));
        });
        
        // Sự kiện nút thêm vào giỏ hàng
        binding.btnAddToCart.setOnClickListener(v -> {
            android.util.Log.d("ProductDetail", "btnAddToCart clicked!");
            if (!sessionManager.isLoggedIn()) {
                // Hiển thị dialog alert yêu cầu đăng nhập
                LoginAlertDialog loginDialog = new LoginAlertDialog(this);
                loginDialog.show();
            } else {
                // Lấy variant và số lượng đang chọn trên màn hình
                int userId = sessionManager.getUserId();
                MyDatabase db = new MyDatabase(this);
                try {
                    // Tạo cart nếu chưa có
                    long cartId = db.createCartIfNotExists(userId);
                    // Lấy số lượng hiện tại
                    int currentQuantity = Integer.parseInt(binding.tvQuantity.getText().toString());
                    // Lấy variant đang chọn
                    String variantId = (selectedVariant != null) ? selectedVariant.variant_id : product.default_variant_id;
                    boolean success = db.addCartItem(
                        cartId,
                        variantId,
                        currentQuantity
                    );
                    if (success) {
                        // Gửi broadcast cập nhật giỏ hàng
                        Intent intent = new Intent("ACTION_CART_UPDATED");
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                        // Ghi log hoạt động
                        logActivity.logActivity(userId, "ADD_TO_CART", "Added product " + product.name + " to cart");
                        Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Có lỗi xảy ra khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Có lỗi xảy ra: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {
                    db.close();
                }
            }
        });
        
        // Sự kiện nút mua ngay
        binding.btnBuyNow.setOnClickListener(v -> {
            android.util.Log.d("ProductDetail", "btnBuyNow clicked!");
            if (!sessionManager.isLoggedIn()) {
                // Hiển thị dialog alert yêu cầu đăng nhập
                LoginAlertDialog loginDialog = new LoginAlertDialog(this);
                loginDialog.show();
            } else {
                int currentQuantity = Integer.parseInt(binding.tvQuantity.getText().toString());
                String variantId = (selectedVariant != null) ? selectedVariant.variant_id : product.default_variant_id;
                MyDatabase.ProductVariant variant = myDatabase.getProductVariantById(variantId);
                ArrayList<CartItemLite> selectedItems = new ArrayList<>();
                if (variant != null) {
                    selectedItems.add(new CartItemLite(
                        0, // cartItemId = 0 vì không phải từ giỏ hàng
                        product.product_id,
                        variant.variant_id,
                        currentQuantity,
                        product.name,
                        variant.variant_name,
                        product.discounted_price
                    ));
                }
                Intent intent = new Intent(this, PaymentActivity.class);
                intent.putExtra("selected_cart_items", selectedItems);
                startActivity(intent);
            }
        });
    }

    private void loadProductInfo() {
        binding.tvProductName.setText(product.name);
        binding.productColor.setText(selectedVariant != null ? selectedVariant.variant_name : "");
        if (selectedVariant != null && selectedVariant.thumbnail_url != null) {
            binding.imvProduct.setImageBitmap(BitmapFactory.decodeByteArray(selectedVariant.thumbnail_url, 0, selectedVariant.thumbnail_url.length));
        } else if (product.thumbnail_url != null) {
            binding.imvProduct.setImageBitmap(BitmapFactory.decodeByteArray(product.thumbnail_url, 0, product.thumbnail_url.length));
        } else {
            binding.imvProduct.setImageResource(R.drawable.ic_launcher_background);
        }
        binding.tvPrice.setText(String.format("%,.0f₫", product.discounted_price));
        binding.tvOriginalPrice.setText(String.format("%,.0f₫", product.original_price));
        if (product.original_price > 0 && product.discounted_price < product.original_price) {
            binding.tvOriginalPrice.setVisibility(View.VISIBLE);
            binding.tvOriginalPrice.setPaintFlags(binding.tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            int percent = (int) ((1 - product.discounted_price / product.original_price) * 100);
            binding.tvDiscount.setText("-" + percent + "%");
        } else {
            binding.tvOriginalPrice.setVisibility(View.INVISIBLE);
            binding.tvOriginalPrice.setPaintFlags(binding.tvOriginalPrice.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
            binding.tvDiscount.setText("");
        }
    }

    private void loadVariantChips() {
        binding.variantChipGroup.removeAllViews();
        for (MyDatabase.ProductVariant v : variants) {
            Chip chip = new Chip(this);
            chip.setText(v.variant_name);
            chip.setCheckable(true);
            chip.setChecked(selectedVariant != null && v.variant_id.equals(selectedVariant.variant_id));
            chip.setOnClickListener(view -> {
                selectedVariant = v;
                binding.productColor.setText(v.variant_name);
                if (v.thumbnail_url != null) {
                    binding.imvProduct.setImageBitmap(BitmapFactory.decodeByteArray(v.thumbnail_url, 0, v.thumbnail_url.length));
                }
                // update checked state
                for (int i = 0; i < binding.variantChipGroup.getChildCount(); i++) {
                    Chip c = (Chip) binding.variantChipGroup.getChildAt(i);
                    c.setChecked(c == chip);
                }
            });
            binding.variantChipGroup.addView(chip);
        }
    }

    private void loadSpecsTable() {
        binding.technicalSpecificationTable.removeAllViews();
        if (product.description == null) return;
        try {
            JSONObject obj = new JSONObject(product.description);
            Iterator<String> keys = obj.keys();
            int i = 0;
            while (keys.hasNext()) {
                String key = keys.next();
                String value;
                Object rawValue = obj.get(key);
                if (rawValue instanceof org.json.JSONArray) {
                    org.json.JSONArray arr = (org.json.JSONArray) rawValue;
                    List<String> lines = new ArrayList<>();
                    for (int j = 0; j < arr.length(); j++) {
                        lines.add(arr.optString(j));
                    }
                    value = TextUtils.join("\n", lines);
                } else if (rawValue instanceof org.json.JSONObject) {
                    org.json.JSONObject subObj = (org.json.JSONObject) rawValue;
                    List<String> lines = new ArrayList<>();
                    Iterator<String> subKeys = subObj.keys();
                    while (subKeys.hasNext()) {
                        String subKey = subKeys.next();
                        String subVal = subObj.optString(subKey, "");
                        lines.add(subKey + ": " + subVal);
                    }
                    value = TextUtils.join("\n", lines);
                } else {
                    value = obj.optString(key, "");
                }
                TableRow row = new TableRow(this);

                // Cột trái (key)
                TextView tvKey = new TextView(this);
                tvKey.setText(key);
                tvKey.setTypeface(getResources().getFont(R.font.nunito_bold));
                tvKey.setTextSize(15);
                tvKey.setTextColor(0xFF222222);
                tvKey.setPadding(dp(12), dp(8), dp(8), dp(8));
                tvKey.setBackgroundResource(i % 2 == 0 ? R.drawable.bg_spec_left_even : R.drawable.bg_spec_left_odd);
                tvKey.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1f));

                // Viền giữa cột
                View divider = new View(this);
                divider.setLayoutParams(new TableRow.LayoutParams(dp(1), TableRow.LayoutParams.MATCH_PARENT));
                divider.setBackgroundColor(0xFFE0E0E0);

                // Cột phải (value)
                TextView tvValue = new TextView(this);
                tvValue.setText(value);
                tvValue.setSingleLine(false);
                tvValue.setMaxLines(20);
                tvValue.setTypeface(getResources().getFont(R.font.nunito_regular));
                tvValue.setTextSize(15);
                tvValue.setTextColor(0xFF444444);
                tvValue.setPadding(dp(8), dp(8), dp(12), dp(8));
                tvValue.setBackgroundColor(0xFFFFFFFF);
                tvValue.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 2f));

                row.addView(tvKey);
                row.addView(divider);
                row.addView(tvValue);

                binding.technicalSpecificationTable.addView(row);
                i++;
            }
        } catch (JSONException e) {
            // Nếu không phải JSON, hiển thị mô tả thường
            TableRow row = new TableRow(this);
            TextView tv = new TextView(this);
            tv.setText(product.description);
            tv.setTypeface(getResources().getFont(R.font.nunito_regular));
            tv.setTextSize(15);
            tv.setTextColor(0xFF444444);
            tv.setPadding(dp(12), dp(8), dp(12), dp(8));
            row.addView(tv);
            binding.technicalSpecificationTable.addView(row);
        }
    }

    private void loadTopReviews(String productId) {
        // Lấy 3 review nhiều sao nhất
        java.util.List<Review> reviews = myDatabase.getTopReviewsByProductId(productId, 3);
        // Lấy tên user cho từng review
        HashMap<Integer, String> userNames = new HashMap<>();
        HashMap<Integer, Integer> userAvatars = new HashMap<>(); // Nếu có avatar resource
        int totalRating = 0;
        int totalCount = 0;
        // Đếm tổng số review và tính trung bình rating
        java.util.List<Review> allReviews = myDatabase.getAllReviewsByProductId(productId); // tối ưu: lấy toàn bộ review
        for (Review r : allReviews) {
            totalRating += r.rating;
            totalCount++;
        }
        double avg = totalCount > 0 ? (double) totalRating / totalCount : 0;
        binding.ratingAverage.setText(String.format("%.1f", avg));
        binding.ratingCount.setText("(" + totalCount + ")");
        // Lấy tên user cho 3 review top
        for (Review r : reviews) {
            if (!userNames.containsKey(r.user_id)) {
                User u = myDatabase.getUserById(r.user_id);
                userNames.put(r.user_id, u != null && u.full_name != null ? u.full_name : "Ẩn danh");
            }
        }
        // Hiển thị lên RecyclerView
        binding.lvComments.setLayoutManager(new LinearLayoutManager(this));
        ReviewAdapter adapter = new ReviewAdapter(this, reviews, userNames, userAvatars);
        binding.lvComments.setAdapter(adapter);
    }

    private void loadRelatedProducts() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        List<MyDatabase.Product> related = myDatabase.getRandomRelatedProducts(product.category_id, product.product_id, 3);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (MyDatabase.Product p : related) {
            View item = inflater.inflate(R.layout.product_item, container, false);
            ImageView img = item.findViewById(R.id.imvThumbnail);
            TextView tvName = item.findViewById(R.id.txtProductName);
            TextView tvPrice = item.findViewById(R.id.tvDiscountedPrice);
            TextView tvOriginalPrice = item.findViewById(R.id.tvOriginalPrice);
            TextView tvDiscount = item.findViewById(R.id.tvDiscount);

            tvName.setText(p.name);
            tvPrice.setText(String.format("%,.0f₫", p.discounted_price));
            tvOriginalPrice.setText(String.format("%,.0f₫", p.original_price));
            if (p.original_price > 0 && p.discounted_price < p.original_price) {
                tvOriginalPrice.setVisibility(View.VISIBLE);
                tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                int percent = (int) ((1 - p.discounted_price / p.original_price) * 100);
                tvDiscount.setText("-" + percent + "%");
            } else {
                tvOriginalPrice.setVisibility(View.INVISIBLE);
                tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
                tvDiscount.setText("");
            }
            if (p.thumbnail_url != null) {
                img.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(p.thumbnail_url, 0, p.thumbnail_url.length));
            } else {
                img.setImageResource(R.drawable.ic_launcher_background);
            }
            item.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProductDetailActivity.class);
                intent.putExtra("product_id", p.product_id);
                startActivity(intent);
            });
            ImageView btnAddToCart = item.findViewById(R.id.ivCart);
            btnAddToCart.setOnClickListener(v -> {
                if (!sessionManager.isLoggedIn()) {
                    // Hiển thị dialog yêu cầu đăng nhập
                    LoginRequiredDialog dialog = new LoginRequiredDialog(this);
                    dialog.show();
                } else {
                    // Mở dialog chọn biến thể và số lượng
                    AddToCartDialog cartDialog = new AddToCartDialog(this, p);
                    cartDialog.show();
                }
            });
            container.addView(item);
        }
        binding.relatedProductsScroll.removeAllViews();
        binding.relatedProductsScroll.addView(container);
    }

    // Helper chuyển dp sang px
    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}