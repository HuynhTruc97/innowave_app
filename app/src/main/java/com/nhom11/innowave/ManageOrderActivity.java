package com.nhom11.innowave;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.tabs.TabLayout;
import com.nhom11.innowave.databinding.ActivityManageOrderBinding;

public class ManageOrderActivity extends AppCompatActivity {
    ActivityManageOrderBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Lấy TabLayout từ layout
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        // Thêm các tab vào TabLayout
        tabLayout.addTab(tabLayout.newTab().setText("Chờ thanh toán"));
        tabLayout.addTab(tabLayout.newTab().setText("Chờ giao hàng"));
        tabLayout.addTab(tabLayout.newTab().setText("Đã hoàn thành"));
    }
}