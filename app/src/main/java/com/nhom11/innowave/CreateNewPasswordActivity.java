package com.nhom11.innowave;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.Intent;
import android.text.TextUtils;
import android.text.InputType;
import androidx.core.content.ContextCompat;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CreateNewPasswordActivity extends AppCompatActivity {
    private EditText edtPassword, edtRePassword;
    private ImageView ivShowPassword, ivShowRePassword;
    private Button btnConfirm;
    private int userId;
    private MyDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_new_passwor);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        edtPassword = findViewById(R.id.edtPassword);
        edtRePassword = findViewById(R.id.edtRePassword);
        ivShowPassword = findViewById(R.id.ivShowPassword);
        ivShowRePassword = findViewById(R.id.ivShowRePassword);
        btnConfirm = findViewById(R.id.btnConfirm);
        db = new MyDatabase(this);
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Lỗi: Không xác định được user.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Show/hide password
        ivShowPassword.setOnClickListener(v -> {
            if (edtPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            edtPassword.setSelection(edtPassword.getText().length());
        });
        ivShowRePassword.setOnClickListener(v -> {
            if (edtRePassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                edtRePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                edtRePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            edtRePassword.setSelection(edtRePassword.getText().length());
        });
        // Theo dõi thay đổi để kiểm tra hợp lệ và bật/tắt nút xác nhận
        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { validateAll(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        };
        edtPassword.addTextChangedListener(watcher);
        edtRePassword.addTextChangedListener(watcher);
        btnConfirm.setOnClickListener(v -> {
            if (!validateAll()) return;
            String password = edtPassword.getText().toString();
            String passwordHash = hashPassword(password);
            boolean ok = db.updateUserPassword(userId, passwordHash);
            if (ok) {
                showSuccessDialog();
            } else {
                Toast.makeText(this, "Đổi mật khẩu thất bại!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private boolean validateAll() {
        String password = edtPassword.getText().toString();
        String rePassword = edtRePassword.getText().toString();
        boolean valid = true;
        if (!isValidPassword(password)) {
            edtPassword.setError("Mật khẩu phải từ 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt");
            valid = false;
        } else {
            edtPassword.setError(null);
        }
        if (!password.equals(rePassword)) {
            edtRePassword.setError("Mật khẩu nhập lại không khớp");
            valid = false;
        } else {
            edtRePassword.setError(null);
        }
        btnConfirm.setEnabled(valid);
        if (valid) {
            btnConfirm.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_color));
        } else {
            btnConfirm.setBackgroundColor(ContextCompat.getColor(this, R.color.l_grey_color));
        }
        return valid;
    }
    private boolean isValidPassword(String password) {
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return password.matches(pattern);
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
    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Thành công")
            .setMessage("Đổi mật khẩu thành công! Bạn có thể đăng nhập lại bằng mật khẩu mới.")
            .setCancelable(false)
            .setPositiveButton("Đăng nhập", (dialog, which) -> {
                Intent intent = new Intent(this, SignInByAccountActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            })
            .show();
    }
}