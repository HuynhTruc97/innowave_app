package com.nhom11.innowave;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;

public class LoginRequiredDialog extends Dialog {
    private Context context;

    public LoginRequiredDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_login_required);

        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnLogin = findViewById(R.id.btnLogin);

        btnCancel.setOnClickListener(v -> dismiss());

        btnLogin.setOnClickListener(v -> {
            // Chuyển đến trang đăng nhập
            Intent intent = new Intent(context, SignInActivity.class);
            context.startActivity(intent);
            dismiss();
        });
    }
} 