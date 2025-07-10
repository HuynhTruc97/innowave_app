package com.nhom11.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nhom11.innowave.AddToCartDialog;
import com.nhom11.innowave.LoginAlertDialog;
import com.nhom11.innowave.MyDatabase;
import com.nhom11.innowave.UserSessionManager;
import com.nhom11.innowave.LogActivity;
import com.nhom11.innowave.R;
import com.nhom11.models.Product;

import java.text.DecimalFormat;
import java.util.List;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ProductAdapter extends BaseAdapter {
    private Context context;
    private List<Product> products;
    private LayoutInflater inflater;
    private OnItemClickListener onItemClickListener;
    private UserSessionManager sessionManager;

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public ProductAdapter(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
        this.inflater = LayoutInflater.from(context);
        this.sessionManager = new UserSessionManager(context);
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public Object getItem(int position) {
        return products.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.product_item, parent, false);
            holder = new ViewHolder();
            holder.imvThumbnail = convertView.findViewById(R.id.imvThumbnail);
            holder.txtProductName = convertView.findViewById(R.id.txtProductName);
            holder.tvDiscountedPrice = convertView.findViewById(R.id.tvDiscountedPrice);
            holder.tvOriginalPrice = convertView.findViewById(R.id.tvOriginalPrice);
            holder.tvDiscount = convertView.findViewById(R.id.tvDiscount);
            holder.ivCart = convertView.findViewById(R.id.ivCart);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Product product = products.get(position);
        holder.txtProductName.setText(product.name);
        holder.tvDiscountedPrice.setText(formatPrice(product.discounted_price));
        holder.tvOriginalPrice.setText(formatPrice(product.original_price));
        if (product.original_price > 0 && product.discounted_price < product.original_price) {
            holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvOriginalPrice.setVisibility(View.VISIBLE);
        } else {
            holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvOriginalPrice.setVisibility(View.INVISIBLE);
        }
        holder.tvDiscount.setText(getDiscountPercent(product.original_price, product.discounted_price));
        if (product.thumbnail_url != null && product.thumbnail_url.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(product.thumbnail_url, 0, product.thumbnail_url.length);
            holder.imvThumbnail.setImageBitmap(bitmap);
        } else {
            holder.imvThumbnail.setImageResource(R.drawable.ic_launcher_background);
        }
        
        // Setup click listeners
        if (onItemClickListener != null) {
            convertView.setOnClickListener(v -> onItemClickListener.onItemClick(product));
        } else {
            convertView.setOnClickListener(null);
        }
        
        // Setup cart button click
        holder.ivCart.setClickable(true);
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
        
        return convertView;
    }

    private String formatPrice(double price) {
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        return "đ " + formatter.format(price);
    }

    private String getDiscountPercent(double original, double discounted) {
        if (original <= 0 || discounted >= original) return "";
        int percent = (int) Math.round(100.0 * (original - discounted) / original);
        return "-" + percent + "%";
    }

    static class ViewHolder {
        ImageView imvThumbnail, ivCart;
        TextView txtProductName, tvDiscountedPrice, tvOriginalPrice, tvDiscount;
    }
} 