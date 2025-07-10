package com.nhom11.innowave;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.nhom11.innowave.databinding.FragmentOrderWaitingPickupBinding;
import com.nhom11.innowave.databinding.ItemBeingPreparedBinding;
import com.nhom11.innowave.databinding.ItemProductPaymentBinding;
import java.util.ArrayList;
import java.util.List;
import android.graphics.Paint;
import com.nhom11.models.Order;
import com.nhom11.models.OrderItem;

public class FragmentOrderWaitingPickup extends Fragment {
    private FragmentOrderWaitingPickupBinding binding;
    private MyDatabase db;
    private List<Order> orders;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderWaitingPickupBinding.inflate(inflater, container, false);
        db = new MyDatabase(requireContext());
        loadOrders();
        return binding.getRoot();
    }

    private void loadOrders() {
        int userId = new UserSessionManager(requireContext()).getUserId();
        orders = db.getOrdersByStatus(userId, "waiting_for_pickup");
        android.util.Log.d("OrderDebug", "orders.size=" + orders.size());
        OrderAdapter adapter = new OrderAdapter(orders);
        binding.recyclerViewOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewOrders.setAdapter(adapter);
    }

    private class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
        private List<Order> orderList;
        public OrderAdapter(List<Order> orderList) { this.orderList = orderList; }
        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemBeingPreparedBinding itemBinding = ItemBeingPreparedBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new OrderViewHolder(itemBinding);
        }
        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            Order order = orderList.get(position);
            holder.bind(order);
        }
        @Override
        public int getItemCount() { return orderList.size(); }
        class OrderViewHolder extends RecyclerView.ViewHolder {
            private final ItemBeingPreparedBinding itemBinding;
            private boolean expanded = false;
            public OrderViewHolder(ItemBeingPreparedBinding binding) {
                super(binding.getRoot());
                this.itemBinding = binding;
            }
            public void bind(Order order) {
                // Trạng thái đơn
                // Xóa dòng này để không set trạng thái cho tvStatus nữa
                // itemBinding.tvStatus.setText(order.status != null ? order.status : "Chờ lấy hàng");
                // Tổng tiền
                itemBinding.tvPaymentAmount.setText(String.format("%,.0f₫", order.totalAmount));
                // Hiển thị sản phẩm trong đơn
                List<OrderItem> orderItems = db.getOrderItemsByOrderId(order.orderId);
                int showCount = expanded ? orderItems.size() : Math.min(1, orderItems.size());
                List<OrderItem> showItems = orderItems.subList(0, showCount);
                android.util.Log.d("OrderDebug", "OrderId=" + order.orderId + ", orderItems.size=" + orderItems.size() + ", showItems.size=" + showItems.size() + ", expanded=" + expanded);
                if (itemBinding.rvOrderItems.getLayoutManager() == null) {
                    itemBinding.rvOrderItems.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(itemView.getContext()));
                }
                itemBinding.rvOrderItems.setAdapter(new OrderItemAdapter(showItems));
                // Nút xem thêm
                itemBinding.btnViewMore.setVisibility(orderItems.size() > 1 && !expanded ? View.VISIBLE : View.GONE);
                itemBinding.btnViewMore.setOnClickListener(v -> {
                    expanded = true;
                    android.util.Log.d("OrderDebug", "[Xem thêm] OrderId=" + order.orderId + ", expanded=true, orderItems.size=" + orderItems.size());
                    bind(order);
                });
            }
        }
    }
    private class OrderItemAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {
        private final List<OrderItem> items;
        public OrderItemAdapter(List<OrderItem> items) { this.items = items; }
        @Override public OrderItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ItemProductPaymentBinding binding = ItemProductPaymentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new OrderItemViewHolder(binding);
        }
        @Override public void onBindViewHolder(OrderItemViewHolder holder, int position) {
            holder.bind(items.get(position));
        }
        @Override public int getItemCount() { return items.size(); }
        class OrderItemViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            private final ItemProductPaymentBinding binding;
            public OrderItemViewHolder(ItemProductPaymentBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
            public void bind(OrderItem item) {
                MyDatabase.ProductVariant variant = db.getProductVariantById(String.valueOf(item.variantId));
                MyDatabase.Product product = db.getProductByVariantId(String.valueOf(item.variantId));
                double[] prices = db.getProductPriceByVariantId(String.valueOf(item.variantId));
                double originalPrice = prices[0];
                double discountedPrice = prices[1];
                // Thêm log kiểm tra dữ liệu
                android.util.Log.d("OrderDebug", "OrderItem: variantId=" + item.variantId + ", quantity=" + item.quantity);
                android.util.Log.d("OrderDebug", "Variant: " + (variant != null ? variant.variant_name : "null"));
                android.util.Log.d("OrderDebug", "Product: " + (product != null ? product.name : "null"));
                android.util.Log.d("OrderDebug", "Prices: original=" + originalPrice + ", discounted=" + discountedPrice);
                // Ảnh biến thể
                if (variant != null && variant.thumbnail_url != null && variant.thumbnail_url.length > 0) {
                    binding.imgProduct.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(variant.thumbnail_url, 0, variant.thumbnail_url.length));
                } else if (product != null && product.thumbnail_url != null && product.thumbnail_url.length > 0) {
                    binding.imgProduct.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(product.thumbnail_url, 0, product.thumbnail_url.length));
                } else {
                    binding.imgProduct.setImageResource(R.drawable.ic_launcher_background);
                }
                binding.tvProductName.setText(product != null ? product.name : "");
                binding.tvColor.setText(variant != null ? variant.variant_name : "");
                binding.tvNewPrice.setText(String.format("%,.0f₫", discountedPrice));
                binding.tvOldPrice.setText(String.format("%,.0f₫", originalPrice));
                binding.tvOldPrice.setPaintFlags(binding.tvOldPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                binding.tvQuantity.setText("x" + item.quantity);
            }
        }
    }
} 