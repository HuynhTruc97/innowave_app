package com.nhom11.innowave;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.widget.Toast;
import com.nhom11.innowave.databinding.ActivitySignUpBinding;
import android.graphics.Color;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.CompoundButton;
import androidx.core.content.ContextCompat;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private MyDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = new MyDatabase(this);

        // Ẩn lỗi ban đầu
        binding.tvErrorPhone.setVisibility(View.GONE);
        binding.tvErrorEmail.setVisibility(View.GONE);
        binding.tvErrorPassword.setVisibility(View.GONE);
        binding.tvErrorRePassword.setVisibility(View.GONE);

        // Show/hide password
        binding.ivShowPassword.setOnClickListener(v -> {
            if (binding.edtPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                binding.edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                binding.edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            binding.edtPassword.setSelection(binding.edtPassword.getText().length());
        });
        binding.ivShowRePassword.setOnClickListener(v -> {
            if (binding.edtRePassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                binding.edtRePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                binding.edtRePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            binding.edtRePassword.setSelection(binding.edtRePassword.getText().length());
        });

        // Theo dõi thay đổi để kiểm tra hợp lệ và bật/tắt nút đăng ký
        View.OnFocusChangeListener validateOnFocus = (v, hasFocus) -> {
            if (!hasFocus) validateAll();
        };
        binding.edtName.setOnFocusChangeListener(validateOnFocus);
        binding.edtPhone.setOnFocusChangeListener(validateOnFocus);
        binding.edtEmail.setOnFocusChangeListener(validateOnFocus);
        binding.edtPassword.setOnFocusChangeListener(validateOnFocus);
        binding.edtRePassword.setOnFocusChangeListener(validateOnFocus);
        binding.cbAgree.setOnCheckedChangeListener((buttonView, isChecked) -> validateAll());

        // TextWatcher cho realtime validation
        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { validateAll(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        };
        binding.edtName.addTextChangedListener(watcher);
        binding.edtPhone.addTextChangedListener(watcher);
        binding.edtEmail.addTextChangedListener(watcher);
        binding.edtPassword.addTextChangedListener(watcher);
        binding.edtRePassword.addTextChangedListener(watcher);

        // Đăng ký
        binding.btnRegister.setOnClickListener(v -> {
            if (!validateAll()) return;
            String name = binding.edtName.getText().toString().trim();
            String phone = binding.edtPhone.getText().toString().trim();
            String email = binding.edtEmail.getText().toString().trim();
            String password = binding.edtPassword.getText().toString();
            String passwordHash = hashPassword(password);
            // Kiểm tra số điện thoại đã tồn tại
            if (db.getUserByPhone(phone) != null) {
                showError(binding.tvErrorPhone, "Số điện thoại đã được sử dụng");
                return;
            }
            // Tạo user mới (lưu password_hash)
            long userId = db.createUserWithPhoneAndPassword(phone, passwordHash);
            if (userId == -1) {
                Toast.makeText(this, "Đăng ký thất bại!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Chuyển sang OTPActivity
            Intent intent = new Intent(this, OTPActivity.class);
            intent.putExtra("phone", phone);
            startActivity(intent);
            finish();
        });

        binding.btnBack.setOnClickListener(v -> finish());
    }

    // Hàm kiểm tra mật khẩu mạnh
    private boolean isValidPassword(String password) {
        // Tối thiểu 8 ký tự, ít nhất 1 chữ hoa, 1 chữ thường, 1 số, 1 ký tự đặc biệt
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=\\-{}\\[\\]:;\"'<>,.?/]).{8,}$";
        return password.matches(pattern);
    }

    // Hàm kiểm tra hợp lệ và hiển thị lỗi
    private boolean validateAll() {
        boolean valid = true;
        // Tên
        if (binding.edtName.getText().toString().trim().isEmpty()) {
            showError(binding.tvErrorName, "Vui lòng nhập tên"); valid = false;
        } else hideError(binding.tvErrorName);
        // SĐT
        String phone = binding.edtPhone.getText().toString().trim();
        if (!phone.matches("^0[0-9]{9}$")) {
            showError(binding.tvErrorPhone, "Số điện thoại phải đủ 10 số và bắt đầu bằng 0"); valid = false;
        } else hideError(binding.tvErrorPhone);
        // Email
        String email = binding.edtEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(binding.tvErrorEmail, "Email không hợp lệ"); valid = false;
        } else hideError(binding.tvErrorEmail);
        // Mật khẩu
        String password = binding.edtPassword.getText().toString();
        if (!isValidPassword(password)) {
            showError(binding.tvErrorPassword, "Mật khẩu phải từ 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt"); valid = false;
        } else hideError(binding.tvErrorPassword);
        // Nhập lại mật khẩu
        String rePassword = binding.edtRePassword.getText().toString();
        if (!rePassword.equals(password)) {
            showError(binding.tvErrorRePassword, "Mật khẩu nhập lại không khớp"); valid = false;
        } else hideError(binding.tvErrorRePassword);
        // Đồng ý điều khoản
        if (!binding.cbAgree.isChecked()) valid = false;
        // Bật/tắt nút đăng ký
        binding.btnRegister.setEnabled(valid);
        if (valid) {
            binding.btnRegister.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_color));
            binding.btnRegister.setTextColor(Color.WHITE);
        } else {
            binding.btnRegister.setBackgroundColor(ContextCompat.getColor(this, R.color.l_grey_color));
            binding.btnRegister.setTextColor(ContextCompat.getColor(this, R.color.grey_color));
        }
        return valid;
    }
    private void showError(android.widget.TextView tv, String msg) {
        tv.setText(msg);
        tv.setTextColor(ContextCompat.getColor(this, R.color.error_color));
        tv.setVisibility(View.VISIBLE);
    }
    private void hideError(android.widget.TextView tv) {
        tv.setText("");
        tv.setVisibility(View.GONE);
    }

    // Hàm hash password SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}