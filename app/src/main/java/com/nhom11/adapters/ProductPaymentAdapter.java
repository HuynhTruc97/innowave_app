package com.nhom11.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.util.Log;

import com.nhom11.innowave.MyDatabase;
import com.nhom11.innowave.R;

import java.util.List;
import androidx.recyclerview.widget.RecyclerView;
// Thêm các import cần thiết khác
import com.nhom11.models.PaymentItem;

public class ProductPaymentAdapter extends BaseAdapter {
    private Context context;
    private List<PaymentItem> items;

    public ProductPaymentAdapter(Context context, List<PaymentItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return items != null ? items.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_product_payment, parent, false);
            holder = new ViewHolder();
            holder.tvProductName = convertView.findViewById(R.id.tvProductName);
            holder.tvNewPrice = convertView.findViewById(R.id.tvNewPrice);
            holder.tvOldPrice = convertView.findViewById(R.id.tvOldPrice);
            holder.tvQuantity = convertView.findViewById(R.id.tvQuantity);
            holder.imgProduct = convertView.findViewById(R.id.imgProduct);
            holder.tvColor = convertView.findViewById(R.id.tvColor);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        PaymentItem item = items.get(position);
        holder.tvProductName.setText(item.productName);
        holder.tvColor.setText(item.variantName != null ? item.variantName : "");
        holder.tvNewPrice.setText(String.format("%,.0f₫", item.price));
        holder.tvOldPrice.setText(String.format("%,.0f₫", item.originalPrice));
        holder.tvOldPrice.setPaintFlags(holder.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        holder.tvQuantity.setText("x" + item.quantity);
        // Log kiểm tra dữ liệu ảnh variant
        if (item.thumbnailUrl != null && item.thumbnailUrl.length > 0) {
            Log.d("PaymentImage", "Đang load ảnh variant cho: " + item.productName + ", variant: " + item.variantName + ", size: " + item.thumbnailUrl.length);
            holder.imgProduct.setImageBitmap(BitmapFactory.decodeByteArray(item.thumbnailUrl, 0, item.thumbnailUrl.length));
        } else {
            Log.d("PaymentImage", "Không có ảnh variant cho: " + item.productName + ", variant: " + item.variantName);
            holder.imgProduct.setImageResource(R.drawable.ic_launcher_background);
        }
        return convertView;
    }

    static class ViewHolder {
        TextView tvProductName, tvNewPrice, tvOldPrice, tvQuantity, tvColor;
        ImageView imgProduct;
    }
} 