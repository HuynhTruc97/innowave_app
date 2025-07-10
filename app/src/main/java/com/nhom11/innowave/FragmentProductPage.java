package com.nhom11.innowave;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.nhom11.adapters.ProductRecyclerAdapter;
import com.nhom11.innowave.databinding.FragmentProductPageBinding;
import com.nhom11.models.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class FragmentProductPage extends Fragment {
    private FragmentProductPageBinding binding;
    private ProductRecyclerAdapter productRecyclerAdapter;
    private MyDatabase myDatabase;
    private List<MyDatabase.Product> products;
    private List<MyDatabase.Product> allProducts;
    private UserSessionManager sessionManager;
    private LogActivity logActivity;

    // Trạng thái sort/filter
    private String currentSort = "default";
    private Integer minPrice = null, maxPrice = null;
    private Dialog sortDialog, filterDialog;
    private Set<Integer> selectedCategoryIds = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProductPageBinding.inflate(inflater, container, false);
        myDatabase = new MyDatabase(requireContext());
        sessionManager = new UserSessionManager(requireContext());
        logActivity = new LogActivity(requireContext());
        allProducts = myDatabase.getAllProducts();
        products = new ArrayList<>(allProducts);
        List<Product> modelProducts = new ArrayList<>();
        for (MyDatabase.Product p : products) {
            modelProducts.add(new Product(
                p.product_id, p.name, p.category_id, p.default_variant_id, p.thumbnail_url,
                p.description, p.original_price, p.discounted_price, p.is_active, p.created_at, p.updated_at
            ));
        }
        productRecyclerAdapter = new ProductRecyclerAdapter(requireContext(), modelProducts);
        productRecyclerAdapter.setOnItemClickListener(product -> {
            Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
            intent.putExtra("product_id", product.product_id);
            startActivity(intent);
        });
        binding.recyclerViewProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerViewProducts.setAdapter(productRecyclerAdapter);

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
        binding.icSearch.setOnClickListener(v -> doSearchSortFilter());
        binding.icBack.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
        binding.icCart.setOnClickListener(v -> {
            // Chuyển sang fragment giỏ hàng bằng NavController
            androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(requireActivity(), R.id.container);
            navController.navigate(R.id.fragment_cart);
        });
        binding.layoutSortFilter.btnSort.setOnClickListener(v -> showSortDialog());
        binding.layoutSortFilter.btnFilter.setOnClickListener(v -> showFilterDialog());
        return binding.getRoot();
    }

    private void showSortDialog() {
        if (sortDialog != null && sortDialog.isShowing()) sortDialog.dismiss();
        sortDialog = new Dialog(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_sort_options, null);
        sortDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        sortDialog.setContentView(view);
        Window window = sortDialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setGravity(android.view.Gravity.BOTTOM);
        }
        sortDialog.show();
        TextView tvDefault = view.findViewById(R.id.tvDefault);
        TextView tvPopular = view.findViewById(R.id.tvPopular);
        TextView tvNewest = view.findViewById(R.id.tvNewest);
        TextView tvPriceAsc = view.findViewById(R.id.tvPriceAsc);
        TextView tvPriceDesc = view.findViewById(R.id.tvPriceDesc);
        TextView tvReset = view.findViewById(R.id.tvReset);
        ImageButton btnCancel = view.findViewById(R.id.btnCancel);
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
        filterDialog = new Dialog(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_filter_dialog, null);
        filterDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        filterDialog.setContentView(view);
        Window window = filterDialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setGravity(android.view.Gravity.BOTTOM);
        }
        filterDialog.show();
        EditText etMinPrice = view.findViewById(R.id.etMinPrice);
        EditText etMaxPrice = view.findViewById(R.id.etMaxPrice);
        Button btnApply = view.findViewById(R.id.btnApply);
        TextView tvReset = view.findViewById(R.id.tvReset);
        ImageButton btnCancel = view.findViewById(R.id.btnCancel);
        ChipGroup categoryChipGroup = view.findViewById(R.id.categoryChipGroup);
        categoryChipGroup.removeAllViews();
        List<MyDatabase.Category> categories = myDatabase.getAllCategories();
        for (MyDatabase.Category cat : categories) {
            Chip chip = new Chip(requireContext());
            chip.setText(cat.name);
            chip.setTag(cat.category_id);
            chip.setCheckable(true);
            chip.setChecked(selectedCategoryIds.contains(cat.category_id));
            if (selectedCategoryIds.contains(cat.category_id)) {
                chip.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
                chip.setChipBackgroundColorResource(R.color.secondary_color);
            } else {
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_color));
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
            chip.setMinHeight((int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 44, getResources().getDisplayMetrics()));
            chip.setCheckedIconVisible(false);
            chip.setCloseIconVisible(false);
            chip.setOnClickListener(v -> {
                int id = (int) chip.getTag();
                boolean isSelected = selectedCategoryIds.contains(id);
                if (isSelected) {
                    selectedCategoryIds.remove(id);
                    chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_color));
                    chip.setChipBackgroundColorResource(android.R.color.white);
                } else {
                    selectedCategoryIds.add(id);
                    chip.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
                    chip.setChipBackgroundColorResource(R.color.secondary_color);
                }
            });
            categoryChipGroup.addView(chip);
        }
        if (minPrice != null) etMinPrice.setText(String.valueOf(minPrice));
        if (maxPrice != null) etMaxPrice.setText(String.valueOf(maxPrice));
        btnApply.setOnClickListener(v -> {
            String minStr = etMinPrice.getText().toString().trim();
            String maxStr = etMaxPrice.getText().toString().trim();
            minPrice = TextUtils.isEmpty(minStr) ? null : Integer.valueOf(minStr);
            maxPrice = TextUtils.isEmpty(maxStr) ? null : Integer.valueOf(maxStr);
            filterDialog.dismiss();
            doSearchSortFilter();
        });
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

    private void doSearchSortFilter() {
        String query = binding.etSearch.getText().toString().trim().toLowerCase();
        List<MyDatabase.Product> filtered = new ArrayList<>();
        for (MyDatabase.Product p : allProducts) {
            if (!TextUtils.isEmpty(query) && !p.name.toLowerCase().contains(query)) continue;
            if (minPrice != null && p.discounted_price < minPrice) continue;
            if (maxPrice != null && p.discounted_price > maxPrice) continue;
            if (!selectedCategoryIds.isEmpty() && !selectedCategoryIds.contains(p.category_id)) continue;
            filtered.add(p);
        }
        if (currentSort.equals("price_asc")) {
            Collections.sort(filtered, Comparator.comparingDouble(p -> p.discounted_price));
        } else if (currentSort.equals("price_desc")) {
            Collections.sort(filtered, (a, b) -> Double.compare(b.discounted_price, a.discounted_price));
        } else if (currentSort.equals("popular") || currentSort.equals("newest")) {
            Collections.shuffle(filtered, new Random());
        }
        List<Product> modelProducts = new ArrayList<>();
        for (MyDatabase.Product p : filtered) {
            modelProducts.add(new Product(
                p.product_id, p.name, p.category_id, p.default_variant_id, p.thumbnail_url,
                p.description, p.original_price, p.discounted_price, p.is_active, p.created_at, p.updated_at
            ));
        }
        productRecyclerAdapter = new ProductRecyclerAdapter(requireContext(), modelProducts);
        productRecyclerAdapter.setOnItemClickListener(product -> {
            Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
            intent.putExtra("product_id", product.product_id);
            startActivity(intent);
        });
        binding.recyclerViewProducts.setAdapter(productRecyclerAdapter);
    }
} 