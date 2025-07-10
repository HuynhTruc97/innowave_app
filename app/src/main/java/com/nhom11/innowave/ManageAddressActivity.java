package com.nhom11.innowave;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.nhom11.adapters.AddressAdapter;
import com.nhom11.models.Address;
import java.util.List;
import android.content.Intent;
import com.nhom11.innowave.databinding.ActivityManageAddressBinding;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.app.AlertDialog;
import androidx.annotation.NonNull;
import com.nhom11.innowave.UserSessionManager;

public class ManageAddressActivity extends AppCompatActivity {
    private ActivityManageAddressBinding binding;
    private AddressAdapter addressAdapter;
    private List<Address> addressList;
    private MyDatabase db;
    private UserSessionManager sessionManager;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityManageAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db = new MyDatabase(this);
        sessionManager = new UserSessionManager(this);
        userId = sessionManager.getUserId();
        binding.rvAddressList.setLayoutManager(new LinearLayoutManager(this));
        loadAddresses();
        binding.btnAddAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddAddressActivity.class);
            startActivityForResult(intent, 100);
        });
        binding.icBack.setOnClickListener(v -> finish());
    }

    private void loadAddresses() {
        addressList = db.getAddressesByUserId(userId);
        addressAdapter = new AddressAdapter(addressList, new AddressAdapter.OnAddressActionListener() {
            @Override
            public void onEdit(Address address) {
                Intent intent = new Intent(ManageAddressActivity.this, EditAddressActivity.class);
                intent.putExtra("address_id", address.getAddressId());
                startActivityForResult(intent, 101);
            }
            @Override
            public void onDelete(Address address) {
                new AlertDialog.Builder(ManageAddressActivity.this)
                    .setTitle("Xóa địa chỉ")
                    .setMessage("Bạn có chắc chắn muốn xóa địa chỉ này không?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        db.deleteAddress(address.getAddressId(), userId);
                        loadAddresses();
                    })
                    .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                    .show();
            }
        });
        binding.rvAddressList.setAdapter(addressAdapter);
        attachSwipeToDelete();
    }

    private void attachSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Address address = addressAdapter.getAddressAt(position);
                addressAdapter.notifyItemChanged(position); // Để giữ lại item nếu user cancel
                new AlertDialog.Builder(ManageAddressActivity.this)
                    .setTitle("Xóa địa chỉ")
                    .setMessage("Bạn có chắc chắn muốn xóa địa chỉ này không?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        db.deleteAddress(address.getAddressId(), userId);
                        loadAddresses();
                    })
                    .setNegativeButton("Không", (dialog, which) -> {
                        dialog.dismiss();
                        addressAdapter.notifyItemChanged(position);
                    })
                    .show();
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(binding.rvAddressList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 100 || requestCode == 101) && resultCode == RESULT_OK) {
            loadAddresses();
        }
    }
}