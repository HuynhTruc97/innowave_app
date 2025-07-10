package com.nhom11.innowave;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

public class LoginAlertDialog {
    private Context context;
    private AlertDialog dialog;

    public LoginAlertDialog(Context context) {
        this.context = context;
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Thông báo")
               .setMessage("Bạn phải đăng nhập để thực hiện chức năng này")
               .setPositiveButton("Đăng nhập", (dialog, which) -> {
                   // Chuyển đến trang đăng nhập
                   Intent intent = new Intent(context, SignInActivity.class);
                   context.startActivity(intent);
               })
               .setNegativeButton("Hủy", (dialog, which) -> {
                   // Đóng dialog và tiếp tục
                   dialog.dismiss();
               })
               .setCancelable(true);

        dialog = builder.create();
        dialog.show();
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
} 