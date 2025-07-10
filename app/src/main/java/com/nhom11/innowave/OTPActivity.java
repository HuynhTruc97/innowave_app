package com.nhom11.innowave;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nhom11.innowave.databinding.ActivityOtpBinding;
import com.nhom11.models.OTPCode;

import java.util.Random;

public class OTPActivity extends AppCompatActivity {
    private ActivityOtpBinding binding;
    private MyDatabase db;
    private String phone;
    private int userId;
    private CountDownTimer timer;
    private static final int OTP_LENGTH = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        // Initialize ViewBinding
        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new MyDatabase(this);
        phone = getIntent().getStringExtra("phone");
        if (phone == null) {
            Toast.makeText(this, "Lỗi: Không nhận được số điện thoại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Lấy hoặc tạo user
        com.nhom11.models.User user = db.getUserByPhone(phone);
        if (user == null) {
            userId = (int) db.createUserWithPhone(phone);
        } else {
            userId = user.user_id;
        }

        // Tạo và lưu OTP
        String otpCode = generateOTP();
        String expiresAt = String.valueOf(System.currentTimeMillis() + 60 * 1000); // 60 giây
        db.createOTP(userId, otpCode, expiresAt);
        
        // Hiển thị OTP để test trong 5 giây
        Toast.makeText(this, "Mã OTP: " + otpCode, Toast.LENGTH_LONG).show();
        
        // Tắt OTP sau 5 giây
        new android.os.Handler().postDelayed(() -> {
            // Có thể thêm logic ẩn OTP ở đây nếu cần
        }, 5000);

        // Setup OTP input fields
        EditText[] otpFields = {
            binding.otp1, binding.otp2, binding.otp3,
            binding.otp4, binding.otp5, binding.otp6
        };

        // Setup text watchers for auto-focus
        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otpFields.length - 1) {
                        otpFields[index + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
            // Thêm xử lý phím backspace
            otpFields[i].setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == android.view.KeyEvent.ACTION_DOWN && keyCode == android.view.KeyEvent.KEYCODE_DEL) {
                    if (otpFields[index].getText().toString().isEmpty() && index > 0) {
                        otpFields[index - 1].requestFocus();
                        otpFields[index - 1].setText("");
                        return true;
                    }
                }
                return false;
            });
        }

        // Setup back button
        binding.btnBack.setOnClickListener(v -> {
            finish();
        });

        // Setup OTP verification on text change
        TextWatcher otpTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String enteredOTP = binding.otp1.getText().toString() +
                        binding.otp2.getText().toString() +
                        binding.otp3.getText().toString() +
                        binding.otp4.getText().toString() +
                        binding.otp5.getText().toString() +
                        binding.otp6.getText().toString();

                String flow = getIntent().getStringExtra("flow");

                if (enteredOTP.length() == OTP_LENGTH) {
                    // Auto-verify when all 6 digits are entered
                    Toast.makeText(OTPActivity.this, "Đang kiểm tra OTP: " + enteredOTP, Toast.LENGTH_SHORT).show();
                    if (db.verifyOTP(userId, enteredOTP)) {
                        // Đăng nhập thành công hoặc xác thực OTP thành công
                        android.util.Log.d("OTPActivity", "OTP verified successfully for user: " + userId);
                        if ("forgot_password".equals(flow)) {
                            // Chuyển sang trang tạo mật khẩu mới
                            Intent intent = new Intent(OTPActivity.this, CreateNewPasswordActivity.class);
                            intent.putExtra("user_id", userId);
                            intent.putExtra("phone", phone);
                            startActivity(intent);
                            finish();
                        } else {
                            // Lấy thông tin user từ database
                            com.nhom11.models.User user = db.getUserByPhone(phone);
                            String userName = user != null ? user.full_name : "User";
                            // Ghi log hoạt động
                            LogActivity logActivity = new LogActivity(OTPActivity.this);
                            logActivity.logActivity(userId, "LOGIN", "User logged in successfully");
                            Intent intent = new Intent(OTPActivity.this, MainActivity.class);
                            intent.putExtra("signed_in_user_id", userId);
                            intent.putExtra("user_name", userName);
                            intent.putExtra("user_phone", phone);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            android.util.Log.d("OTPActivity", "Starting MainActivity with signed_in_user_id: " + userId);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        // Hiển thị thông báo lỗi dưới thời gian
                        binding.tvError.setText("Mã xác nhận không đúng hoặc đã hết hạn");
                        binding.tvError.setVisibility(android.view.View.VISIBLE);
                        binding.tvError.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    }
                } else {
                    binding.tvError.setVisibility(android.view.View.GONE);
                }
            }
        };

        // Add text watcher to all OTP fields
        binding.otp1.addTextChangedListener(otpTextWatcher);
        binding.otp2.addTextChangedListener(otpTextWatcher);
        binding.otp3.addTextChangedListener(otpTextWatcher);
        binding.otp4.addTextChangedListener(otpTextWatcher);
        binding.otp5.addTextChangedListener(otpTextWatcher);
        binding.otp6.addTextChangedListener(otpTextWatcher);

        // Setup resend button
        binding.tvResend.setOnClickListener(v -> {
            if (timer != null) {
                timer.cancel();
            }
            startResendTimer();
            // Tạo OTP mới
            String newOTP = generateOTP();
            String newExpiresAt = String.valueOf(System.currentTimeMillis() + 60 * 1000);
            db.createOTP(userId, newOTP, newExpiresAt);
            // Hiển thị OTP mới để test trong 5 giây
            Toast.makeText(this, "Mã OTP mới: " + newOTP, Toast.LENGTH_LONG).show();
            // Xóa các ô nhập OTP khi gửi lại mã
            for (EditText otpField : otpFields) {
                otpField.setText("");
            }
            // Tắt OTP sau 5 giây
            new android.os.Handler().postDelayed(() -> {
                // Có thể thêm logic ẩn OTP ở đây nếu cần
            }, 5000);
        });

        // Bắt đầu timer
        startResendTimer();
    }

    private void startResendTimer() {
        binding.tvResend.setEnabled(false);
        binding.tvTime.setVisibility(android.view.View.VISIBLE);
        timer = new CountDownTimer(60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                binding.tvTime.setText(String.format("%02d:%02d", minutes, seconds));
            }
            @Override
            public void onFinish() {
                binding.tvResend.setEnabled(true);
                binding.tvTime.setVisibility(android.view.View.GONE);
            }
        };
        timer.start();
    }

    private String generateOTP() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}