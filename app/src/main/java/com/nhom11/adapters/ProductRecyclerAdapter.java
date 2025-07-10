package com.nhom11.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom11.innowave.AddToCartDialog;
import com.nhom11.innowave.LoginAlertDialog;
import com.nhom11.innowave.MyDatabase;
import com.nhom11.innowave.UserSessionManager;
import com.nhom11.innowave.LogActivity;
import com.nhom11.innowave.R;
import com.nhom11.models.Product;

import java.util.List;

public class ProductRecyclerAdapter extends RecyclerView.Adapter<ProductRecyclerAdapter.ViewHolder> {
    private Context context;
    private List<Product> products;
    private OnItemClickListener onItemClickListener;
    private UserSessionManager sessionManager;

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public ProductRecyclerAdapter(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
        this.sessionManager = new UserSessionManager(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.product_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.txtProductName.setText(product.name);
        holder.tvDiscountedPrice.setText(formatPrice(product.discounted_price));
        holder.tvOriginalPrice.setText(formatPrice(product.original_price));
        if (product.original_price > 0 && product.discounted_price < product.original_price) {
            holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvOriginalPrice.setVisibility(View.VISIBLE);
            int percent = (int) Math.round(100.0 * (product.original_price - product.discounted_price) / product.original_price);
            holder.tvDiscount.setText("-" + percent + "%");
        } else {
            holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvOriginalPrice.setVisibility(View.INVISIBLE);
            holder.tvDiscount.setText("");
        }
        if (product.thumbnail_url != null && product.thumbnail_url.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(product.thumbnail_url, 0, product.thumbnail_url.length);
            holder.imvThumbnail.setImageBitmap(bitmap);
        } else {
            holder.imvThumbnail.setImageResource(R.drawable.ic_launcher_background);
        }
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) onItemClickListener.onItemClick(product);
        });
        
        // Setup cart button click
        holder.ivCart.setOnClickListener(v -> {
            if (!sessionManager.isLoggedIn()) {
                LoginAlertDialog loginDialog = new LoginAlertDialog(context);
                loginDialog.show();
            } else {
                MyDatabase db = new MyDatabase(context);
                MyDatabase.Product dbProduct = db.getProductById(product.product_id);
                db.close();
                if (dbProduct != null) {
                    AddToCartDialog cartDialog = new AddToCartDialog(context, dbProduct);
                    cartDialog.show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imvThumbnail, ivCart;
        TextView txtProductName, tvDiscountedPrice, tvOriginalPrice, tvDiscount;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imvThumbnail = itemView.findViewById(R.id.imvThumbnail);
            ivCart = itemView.findViewById(R.id.ivCart);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            tvDiscountedPrice = itemView.findViewById(R.id.tvDiscountedPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            tvDiscount = itemView.findViewById(R.id.tvDiscount);
        }
    }

    private String formatPrice(double price) {
        java.text.DecimalFormat formatter = new java.text.DecimalFormat("###,###,###");
        return "đ " + formatter.format(price);
    }

    private void handleCartClick(Product product) {
        if (!sessionManager.isLoggedIn()) {
            // Hiển thị dialog alert yêu cầu đăng nhập
            LoginAlertDialog loginDialog = new LoginAlertDialog(context);
            loginDialog.show();
        } else {
            // Thêm trực tiếp vào giỏ hàng
            addToCart(product);
        }
    }

    private void addToCart(Product product) {
        try {
            MyDatabase db = new MyDatabase(context);
            int userId = sessionManager.getUserId();
            long cartId = db.createCartIfNotExists(userId);
            // Thêm item vào cart với số lượng 1, chỉ truyền variantId
            boolean success = db.addCartItem(cartId, product.default_variant_id, 1);
            if (success) {
                // Gửi broadcast cập nhật giỏ hàng
                Intent intent = new Intent("ACTION_CART_UPDATED");
                androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                android.widget.Toast.makeText(context, "Đã thêm vào giỏ hàng", android.widget.Toast.LENGTH_SHORT).show();
            } else {
                android.widget.Toast.makeText(context, "Có lỗi xảy ra khi thêm vào giỏ hàng", android.widget.Toast.LENGTH_SHORT).show();
            }
            db.close();
        } catch (Exception e) {
            android.widget.Toast.makeText(context, "Có lỗi xảy ra: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }
} 