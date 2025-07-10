package com.nhom11.innowave;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageButton;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import android.graphics.Color;

import com.nhom11.adapters.ProductRecyclerAdapter;
import com.nhom11.innowave.MyDatabase.Category;
import java.util.HashSet;
import java.util.Set;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import androidx.core.content.ContextCompat;
import android.util.TypedValue;
import com.nhom11.innowave.databinding.ActivityProductPageBinding;
import com.nhom11.models.Product;
import android.content.Context;

public class ProductPageActivity extends AppCompatActivity {

    ActivityProductPageBinding binding;
    private ProductRecyclerAdapter productRecyclerAdapter;
    private MyDatabase myDatabase;
    private List<MyDatabase.Product> products;
    private List<MyDatabase.Product> allProducts;
    private UserSessionManager sessionManager;
    private LogActivity logActivity;

    // Trạng thái sort/filter
    private String currentSort = "default";
    private Integer minPrice = null, maxPrice = null;
    private String filterType = null;
    private Dialog sortDialog, filterDialog;
    private Set<Integer> selectedCategoryIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityProductPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.recyclerViewProducts.setLayoutManager(new GridLayoutManager(this, 2));
        myDatabase = new MyDatabase(this);
        sessionManager = new UserSessionManager(this);
        logActivity = new LogActivity(this);
        allProducts = myDatabase.getAllProducts();
        products = new ArrayList<>(allProducts);
        // Chuyển đổi sang List<Product> model
        List<Product> modelProducts = new ArrayList<>();
        for (MyDatabase.Product p : products) {
            modelProducts.add(new Product(
                p.product_id, p.name, p.category_id, p.default_variant_id, p.thumbnail_url,
                p.description, p.original_price, p.discounted_price, p.is_active, p.created_at, p.updated_at
            ));
        }
        productRecyclerAdapter = new ProductRecyclerAdapter(this, modelProducts);
        productRecyclerAdapter.setOnItemClickListener(product -> {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.product_id);
            startActivity(intent);
        });
        binding.recyclerViewProducts.setAdapter(productRecyclerAdapter);

        // Search realtime khi nhập text
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                doSearchSortFilter();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        binding.icSearch.setOnClickListener(v -> {
            android.util.Log.d("ProductPage", "icSearch clicked!");
            doSearchSortFilter();
        });

        // Nút back: mở fragment homepage
        binding.icBack.setOnClickListener(v -> {
            android.util.Log.d("ProductPage", "icBack clicked!");
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("open_home", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        // Nút cart: mở fragment cart
        binding.icCart.setOnClickListener(v -> {
            android.util.Log.d("ProductPage", "icCart clicked!");
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("open_cart", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Mở dialog sort
        binding.layoutSortFilter.btnSort.setOnClickListener(v -> showSortDialog());
        // Mở dialog filter
        binding.layoutSortFilter.btnFilter.setOnClickListener(v -> showFilterDialog());

        // Xử lý search query từ FragmentHomePage
        Intent intent = getIntent();
        if (intent != null) {
            String searchQuery = intent.getStringExtra("search_query");
            boolean focusSearch = intent.getBooleanExtra("focus_search", false);
            
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                binding.etSearch.setText(searchQuery);
                binding.etSearch.setSelection(searchQuery.length());
                
                if (focusSearch) {
                    binding.etSearch.requestFocus();
                    binding.etSearch.post(() -> {
                        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.showSoftInput(binding.etSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                }
                
                // Ghi log search nếu user đã đăng nhập
                if (sessionManager.isLoggedIn()) {
                    int userId = sessionManager.getUserId();
                    logActivity.logSearch(userId, searchQuery);
                }
                
                // Thực hiện search ngay lập tức
                doSearchSortFilter();
            }
        }
    }

    private void showSortDialog() {
        if (sortDialog != null && sortDialog.isShowing()) sortDialog.dismiss();
        sortDialog = new Dialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_sort_options, null);
        sortDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        sortDialog.setContentView(view);
        Window window = sortDialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setGravity(android.view.Gravity.BOTTOM);
        }
        sortDialog.show();
        // Lấy view
        TextView tvDefault = view.findViewById(R.id.tvDefault);
        TextView tvPopular = view.findViewById(R.id.tvPopular);
        TextView tvNewest = view.findViewById(R.id.tvNewest);
        TextView tvPriceAsc = view.findViewById(R.id.tvPriceAsc);
        TextView tvPriceDesc = view.findViewById(R.id.tvPriceDesc);
        TextView tvReset = view.findViewById(R.id.tvReset);
        ImageButton btnCancel = view.findViewById(R.id.btnCancel);
        // Xử lý chọn sort
        tvDefault.setOnClickListener(v -> { currentSort = "default"; sortDialog.dismiss(); doSearchSortFilter(); });
        tvPopular.setOnClickListener(v -> { currentSort = "popular"; sortDialog.dismiss(); doSearchSortFilter(); });
        tvNewest.setOnClickListener(v -> { currentSort = "newest"; sortDialog.dismiss(); doSearchSortFilter(); });
        tvPriceAsc.setOnClickListener(v -> { currentSort = "price_asc"; sortDialog.dismiss(); doSearchSortFilter(); });
        tvPriceDesc.setOnClickListener(v -> { currentSort = "price_desc"; sortDialog.dismiss(); doSearchSortFilter(); });
        tvReset.setOnClickListener(v -> { currentSort = "default"; sortDialog.dismiss(); doSearchSortFilter(); });
        btnCancel.setOnClickListener(v -> sortDialog.dismiss());
    }

    private void showFilterDialog() {
        if (filterDialog != null && filterDialog.isShowing()) filterDialog.dismiss();
        filterDialog = new Dialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_filter_dialog, null);
        filterDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        filterDialog.setContentView(view);
        Window window = filterDialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setGravity(android.view.Gravity.BOTTOM);
        }
        filterDialog.show();
        // Lấy view
        EditText etMinPrice = view.findViewById(R.id.etMinPrice);
        EditText etMaxPrice = view.findViewById(R.id.etMaxPrice);
        Button btnApply = view.findViewById(R.id.btnApply);
        TextView tvReset = view.findViewById(R.id.tvReset);
        ImageButton btnCancel = view.findViewById(R.id.btnCancel);
        // Dynamic category chips
        ChipGroup categoryChipGroup = view.findViewById(R.id.categoryChipGroup);
        categoryChipGroup.removeAllViews();
        List<Category> categories = myDatabase.getAllCategories();
        for (Category cat : categories) {
            Chip chip = new Chip(this);
            chip.setText(cat.name);
            chip.setTag(cat.category_id);
            chip.setCheckable(true);
            chip.setChecked(selectedCategoryIds.contains(cat.category_id));
            if (selectedCategoryIds.contains(cat.category_id)) {
                chip.setTextColor(Color.WHITE);
                chip.setChipBackgroundColorResource(R.color.secondary_color);
            } else {
                chip.setTextColor(ContextCompat.getColor(this, R.color.primary_color));
                chip.setChipBackgroundColorResource(android.R.color.white);
            }
            chip.setTextSize(15);
            ChipGroup.LayoutParams params = new ChipGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(16, 8, 16, 8);
            chip.setLayoutParams(params);
            chip.setPadding(24, 0, 24, 0);
            chip.setMinHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 44, getResources().getDisplayMetrics()));
            chip.setCheckedIconVisible(false);
            chip.setCloseIconVisible(false);
            chip.setOnClickListener(v -> {
                int id = (int) chip.getTag();
                boolean isSelected = selectedCategoryIds.contains(id);
                if (isSelected) {
                    selectedCategoryIds.remove(id);
                    chip.setTextColor(ContextCompat.getColor(this, R.color.primary_color));
                    chip.setChipBackgroundColorResource(android.R.color.white);
                } else {
                    selectedCategoryIds.add(id);
                    chip.setTextColor(Color.WHITE);
                    chip.setChipBackgroundColorResource(R.color.secondary_color);
                }
            });
            categoryChipGroup.addView(chip);
        }
        // Set lại giá trị min/max nếu đã nhập trước đó
        if (minPrice != null) etMinPrice.setText(String.valueOf(minPrice));
        if (maxPrice != null) etMaxPrice.setText(String.valueOf(maxPrice));
        // Áp dụng filter
        btnApply.setOnClickListener(v -> {
            // Lấy giá trị min/max
            String minStr = etMinPrice.getText().toString().trim();
            String maxStr = etMaxPrice.getText().toString().trim();
            minPrice = TextUtils.isEmpty(minStr) ? null : Integer.valueOf(minStr);
            maxPrice = TextUtils.isEmpty(maxStr) ? null : Integer.valueOf(maxStr);
            filterDialog.dismiss();
            doSearchSortFilter();
        });
        // Reset filter
        tvReset.setOnClickListener(v -> {
            minPrice = null; maxPrice = null; selectedCategoryIds.clear();
            etMinPrice.setText(""); etMaxPrice.setText("");
            for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
                View child = categoryChipGroup.getChildAt(i);
                if (child instanceof Chip) {
                    ((Chip) child).setChecked(false);
                }
            }
        });
        btnCancel.setOnClickListener(v -> filterDialog.dismiss());
    }

    // Kết hợp search, sort, filter
    private void doSearchSortFilter() {
        String query = binding.etSearch.getText().toString().trim().toLowerCase();
        List<MyDatabase.Product> filtered = new ArrayList<>();
        for (MyDatabase.Product p : allProducts) {
            // Search theo tên
            if (!TextUtils.isEmpty(query) && !p.name.toLowerCase().contains(query)) continue;
            // Filter theo giá
            if (minPrice != null && p.discounted_price < minPrice) continue;
            if (maxPrice != null && p.discounted_price > maxPrice) continue;
            // Filter theo category
            if (!selectedCategoryIds.isEmpty() && !selectedCategoryIds.contains(p.category_id)) continue;
            filtered.add(p);
        }
        // Sort
        if (currentSort.equals("price_asc")) {
            Collections.sort(filtered, Comparator.comparingDouble(p -> p.discounted_price));
        } else if (currentSort.equals("price_desc")) {
            Collections.sort(filtered, (a, b) -> Double.compare(b.discounted_price, a.discounted_price));
        } else if (currentSort.equals("popular") || currentSort.equals("newest")) {
            Collections.shuffle(filtered, new Random());
        }
        // Chuyển đổi sang List<Product> model
        List<Product> modelProducts = new ArrayList<>();
        for (MyDatabase.Product p : filtered) {
            modelProducts.add(new Product(
                p.product_id, p.name, p.category_id, p.default_variant_id, p.thumbnail_url,
                p.description, p.original_price, p.discounted_price, p.is_active, p.created_at, p.updated_at
            ));
        }
        productRecyclerAdapter = new ProductRecyclerAdapter(this, modelProducts);
        productRecyclerAdapter.setOnItemClickListener(product -> {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.product_id);
            startActivity(intent);
        });
        binding.recyclerViewProducts.setAdapter(productRecyclerAdapter);
    }
}