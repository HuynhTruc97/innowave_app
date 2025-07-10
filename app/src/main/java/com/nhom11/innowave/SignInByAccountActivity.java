package com.nhom11.innowave;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.nhom11.innowave.databinding.ActivitySignInByAccountBinding;
import android.graphics.Color;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import com.nhom11.models.User;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.nhom11.innowave.UserSessionManager;

public class SignInByAccountActivity extends AppCompatActivity {
    private ActivitySignInByAccountBinding binding;
    private MyDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInByAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = new MyDatabase(this);

        // Ẩn lỗi ban đầu
        binding.tvErrorPhone.setVisibility(View.GONE);
        binding.tvErrorPassword.setVisibility(View.GONE);

        // Show/hide password
        binding.ivShowPassword.setOnClickListener(v -> {
            if (binding.edtPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                binding.edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                binding.edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            binding.edtPassword.setSelection(binding.edtPassword.getText().length());
        });

        // Theo dõi thay đổi để kiểm tra hợp lệ và bật/tắt nút đăng nhập
        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { validateAll(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        };
        binding.edtPhone.addTextChangedListener(watcher);
        binding.edtPassword.addTextChangedListener(watcher);

        // Đăng nhập
        binding.btnDangNhap.setOnClickListener(v -> {
            if (!validateAll()) return;
            String phone = binding.edtPhone.getText().toString().trim();
            String password = binding.edtPassword.getText().toString();
            String passwordHash = hashPassword(password);
            User user = db.getUserByPhone(phone);
            if (user == null || !passwordHash.equals(user.password_hash)) {
                // Nếu sai số điện thoại hoặc mật khẩu, chỉ hiển thị chung một thông báo lỗi dưới ô mật khẩu
                hideError(binding.tvErrorPhone);
                showError(binding.tvErrorPassword, "Số điện thoại hoặc Mật khẩu không đúng, vui lòng thử lại");
                return;
            }
            // Đăng nhập thành công
            hideError(binding.tvErrorPhone);
            hideError(binding.tvErrorPassword);
            // Lưu session
            UserSessionManager sessionManager = new UserSessionManager(this);
            sessionManager.createLoginSession(user.user_id, user.full_name, user.phone_number);
            // Chuyển sang MainActivity (fragment tài khoản)
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("signed_in_user_id", user.user_id);
            intent.putExtra("user_name", user.full_name);
            intent.putExtra("user_phone", user.phone_number);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        binding.btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        });
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnLoginByOTP.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        });
        binding.btnForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private boolean validateAll() {
        boolean valid = true;
        String phone = binding.edtPhone.getText().toString().trim();
        String password = binding.edtPassword.getText().toString();
        if (!phone.matches("^0[0-9]{9}$")) {
            showError(binding.tvErrorPhone, "Số điện thoại phải đủ 10 số và bắt đầu bằng 0"); valid = false;
        } else hideError(binding.tvErrorPhone);
        if (TextUtils.isEmpty(password)) {
            showError(binding.tvErrorPassword, "Vui lòng nhập mật khẩu"); valid = false;
        } else hideError(binding.tvErrorPassword);
        binding.btnDangNhap.setEnabled(valid);
        if (valid) {
            binding.btnDangNhap.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_color));
            binding.btnDangNhap.setTextColor(Color.WHITE);
        } else {
            binding.btnDangNhap.setBackgroundColor(ContextCompat.getColor(this, R.color.l_grey_color));
            binding.btnDangNhap.setTextColor(ContextCompat.getColor(this, R.color.grey_color));
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