package com.nhom11.innowave;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.nhom11.adapters.AddressAdapter;
import com.nhom11.models.Address;
import com.nhom11.innowave.databinding.ActivitySelectAddressBinding;
import java.util.List;

public class SelectAddressActivity extends AppCompatActivity {
    private ActivitySelectAddressBinding binding;
    private AddressAdapter addressAdapter;
    private List<Address> addressList;
    private MyDatabase db;
    private UserSessionManager sessionManager;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySelectAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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
                Intent intent = new Intent(SelectAddressActivity.this, EditAddressActivity.class);
                intent.putExtra("address_id", address.getAddressId());
                startActivityForResult(intent, 101);
            }
            @Override
            public void onDelete(Address address) {
                // Không cho xóa trong màn chọn địa chỉ
            }
            @Override
            public void onSelect(Address address) {
                Intent result = new Intent();
                result.putExtra("selected_address_id", address.getAddressId());
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        }, true); // true: enable chọn địa chỉ
        binding.rvAddressList.setAdapter(addressAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 100 || requestCode == 101) && resultCode == RESULT_OK) {
            loadAddresses();
        }
    }
} 