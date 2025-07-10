package com.nhom11.innowave;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import com.nhom11.innowave.databinding.ActivityManageOrderBinding;
import com.nhom11.models.Order;
import com.nhom11.models.OrderItem;
import java.util.List;
import android.widget.TextView;
import android.widget.ImageView;
import android.graphics.Paint;
import com.nhom11.innowave.FragmentOrderWaitingPickup;
import java.util.HashMap;
import java.util.Map;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class ManageOrderActivity extends AppCompatActivity {
    ActivityManageOrderBinding binding;
    private MyDatabase db;
    private UserSessionManager sessionManager;
    private OrderAdapter orderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = new MyDatabase(this);
        sessionManager = new UserSessionManager(this);

        binding.btnBack.setOnClickListener(v -> finish());

        // Thêm các tab vào TabLayout
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Chờ thanh toán"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Chờ lấy hàng"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Chờ giao hàng"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Đã hoàn thành"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Đã hủy"));

        // Mặc định hiển thị tab "Chờ lấy hàng"
        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(1));
        loadFragment(new FragmentOrderWaitingPickup());

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment = null;
                switch (tab.getPosition()) {
                    case 0:
                        // TODO: Tạo FragmentPending nếu có
                        break;
                    case 1:
                        fragment = new FragmentOrderWaitingPickup();
                        break;
                    case 2:
                        // TODO: Tạo FragmentShipping nếu có
                        break;
                    case 3:
                        // TODO: Tạo FragmentFinished nếu có
                        break;
                    case 4:
                        // TODO: Tạo FragmentCancelled nếu có
                        break;
                }
                if (fragment != null) {
                    loadFragment(fragment);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    // Adapter hiển thị danh sách đơn hàng
    public static class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
        private List<Order> orders;
        private Map<Integer, List<OrderItem>> orderItemsMap;
        public OrderAdapter(List<Order> orders, Map<Integer, List<OrderItem>> orderItemsMap) {
            this.orders = orders;
            this.orderItemsMap = orderItemsMap;
        }
        @Override
        public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_being_prepared, parent, false);
            return new OrderViewHolder(view);
        }
        @Override
        public void onBindViewHolder(OrderViewHolder holder, int position) {
            Order order = orders.get(position);
            List<OrderItem> items = orderItemsMap.get(order.orderId);
            holder.bind(order, items);
        }
        @Override
        public int getItemCount() { return orders.size(); }

        public static class OrderViewHolder extends RecyclerView.ViewHolder {
            private RecyclerView rvOrderItems;
            private View btnViewMore;
            private boolean expanded = false;
            public OrderViewHolder(View itemView) {
                super(itemView);
                rvOrderItems = itemView.findViewById(R.id.rvOrderItems);
                btnViewMore = itemView.findViewById(R.id.btn_view_more);
            }
            public void bind(Order order, List<OrderItem> items) {
                if (rvOrderItems.getLayoutManager() == null) {
                    rvOrderItems.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
                }
                int showCount = expanded ? items.size() : Math.min(1, items.size());
                List<OrderItem> showItems = items.subList(0, showCount);
                rvOrderItems.setAdapter(new OrderItemAdapter(showItems));
                btnViewMore.setVisibility(items.size() > 1 && !expanded ? View.VISIBLE : View.GONE);
                btnViewMore.setOnClickListener(v -> {
                    expanded = true;
                    bind(order, items);
                });
            }
        }
    }

    // Adapter con cho OrderItem
    public static class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {
        private List<OrderItem> items;
        public OrderItemAdapter(List<OrderItem> items) { this.items = items; }
        @Override
        public OrderItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_payment, parent, false);
            return new OrderItemViewHolder(view);
        }
        @Override
        public void onBindViewHolder(OrderItemViewHolder holder, int position) {
            holder.bind(items.get(position));
        }
        @Override
        public int getItemCount() { return items.size(); }
        public static class OrderItemViewHolder extends RecyclerView.ViewHolder {
            public OrderItemViewHolder(View itemView) { super(itemView); }
            public void bind(OrderItem orderItem) {
                String variantIdStr = String.valueOf(orderItem.variantId);
                MyDatabase db = new MyDatabase(itemView.getContext());
                MyDatabase.ProductVariant variant = db.getProductVariantById(variantIdStr);
                MyDatabase.Product product = db.getProductByVariantId(variantIdStr);
                double[] prices = db.getProductPriceByVariantId(variantIdStr);
                double originalPrice = prices[0];
                double discountedPrice = prices[1];
                TextView tvProductName = itemView.findViewById(R.id.tvProductName);
                TextView tvColor = itemView.findViewById(R.id.tvColor);
                TextView tvNewPrice = itemView.findViewById(R.id.tvNewPrice);
                TextView tvOldPrice = itemView.findViewById(R.id.tvOldPrice);
                TextView tvQuantity = itemView.findViewById(R.id.tvQuantity);
                ImageView imgProduct = itemView.findViewById(R.id.imgProduct);
                if (variant != null && variant.thumbnail_url != null && variant.thumbnail_url.length > 0) {
                    imgProduct.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(variant.thumbnail_url, 0, variant.thumbnail_url.length));
                } else if (product != null && product.thumbnail_url != null && product.thumbnail_url.length > 0) {
                    imgProduct.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(product.thumbnail_url, 0, product.thumbnail_url.length));
                } else {
                    imgProduct.setImageResource(R.drawable.ic_launcher_background);
                }
                tvProductName.setText(product != null ? product.name : "");
                tvColor.setText(variant != null ? variant.variant_name : "");
                tvNewPrice.setText(String.format("%,.0f₫", discountedPrice));
                tvOldPrice.setText(String.format("%,.0f₫", originalPrice));
                tvOldPrice.setPaintFlags(tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvQuantity.setText("x" + orderItem.quantity);
            }
        }
    }
}