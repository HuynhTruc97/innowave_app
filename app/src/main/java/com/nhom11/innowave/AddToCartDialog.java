package com.nhom11.innowave;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.nhom11.innowave.databinding.DialogAddProductToCartBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;
import android.util.Log;

public class AddToCartDialog extends BottomSheetDialog {
    private Context context;
    private MyDatabase.Product product;
    private List<MyDatabase.ProductVariant> variants;
    private MyDatabase.ProductVariant selectedVariant;
    private int quantity = 1;
    private UserSessionManager sessionManager;
    private LogActivity logActivity;
    private DialogAddProductToCartBinding binding;

    public AddToCartDialog(@NonNull Context context, MyDatabase.Product product) {
        super(context, com.google.android.material.R.style.Theme_Design_BottomSheetDialog);
        this.context = context;
        this.product = product;
        this.sessionManager = new UserSessionManager(context);
        this.logActivity = new LogActivity(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = DialogAddProductToCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set dialog to full width, bottom gravity, wrap content height
        Window window = getWindow();
        if (window != null) {
            window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Load variants
        MyDatabase db = new MyDatabase(context);
        variants = db.getVariantsByProductId(product.product_id);
        selectedVariant = null;
        for (MyDatabase.ProductVariant v : variants) {
            if (v.variant_id.equals(product.default_variant_id)) {
                selectedVariant = v;
                break;
            }
        }
        if (selectedVariant == null && !variants.isEmpty()) {
            selectedVariant = variants.get(0);
        }

        // Cập nhật từng view riêng biệt như ProductDetailActivity
        binding.txtProductName.setText(product.name);
        binding.txtNewPrice.setText(String.format("%,.0f₫", product.discounted_price));
        binding.tvOldPrice.setText(String.format("%,.0f₫", product.original_price));
        if (product.original_price > 0 && product.discounted_price < product.original_price) {
            binding.tvOldPrice.setVisibility(View.VISIBLE);
            binding.tvOldPrice.setPaintFlags(binding.tvOldPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            int percent = (int) ((1 - product.discounted_price / product.original_price) * 100);
            binding.txtDiscountPercent.setText("-" + percent + "%");
        } else {
            binding.tvOldPrice.setVisibility(View.GONE);
            binding.txtDiscountPercent.setText("");
        }
        if (selectedVariant != null && selectedVariant.thumbnail_url != null) {
            binding.imvPhoto.setImageBitmap(BitmapFactory.decodeByteArray(selectedVariant.thumbnail_url, 0, selectedVariant.thumbnail_url.length));
        } else if (product.thumbnail_url != null) {
            binding.imvPhoto.setImageBitmap(BitmapFactory.decodeByteArray(product.thumbnail_url, 0, product.thumbnail_url.length));
        } else {
            binding.imvPhoto.setImageResource(R.drawable.ic_launcher_background);
        }
        binding.labelProductColor.setText(getContext().getString(R.string.product_color));
        binding.productColor.setText(selectedVariant != null ? (" " + selectedVariant.variant_name) : "");
        binding.labelQuantity.setText(getContext().getString(R.string.quantity));
        binding.tvQuantity.setText(String.valueOf(quantity));

        setupVariantChips();
        setupQuantityControls();
        setupButtons();
    }

    private void setupVariantChips() {
        ChipGroup chipGroup = binding.variantChipGroup;
        chipGroup.removeAllViews();
        for (MyDatabase.ProductVariant v : variants) {
            Chip chip = new Chip(context);
            chip.setText(v.variant_name);
            chip.setCheckable(true);
            chip.setChecked(selectedVariant != null && v.variant_id.equals(selectedVariant.variant_id));
            chip.setOnClickListener(view -> {
                selectedVariant = v;
                // Chỉ cập nhật ảnh và tên màu sắc khi chọn chip
                if (selectedVariant != null && selectedVariant.thumbnail_url != null) {
                    binding.imvPhoto.setImageBitmap(BitmapFactory.decodeByteArray(selectedVariant.thumbnail_url, 0, selectedVariant.thumbnail_url.length));
                } else if (product.thumbnail_url != null) {
                    binding.imvPhoto.setImageBitmap(BitmapFactory.decodeByteArray(product.thumbnail_url, 0, product.thumbnail_url.length));
                } else {
                    binding.imvPhoto.setImageResource(R.drawable.ic_launcher_background);
                }
                binding.productColor.setText(" " + v.variant_name);
                // Cập nhật lại trạng thái chip
                for (int i = 0; i < chipGroup.getChildCount(); i++) {
                    Chip c = (Chip) chipGroup.getChildAt(i);
                    c.setChecked(c == chip);
                }
            });
            chipGroup.addView(chip);
        }
    }

    private void setupQuantityControls() {
        binding.tvQuantity.setText(String.valueOf(quantity));
        binding.btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.tvQuantity.setText(String.valueOf(quantity));
            }
        });
        binding.btnPlus.setOnClickListener(v -> {
            quantity++;
            binding.tvQuantity.setText(String.valueOf(quantity));
        });
    }

    private void setupButtons() {
        if (binding.btnClose != null) {
            binding.btnClose.setOnClickListener(v -> dismiss());
        }
        binding.btnAddToCart.setOnClickListener(v -> {
            if (selectedVariant == null) {
                Toast.makeText(context, "Vui lòng chọn biến thể sản phẩm", Toast.LENGTH_SHORT).show();
                return;
            }
            addToCart();
        });
    }

    private void addToCart() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(context, "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int userId = sessionManager.getUserId();
        MyDatabase db = new MyDatabase(context);
        
        try {
            // Tạo cart nếu chưa có
            long cartId = db.createCartIfNotExists(userId);
            
            // Thêm item vào cart
            android.util.Log.d("AddToCartDialog", "userId=" + userId + ", cartId=" + cartId + ", variantId=" + selectedVariant.variant_id + ", quantity=" + quantity);
            boolean success = db.addCartItem(cartId, selectedVariant.variant_id, quantity);
            android.util.Log.d("AddToCartDialog", "addCartItem success=" + success);
            
            if (success) {
                // Ghi log hoạt động
                logActivity.logActivity(userId, "ADD_TO_CART", "Added product " + product.name + " to cart");
                // Gửi broadcast cập nhật giỏ hàng
                Intent intent = new Intent("ACTION_CART_UPDATED");
                androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                dismiss();
            } else {
                Toast.makeText(context, "Có lỗi xảy ra khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Có lỗi xảy ra: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
    }
} 