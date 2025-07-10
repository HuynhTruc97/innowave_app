package com.nhom11.innowave;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.nhom11.innowave.databinding.ActivityPaymentBinding;
import com.nhom11.models.Address;
import java.util.ArrayList;
import java.util.List;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.nhom11.adapters.ProductPaymentAdapter;
import com.nhom11.models.Product;
import android.widget.ListView;
import android.widget.ListAdapter;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.nhom11.models.PaymentItem;
import android.util.Log;
import java.io.Serializable;
import com.nhom11.innowave.CartItemLite;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PaymentActivity extends AppCompatActivity {
    private ActivityPaymentBinding binding;
    private UserSessionManager sessionManager;
    private MyDatabase db;
    private Address defaultAddress;
    private List<MyDatabase.ProductVariant> productVariants;
    private List<Integer> quantities;
    private double totalProductPrice = 0;
    private double shippingFee = 0;
    private double shippingDiscount = 0;
    private double voucherDiscount = 0;
    private double endPrice = 0;
    private double savingAmount = 0;
    private boolean isBuyNow = false;
    private List<Product> products;
    private List<PaymentItem> paymentItems;
    private ActivityResultLauncher<Intent> addAddressLauncher;
    private static final int REQUEST_SELECT_ADDRESS = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = new UserSessionManager(this);
        db = new MyDatabase(this);
        int userId = sessionManager.getUserId();

        binding.btnBack.setOnClickListener(v -> finish());

        // Nhận dữ liệu intent
        ArrayList<CartItemLite> selectedItems = (ArrayList<CartItemLite>) getIntent().getSerializableExtra("selected_cart_items");
        paymentItems = new ArrayList<>();
        if (selectedItems != null) {
            for (CartItemLite item : selectedItems) {
                // Truy vấn lại thông tin biến thể và sản phẩm từ database
                MyDatabase.ProductVariant variant = db.getProductVariantById(item.variantId);
                MyDatabase.Product product = db.getProductByVariantId(item.variantId);
                byte[] thumbnail = (variant != null && variant.thumbnail_url != null && variant.thumbnail_url.length > 0)
                    ? variant.thumbnail_url
                    : (product != null ? product.thumbnail_url : null);
                double originalPrice = (product != null) ? product.original_price : item.price;
                double discountedPrice = (product != null) ? product.discounted_price : item.price;
                paymentItems.add(new PaymentItem(
                    item.productName,
                    item.variantName,
                    item.quantity,
                    discountedPrice,
                    originalPrice,
                    thumbnail,
                    item.variantId
                ));
            }
        }
        addAddressLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadDefaultAddress(userId);
                }
            }
        );

        // Khi nhấn thêm địa chỉ (chưa có địa chỉ)
        binding.receivingAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddAddressActivity.class);
            addAddressLauncher.launch(intent);
        });
        // Khi nhấn đổi địa chỉ (nếu có)
        binding.btnChangeAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectAddressActivity.class);
            startActivityForResult(intent, REQUEST_SELECT_ADDRESS);
        });

        loadDefaultAddress(userId);
        loadProductList();
        calculateTotal();
        setupOrderButton(userId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_ADDRESS && resultCode == RESULT_OK && data != null) {
            int selectedAddressId = data.getIntExtra("selected_address_id", -1);
            if (selectedAddressId != -1) {
                Address selected = db.getAddressById(selectedAddressId);
                if (selected != null) {
                    defaultAddress = selected;
                    binding.tvRecipientName.setText(selected.getRecipientName());
                    binding.tvPhoneNumber.setText(selected.getPhoneNumber());
                    binding.tvStreetAddress.setText(selected.getStreetAddress());
                    String wardDistrictProvince = selected.getWard() + ", " + selected.getDistrict() + ", " + selected.getProvince();
                    binding.tvWardDistrictProvince.setText(wardDistrictProvince);
                    binding.receivingAddress.setVisibility(View.GONE);
                    binding.layoutAddress.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void loadDefaultAddress(int userId) {
        List<Address> addresses = db.getAddressesByUserId(userId);
        defaultAddress = null;
        for (Address addr : addresses) {
            if (addr.isDefault() == 1) {
                defaultAddress = addr;
                break;
            }
        }
        if (defaultAddress != null) {
            binding.tvRecipientName.setText(defaultAddress.getRecipientName());
            binding.tvPhoneNumber.setText(defaultAddress.getPhoneNumber());
            binding.tvStreetAddress.setText(defaultAddress.getStreetAddress());
            String wardDistrictProvince = defaultAddress.getWard() + ", " + defaultAddress.getDistrict() + ", " + defaultAddress.getProvince();
            binding.tvWardDistrictProvince.setText(wardDistrictProvince);
            binding.receivingAddress.setVisibility(View.GONE);
            binding.layoutAddress.setVisibility(View.VISIBLE);
        } else {
            binding.layoutAddress.setVisibility(View.GONE);
            binding.receivingAddress.setVisibility(View.VISIBLE);
        }
    }

    private void loadProductList() {
        RecyclerView recyclerView = binding.rvProductPayment;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ProductPaymentRecyclerAdapter(paymentItems));
        // Giảm khoảng cách giữa các item
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(android.graphics.Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.top = 0;
                outRect.bottom = 8; // khoảng cách nhỏ giữa các item
            }
        });
    }

    // Adapter cho RecyclerView hiển thị sản phẩm thanh toán
    public static class ProductPaymentRecyclerAdapter extends RecyclerView.Adapter<ProductPaymentRecyclerAdapter.ViewHolder> {
        private final List<PaymentItem> items;
        public ProductPaymentRecyclerAdapter(List<PaymentItem> items) { this.items = items; }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_payment, parent, false);
            return new ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PaymentItem item = items.get(position);
            holder.bind(item);
        }
        @Override
        public int getItemCount() { return items.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(View itemView) { super(itemView); }
            public void bind(PaymentItem item) {
                TextView tvProductName = itemView.findViewById(R.id.tvProductName);
                TextView tvColor = itemView.findViewById(R.id.tvColor);
                TextView tvNewPrice = itemView.findViewById(R.id.tvNewPrice);
                TextView tvOldPrice = itemView.findViewById(R.id.tvOldPrice);
                TextView tvQuantity = itemView.findViewById(R.id.tvQuantity);
                android.widget.ImageView imgProduct = itemView.findViewById(R.id.imgProduct);
                tvProductName.setText(item.productName);
                tvColor.setText(item.variantName);
                tvNewPrice.setText(String.format("%,.0f₫", item.price));
                tvOldPrice.setText(String.format("%,.0f₫", item.originalPrice));
                tvOldPrice.setPaintFlags(tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvQuantity.setText("x" + item.quantity);
                if (item.thumbnailUrl != null && item.thumbnailUrl.length > 0) {
                    imgProduct.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(item.thumbnailUrl, 0, item.thumbnailUrl.length));
                } else {
                    imgProduct.setImageResource(R.drawable.ic_launcher_background);
                }
            }
        }
    }

    // Tự động set chiều cao cho ListView để hiển thị hết tất cả item
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) return;
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(
                View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private void calculateTotal() {
        totalProductPrice = 0;
        double totalOriginalPrice = 0;
        for (int i = 0; i < paymentItems.size(); i++) {
            PaymentItem item = paymentItems.get(i);
            int qty = item.quantity;
            totalProductPrice += item.price * qty;
            totalOriginalPrice += item.originalPrice * qty;
        }
        // Tính phí vận chuyển
        if (totalProductPrice >= 500000) {
            shippingFee = 0;
            binding.tvDiscountedShippingFee.setText(formatMoney(35000)); // Được giảm 35.000
        } else {
            shippingFee = 35000;
            binding.tvDiscountedShippingFee.setText(formatMoney(0)); // Không được giảm
        }
        voucherDiscount = 0; // Nếu có voucher thì trừ ở đây

        endPrice = totalProductPrice + shippingFee - voucherDiscount;

        // Set lên view
        binding.tvFirstProductPrice.setText(formatMoney(totalProductPrice));
        binding.tvProductPrice.setText(formatMoney(totalProductPrice));
        binding.tvDeliveryCostBefore.setText(formatMoney(35000));
        if (shippingFee == 0) {
            binding.tvDeliveryCostBefore.setPaintFlags(binding.tvDeliveryCostBefore.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            binding.tvDeliveryCostAfter.setText("0");
        } else {
            binding.tvDeliveryCostAfter.setText(formatMoney(35000));
            binding.tvDeliveryCostBefore.setVisibility(View.GONE);
        }
        binding.tvVoucherDiscountAmount.setText("0");
        binding.tvEndPrice1.setText(formatMoney(endPrice));
        binding.tvEndPrice2.setText(formatMoney(endPrice));
        // Số tiền tiết kiệm = (tổng giá gốc - tổng giá đã giảm) + (nếu được miễn phí vận chuyển thì cộng 35000) + voucher
        double savingAmount = (totalOriginalPrice - totalProductPrice) + (shippingFee == 0 ? 35000 : 0) + voucherDiscount;
        binding.tvSavingAmount.setText(formatMoney(savingAmount));
    }

    private void setupOrderButton(int userId) {
        binding.btnOrder.setOnClickListener(v -> {
            if (defaultAddress == null) {
                Toast.makeText(this, "Vui lòng thêm địa chỉ nhận hàng!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Lưu đơn hàng
            double totalPrice = endPrice;
            double shipping = shippingFee;
            long orderId = db.insertOrder(userId, defaultAddress.getAddressId(), totalPrice, shipping, "waiting_for_pickup");
            boolean allOrderItemsInserted = true;
            if (orderId > 0) {
                for (int i = 0; i < paymentItems.size(); i++) {
                    PaymentItem item = paymentItems.get(i);
                    int qty = item.quantity;
                    double discountedPrice = item.price;
                    try {
                        db.insertOrderItem(orderId, String.valueOf(item.variantId), qty, discountedPrice, discountedPrice * qty);
                    } catch (Exception e) {
                        allOrderItemsInserted = false;
                        break;
                    }
                }
            } else {
                allOrderItemsInserted = false;
            }
            if (orderId > 0 && allOrderItemsInserted) {
                // Xóa các cart_item_id nếu có (chỉ xóa nếu cart_item_id > 0)
                ArrayList<CartItemLite> selectedItems = (ArrayList<CartItemLite>) getIntent().getSerializableExtra("selected_cart_items");
                if (selectedItems != null) {
                    for (CartItemLite item : selectedItems) {
                        if (item.cartItemId > 0) {
                            db.deleteCartItem(item.cartItemId);
                        }
                    }
                    // Gửi broadcast cập nhật giỏ hàng
                    android.content.Intent intent = new android.content.Intent("ACTION_CART_UPDATED");
                    androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                }
                Log.d("OrderDebug", "Lưu đơn hàng và order_item thành công, đã xóa các item trong cart!");
                Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                showOrderSuccessDialog();
            } else {
                Log.e("OrderDebug", "Lỗi khi lưu đơn hàng hoặc order_item!");
                Toast.makeText(this, "Có lỗi khi đặt hàng, vui lòng thử lại!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showOrderSuccessDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_order_success);
        dialog.setCancelable(false);
        // Set dialog width = MATCH_PARENT, height = WRAP_CONTENT
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
        Button btnBackHome = dialog.findViewById(R.id.btnBackHome);
        Button btnViewOrder = dialog.findViewById(R.id.btnViewOrder);
        btnBackHome.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        btnViewOrder.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(this, ManageOrderActivity.class);
            startActivity(intent);
            finish();
        });
        dialog.show();
    }

    private String formatMoney(double amount) {
        return String.format("%,.0f₫", amount);
    }

    // Lấy địa chỉ mặc định từ DB (giả lập)
    private Address getDefaultAddressFromDB() {
        // TODO: Thay bằng truy vấn thực tế từ DB
        // Trả về null nếu chưa có địa chỉ
        // Ví dụ trả về địa chỉ mẫu:
        // return new Address(1, 1, "Nguyễn Văn A", "0123456789", "123 Đường ABC", "Phường 1", "Quận 1", "TP.HCM", 1, "", "");
        return null;
    }

    // Lấy danh sách sản phẩm cần thanh toán (giả lập)
    private List<Product> getProductsForPayment() {
        // TODO: Lấy từ giỏ hàng hoặc sản phẩm mua ngay
        List<Product> products = new ArrayList<>();
        // Ví dụ dữ liệu mẫu:
        // products.add(new Product("1", "Sản phẩm A", 1, "1", null, "Mô tả", 100000, 80000, 1, "", ""));
        // products.add(new Product("2", "Sản phẩm B", 1, "1", null, "Mô tả", 200000, 150000, 1, "", ""));
        return products;
    }

    // Lấy số lượng tương ứng với sản phẩm (giả lập)
    private List<Integer> getQuantitiesForPayment() {
        // TODO: Lấy từ giỏ hàng hoặc sản phẩm mua ngay
        List<Integer> quantities = new ArrayList<>();
        // Ví dụ dữ liệu mẫu:
        // quantities.add(2);
        // quantities.add(1);
        return quantities;
    }

    // Lưu đơn hàng vào DB (giả lập)
    private long saveOrderToDB(Address address, List<Product> products, List<Integer> quantities, double totalPrice) {
        // TODO: Lưu vào DB thực tế, trả về orderId
        // Ví dụ trả về id mẫu:
        return System.currentTimeMillis();
    }

    // Hiển thị dialog đặt hàng thành công
    private void showOrderSuccessDialog(long orderId) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_order_success);
        dialog.setCancelable(false);
        dialog.findViewById(R.id.btnBackHome).setOnClickListener(v -> {
            dialog.dismiss();
            // Quay về trang chủ
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        dialog.findViewById(R.id.btnViewOrder).setOnClickListener(v -> {
            dialog.dismiss();
            // Mở activity_manage_order
            Intent intent = new Intent(this, ManageOrderActivity.class);
            intent.putExtra("orderId", orderId);
            startActivity(intent);
            finish();
        });
        dialog.show();
    }
}