package com.nhom11.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import android.database.Cursor;
import android.graphics.Paint;

import com.nhom11.innowave.MyDatabase;
import com.nhom11.innowave.R;

import java.text.DecimalFormat;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {
    private Context context;
    private List<MyDatabase.CartItem> cartItems;
    private Set<Integer> checkedCartItemIds = new HashSet<>();
    private OnCartItemCheckedChangeListener checkedChangeListener;

    public interface OnCartItemCheckedChangeListener {
        void onCheckedChanged(Set<Integer> checkedPositions);
    }
    public void setOnCartItemCheckedChangeListener(OnCartItemCheckedChangeListener listener) {
        this.checkedChangeListener = listener;
    }

    public CartItemAdapter(Context context, List<MyDatabase.CartItem> cartItems) {
        this(context, cartItems, null);
    }

    public CartItemAdapter(Context context, List<MyDatabase.CartItem> cartItems, Set<Integer> checkedIds) {
        this.context = context;
        this.cartItems = cartItems;
        if (checkedIds != null) {
            this.checkedCartItemIds = new HashSet<>(checkedIds);
        }
    }

    public void setCheckedCartItemIds(Set<Integer> checkedIds) {
        if (checkedIds != null) {
            this.checkedCartItemIds = new HashSet<>(checkedIds);
            notifyDataSetChanged();
        }
    }

    public Set<Integer> getCheckedCartItemIds() {
        return checkedCartItemIds;
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MyDatabase.CartItem item = cartItems.get(position);
        holder.txtProductName.setText(item.product_name);
        holder.txtVariantName.setText(item.variant_name);
        holder.txtPrice.setText(formatPrice(item.price));
        holder.txtQuantity.setText(String.valueOf(item.quantity));
        holder.cbSelect.setChecked(checkedCartItemIds.contains(item.cart_item_id));
        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) checkedCartItemIds.add(item.cart_item_id);
            else checkedCartItemIds.remove(item.cart_item_id);
            if (checkedChangeListener != null) checkedChangeListener.onCheckedChanged(checkedCartItemIds);
        });
        double originalPrice = getOriginalPriceForVariant(item.variant_id);
        if (originalPrice > item.price) {
            holder.txtOldPrice.setVisibility(View.VISIBLE);
            holder.txtOldPrice.setText(formatPrice(originalPrice));
            holder.txtOldPrice.setPaintFlags(holder.txtOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.txtOldPrice.setVisibility(View.GONE);
        }
        if (item.thumbnail_url != null && item.thumbnail_url.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(item.thumbnail_url, 0, item.thumbnail_url.length);
            holder.imvThumbnail.setImageBitmap(bitmap);
        } else {
            holder.imvThumbnail.setImageResource(R.drawable.ic_launcher_background);
        }
        holder.btnPlus.setOnClickListener(v -> {
            int newQuantity = item.quantity + 1;
            MyDatabase db = new MyDatabase(context);
            boolean success = db.updateCartItemQuantity(item.cart_item_id, newQuantity);
            db.close();
            if (success) {
                item.quantity = newQuantity;
                holder.txtQuantity.setText(String.valueOf(newQuantity));
                Intent intent = new Intent("ACTION_CART_UPDATED");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                if (checkedChangeListener != null) checkedChangeListener.onCheckedChanged(checkedCartItemIds);
            } else {
                Toast.makeText(context, "Lỗi khi cập nhật số lượng", Toast.LENGTH_SHORT).show();
            }
        });
        holder.btnMinus.setOnClickListener(v -> {
            if (item.quantity > 1) {
                int newQuantity = item.quantity - 1;
                MyDatabase db = new MyDatabase(context);
                boolean success = db.updateCartItemQuantity(item.cart_item_id, newQuantity);
                db.close();
                if (success) {
                    item.quantity = newQuantity;
                    holder.txtQuantity.setText(String.valueOf(newQuantity));
                    Intent intent = new Intent("ACTION_CART_UPDATED");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    if (checkedChangeListener != null) checkedChangeListener.onCheckedChanged(checkedCartItemIds);
                } else {
                    Toast.makeText(context, "Lỗi khi cập nhật số lượng", Toast.LENGTH_SHORT).show();
                }
            }
        });
        holder.btnDelete.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(context)
                .setTitle("Xác nhận")
                .setMessage("Bạn có muốn xóa sản phẩm này ra khỏi giỏ hàng không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    MyDatabase db = new MyDatabase(context);
                    db.deleteCartItem(item.cart_item_id);
                    db.close();
                    android.widget.Toast.makeText(context, "Đã xóa sản phẩm khỏi giỏ hàng", android.widget.Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent("ACTION_CART_UPDATED");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    if (checkedChangeListener != null) checkedChangeListener.onCheckedChanged(checkedCartItemIds);
                })
                .setNegativeButton("Không", null)
                .show();
        });
    }

    private String formatPrice(double price) {
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        return "đ " + formatter.format(price);
    }

    private double getOriginalPriceForVariant(String variantId) {
        MyDatabase db = new MyDatabase(context);
        double price = 0;
        try {
            Cursor cursor = db.getReadableDatabase().rawQuery("SELECT pv.variant_id, p.original_price FROM PRODUCT_VARIANT pv JOIN PRODUCT p ON pv.product_id = p.product_id WHERE pv.variant_id = ?", new String[]{variantId});
            if (cursor.moveToFirst()) {
                price = cursor.getDouble(cursor.getColumnIndexOrThrow("original_price"));
            }
            cursor.close();
        } catch (Exception e) {}
        db.close();
        return price;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imvThumbnail;
        TextView txtProductName, txtVariantName, txtPrice, txtQuantity, txtOldPrice;
        CheckBox cbSelect;
        ImageView btnPlus, btnMinus, btnDelete;
        ViewHolder(View itemView) {
            super(itemView);
            imvThumbnail = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.tvProductName);
            txtVariantName = itemView.findViewById(R.id.tvProductColor);
            txtPrice = itemView.findViewById(R.id.tvProductPrice);
            txtQuantity = itemView.findViewById(R.id.tvQuantity);
            txtOldPrice = itemView.findViewById(R.id.tvProductOldPrice);
            cbSelect = itemView.findViewById(R.id.cbSelect);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
} 