package com.nhom11.innowave;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.Toast;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nhom11.innowave.databinding.ActivityAddAddressBinding;
import com.nhom11.models.Address;
import java.util.Arrays;
import java.util.List;

public class AddAddressActivity extends AppCompatActivity {
    ActivityAddAddressBinding binding;
    private MyDatabase db;
    private UserSessionManager sessionManager;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        // Initialize ViewBinding
        binding = ActivityAddAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db = new MyDatabase(this);
        sessionManager = new UserSessionManager(this);
        userId = sessionManager.getUserId();
        setupSpinners();
        binding.btnCreateAddress.setOnClickListener(v -> createAddress());
        binding.icBack.setOnClickListener(v -> finish());
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
    }

    private void createAddress() {
        String recipientName = binding.edtReceiverName.getText().toString().trim();
        String phone = binding.edtPhone.getText().toString().trim();
        String street = binding.detailAdd.getText().toString().trim();
        String province = binding.province.getSelectedItem().toString();
        String district = binding.district.getSelectedItem().toString();
        String ward = binding.ward.getSelectedItem().toString();
        boolean hasAddress = db.userHasAddress(userId);
        int isDefault = 0;
        if (!hasAddress) {
            isDefault = 1;
        } else if (binding.switchDefault.isChecked()) {
            isDefault = 1;
        }
        if (recipientName.isEmpty() || phone.isEmpty() || street.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }
        Address address = new Address(0, userId, recipientName, phone, street, ward, district, province, isDefault, null, null);
        long newId = db.insertAddress(address);
        if (isDefault == 1 && hasAddress) {
            db.setDefaultAddress(userId, (int)newId);
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