package com.nhom11.innowave;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nhom11.innowave.databinding.ActivitySignInBinding;
import com.nhom11.models.User;

public class SignInActivity extends AppCompatActivity {
    private ActivitySignInBinding binding;
    private MyDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        // Initialize ViewBinding
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new MyDatabase(this);
        
        // Back button
        binding.btnBack.setOnClickListener(v -> finish());

        // Setup phone number validation
        binding.edtPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String phone = s.toString();
                boolean isValid = phone.length() == 10 && phone.matches("0[0-9]{9}");
                binding.btnContinue.setEnabled(isValid);
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Continue button click
        binding.btnContinue.setOnClickListener(v -> {
            String phone = binding.edtPhone.getText().toString();
            if (phone.length() != 10 || !phone.matches("0[0-9]{9}")) {
                Toast.makeText(this, "Số điện thoại không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Kiểm tra số điện thoại có tồn tại không
            User user = db.getUserByPhone(phone);
            if (user == null) {
                // Tạo user mới với số điện thoại này
                long newUserId = db.createUserWithPhone(phone);
                if (newUserId == -1) {
                    showErrorDialog("Không thể tạo tài khoản! Vui lòng thử lại.");
                    return;
                }
            }
            
            // Hiển thị dialog xác nhận điều khoản
            showConfirmDialog(phone);
        });

        // Login by account button
        binding.btnLogInByAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignInByAccountActivity.class);
            startActivity(intent);
        });
    }

    private void showErrorDialog(String message) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Lỗi")
               .setMessage(message)
               .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
               .show();
    }

    private void showConfirmDialog(String phone) {
        
        // Tạo Dialog sử dụng layout dialog_confirm.xml
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_confirm);
        
        // Thiết lập dialog style
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        
        // Lấy các view từ layout
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        CheckBox cbConfirm = dialog.findViewById(R.id.cbConfirm);
        
        // Ban đầu nút confirm bị disable
        btnConfirm.setEnabled(false);
        
        // Khi checkbox được check thì enable nút confirm
        cbConfirm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnConfirm.setEnabled(isChecked);
        });
        
        // Xử lý khi bấm confirm
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(SignInActivity.this, OTPActivity.class);
            intent.putExtra("phone", phone);
            startActivity(intent);
            finish();
        });
        
        // Xử lý nút close
        dialog.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        
        // Hiển thị dialog từ dưới lên
        try {
            // Thiết lập kích thước dialog
            android.view.Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
                               android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                // Thiết lập dialog hiển thị từ dưới lên
                android.view.WindowManager.LayoutParams params = window.getAttributes();
                params.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
                params.gravity = android.view.Gravity.BOTTOM; // Hiển thị từ dưới lên
                window.setAttributes(params);
            }
            dialog.show();
        } catch (Exception e) {
            // Fallback nếu dialog không hiển thị được
            Toast.makeText(this, "Lỗi hiển thị dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SignInActivity.this, OTPActivity.class);
            intent.putExtra("phone", phone);
            startActivity(intent);
            finish();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}