package com.nhom11.innowave;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.nhom11.innowave.databinding.FragmentCartBinding;
import com.nhom11.adapters.CartItemAdapter;
import java.util.List;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ListView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.AdapterView;
import android.view.MotionEvent;
import android.view.GestureDetector;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import java.util.Set;
import java.util.ArrayList;
import java.io.Serializable;
import com.nhom11.innowave.CartItemLite;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentCart#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentCart extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentCartBinding binding;
    private UserSessionManager sessionManager;
    private MyDatabase myDatabase;
    private BroadcastReceiver cartUpdateReceiver;
    private CartItemAdapter cartAdapter;
    private List<MyDatabase.CartItem> currentCartItems;

    public FragmentCart() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentCart.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentCart newInstance(String param1, String param2) {
        FragmentCart fragment = new FragmentCart();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new UserSessionManager(requireContext());
        myDatabase = new MyDatabase(requireContext());
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment using ViewBinding
        binding = FragmentCartBinding.inflate(inflater, container, false);
        
        // Kiểm tra user đã đăng nhập chưa
        if (sessionManager.isLoggedIn()) {
            int userId = sessionManager.getUserId();
            android.util.Log.d("FragmentCart", "User logged in with ID: " + userId);
            
            // Load cart items for this user
            loadCartItems(userId);
            
        } else {
            android.util.Log.d("FragmentCart", "User not logged in");
            // Hiển thị thông báo yêu cầu đăng nhập
            showLoginRequiredMessage();
        }
        
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cartUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (sessionManager.isLoggedIn()) {
                    int userId = sessionManager.getUserId();
                    loadCartItems(userId);
                }
            }
        };
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(cartUpdateReceiver, new IntentFilter("ACTION_CART_UPDATED"));

        // Thêm logic cho nút Tiếp tục
        if (binding.btnContinue != null) {
            binding.btnContinue.setOnClickListener(v -> {
                if (cartAdapter == null || currentCartItems == null) return;
                Set<Integer> checkedIds = cartAdapter.getCheckedCartItemIds();
                if (checkedIds.isEmpty()) {
                    Toast.makeText(requireContext(), "Vui lòng chọn sản phẩm để thanh toán!", Toast.LENGTH_SHORT).show();
                    return;
                }
                ArrayList<CartItemLite> selectedItems = new ArrayList<>();
                for (MyDatabase.CartItem item : currentCartItems) {
                    if (checkedIds.contains(item.cart_item_id)) {
                        selectedItems.add(new CartItemLite(
                            item.cart_item_id, // truyền cart_item_id
                            item.product_id,
                            item.variant_id,
                            item.quantity,
                            item.product_name,
                            item.variant_name,
                            item.price
                        ));
                    }
                }
                Intent intent = new Intent(requireContext(), PaymentActivity.class);
                intent.putExtra("selected_cart_items", selectedItems);
                startActivity(intent);
            });
        }
        // Thêm logic cho nút Mua ngay (Shop Now) khi giỏ hàng trống
        if (binding.btnShopNow != null) {
            binding.btnShopNow.setOnClickListener(v -> {
                // Sử dụng NavController để chuyển sang fragment_product_page
                androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(requireActivity(), R.id.container);
                navController.navigate(R.id.fragment_product_page);
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cartUpdateReceiver != null) {
            LocalBroadcastManager.getInstance(requireContext())
                .unregisterReceiver(cartUpdateReceiver);
        }
        binding = null;
    }

    private void loadCartItems(int userId) {
        try {
            long cartId = myDatabase.getCartIdByUserId(userId);
            android.util.Log.d("FragmentCart", "Loading cart for userId=" + userId + ", cartId=" + cartId);
            if (cartId == -1) {
                showEmptyCartMessage();
                return;
            }
            List<MyDatabase.CartItem> cartItems = myDatabase.getCartItemsByCartId(cartId);
            android.util.Log.d("FragmentCart", "cartItems.size=" + cartItems.size());
            if (cartItems.isEmpty()) {
                showEmptyCartMessage();
            } else {
                showCartItems(cartItems);
            }
        } catch (Exception e) {
            android.util.Log.e("FragmentCart", "Error loading cart items: " + e.getMessage());
            showEmptyCartMessage();
        }
    }

    private void showCartItems(List<MyDatabase.CartItem> cartItems) {
        if (binding.layoutCartContent != null) {
            binding.layoutCartContent.setVisibility(View.VISIBLE);
        }
        if (binding.layoutEmptyCart != null) {
            binding.layoutEmptyCart.setVisibility(View.GONE);
        }
        if (binding.rvCartProducts != null) {
            // Sắp xếp giỏ hàng mới nhất lên đầu (giả sử cart_item_id tăng dần theo thời gian)
            java.util.Collections.sort(cartItems, (a, b) -> Integer.compare(b.cart_item_id, a.cart_item_id));
            currentCartItems = cartItems;
            Set<Integer> checkedIds = null;
            if (cartAdapter != null) {
                checkedIds = cartAdapter.getCheckedCartItemIds();
            }
            cartAdapter = new com.nhom11.adapters.CartItemAdapter(requireContext(), cartItems, checkedIds);
            cartAdapter.setOnCartItemCheckedChangeListener(checkedPositions -> updateTotal());
            binding.rvCartProducts.setAdapter(cartAdapter);
            binding.rvCartProducts.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
            setupSwipeToDelete();
        }
        updateTotal();
    }

    private void updateTotal() {
        if (cartAdapter == null || currentCartItems == null) return;
        double total = 0;
        Set<Integer> checkedIds = cartAdapter.getCheckedCartItemIds();
        for (MyDatabase.CartItem item : currentCartItems) {
            if (checkedIds.contains(item.cart_item_id)) {
                total += item.quantity * item.price;
            }
        }
        if (binding.tvTotal != null) {
            binding.tvTotal.setText(String.format("%,.0fđ", total));
        }
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && currentCartItems != null && position < currentCartItems.size()) {
                    MyDatabase.CartItem item = currentCartItems.get(position);
                    new AlertDialog.Builder(requireContext())
                        .setTitle("Xác nhận")
                        .setMessage("Bạn có muốn xóa sản phẩm này ra khỏi giỏ hàng không?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            MyDatabase db = new MyDatabase(requireContext());
                            db.deleteCartItem(item.cart_item_id);
                            db.close();
                            Toast.makeText(requireContext(), "Đã xóa sản phẩm khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                            android.content.Intent intent = new android.content.Intent("ACTION_CART_UPDATED");
                            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);
                        })
                        .setNegativeButton("Không", (dialog, which) -> {
                            cartAdapter.notifyItemChanged(position);
                        })
                        .setOnCancelListener(dialog -> cartAdapter.notifyItemChanged(position))
                        .show();
                }
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(binding.rvCartProducts);
    }

    private void showEmptyCartMessage() {
        // Hiển thị layout trống, ẩn layout có sản phẩm
        if (binding.layoutEmptyCart != null) {
            binding.layoutEmptyCart.setVisibility(View.VISIBLE);
        }
        if (binding.layoutCartContent != null) {
            binding.layoutCartContent.setVisibility(View.GONE);
        }
    }

    private void showLoginRequiredMessage() {
        // Hiển thị layout giỏ hàng trống với thông báo đăng nhập
        if (binding.layoutEmptyCart != null) {
            binding.layoutEmptyCart.setVisibility(View.VISIBLE);
            // Có thể thêm logic để thay đổi text trong layout này
        }
        
        // Ẩn layout giỏ hàng có sản phẩm
        if (binding.layoutCartContent != null) {
            binding.layoutCartContent.setVisibility(View.GONE);
        }
    }
}