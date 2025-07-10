package com.nhom11.innowave;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.nhom11.innowave.databinding.ActivityEditAddressBinding;
import com.nhom11.models.Address;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import java.util.Arrays;
import java.util.List;

public class EditAddressActivity extends AppCompatActivity {
    private ActivityEditAddressBinding binding;
    private MyDatabase db;
    private int addressId;
    private Address address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEditAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db = new MyDatabase(this);
        addressId = getIntent().getIntExtra("address_id", -1);
        if (addressId != -1) {
            address = db.getAddressById(addressId);
            if (address != null) {
                loadAddressToForm(address);
            }
        }
        setupSpinners();
        binding.icBack.setOnClickListener(v -> finish());
        binding.btnEditAddress.setOnClickListener(v -> saveEdit());
    }

    private void loadAddressToForm(Address address) {
        binding.edtReceiverName.setText(address.getRecipientName());
        binding.edtPhone.setText(address.getPhoneNumber());
        binding.detailAdd.setText(address.getStreetAddress());
        binding.switchDefault.setChecked(address.isDefault() == 1);
        // Spinner sẽ được set sau khi setupSpinners
    }

    private void setupSpinners() {
        List<String> provinces = Arrays.asList("Bình Định", "Hà Nội", "TP. Hồ Chí Minh");
        List<String> districts = Arrays.asList("Thị xã An Nhơn", "Quận 1", "Quận 2");
        List<String> wards = Arrays.asList("Nhơn Thành", "Phường 1", "Phường 2");
        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, provinces);
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.province.setAdapter(provinceAdapter);
        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, districts);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.district.setAdapter(districtAdapter);
        ArrayAdapter<String> wardAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wards);
        wardAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.ward.setAdapter(wardAdapter);
        // Set selection theo dữ liệu cũ
        if (address != null) {
            if (address.getProvince() != null) {
                int idx = provinces.indexOf(address.getProvince());
                if (idx >= 0) binding.province.setSelection(idx);
            }
            if (address.getDistrict() != null) {
                int idx = districts.indexOf(address.getDistrict());
                if (idx >= 0) binding.district.setSelection(idx);
            }
            if (address.getWard() != null) {
                int idx = wards.indexOf(address.getWard());
                if (idx >= 0) binding.ward.setSelection(idx);
            }
        }
    }

    private void saveEdit() {
        String recipientName = binding.edtReceiverName.getText().toString().trim();
        String phone = binding.edtPhone.getText().toString().trim();
        String street = binding.detailAdd.getText().toString().trim();
        String province = binding.province.getSelectedItem().toString();
        String district = binding.district.getSelectedItem().toString();
        String ward = binding.ward.getSelectedItem().toString();
        if (recipientName.isEmpty() || phone.isEmpty() || street.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }
        address.setRecipientName(recipientName);
        address.setPhoneNumber(phone);
        address.setStreetAddress(street);
        address.setProvince(province);
        address.setDistrict(district);
        address.setWard(ward);
        db.updateAddress(address);
        // Nếu người dùng bật switch_default thì set địa chỉ này thành mặc định
        if (binding.switchDefault.isChecked() && address.isDefault() != 1) {
            db.setDefaultAddress(address.getUserId(), address.getAddressId());
        }
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}