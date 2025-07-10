package com.nhom11.innowave;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.text.TextUtils;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.ImageView;
import com.nhom11.innowave.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;
    private MyDatabase db;
    private TextView tvErrorPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
        //     Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        //     v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        //     return insets;
        // });
        db = new MyDatabase(this);
        binding.btnBack.setOnClickListener(v -> finish());
        binding.edtPhone.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String phone = s.toString().trim();
                boolean valid = phone.matches("^0[0-9]{9}$");
                binding.btnContinue.setEnabled(valid);
                if (valid) {
                    binding.btnContinue.setBackgroundResource(R.color.primary_color);
                    binding.btnContinue.setTextColor(getResources().getColor(android.R.color.white));
                    binding.tvErrorPhone.setVisibility(android.view.View.GONE);
                } else {
                    binding.btnContinue.setBackgroundResource(R.color.l_grey_color);
                    binding.btnContinue.setTextColor(getResources().getColor(R.color.grey_color));
                    if (!phone.isEmpty()) {
                        binding.tvErrorPhone.setText("Số điện thoại phải đủ 10 số và bắt đầu bằng 0");
                        binding.tvErrorPhone.setVisibility(android.view.View.VISIBLE);
                    } else {
                        binding.tvErrorPhone.setVisibility(android.view.View.GONE);
                    }
                }
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        binding.btnContinue.setOnClickListener(v -> {
            String phone = binding.edtPhone.getText().toString().trim();
            if (!phone.matches("^0[0-9]{9}$")) {
                binding.tvErrorPhone.setText("Số điện thoại phải đủ 10 số và bắt đầu bằng 0");
                binding.tvErrorPhone.setVisibility(android.view.View.VISIBLE);
                return;
            }
            // Kiểm tra số điện thoại có tồn tại không
            com.nhom11.models.User user = db.getUserByPhone(phone);
            if (user == null) {
                binding.tvErrorPhone.setText("Số điện thoại chưa đăng ký tài khoản!");
                binding.tvErrorPhone.setVisibility(android.view.View.VISIBLE);
                return;
            }
            binding.tvErrorPhone.setVisibility(android.view.View.GONE);
            // Gửi OTP và chuyển sang OTPActivity
            Intent intent = new Intent(this, OTPActivity.class);
            intent.putExtra("phone", phone);
            intent.putExtra("flow", "forgot_password"); // Đánh dấu flow quên mật khẩu
            startActivity(intent);
            finish();
        });
    }
}