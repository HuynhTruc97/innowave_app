package com.nhom11.innowave;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.navigation.NavDestination;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nhom11.innowave.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private UserSessionManager sessionManager;
    
    // Method để các fragment có thể kiểm tra trạng thái đăng nhập
    public int getSignedInUserId() {
        return sessionManager.getUserId();
    }
    
    public boolean isUserLoggedIn() {
        return sessionManager.isLoggedIn();
    }
    
    public UserSessionManager getSessionManager() {
        return sessionManager;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Áp dụng ngôn ngữ trước khi inflate layout
        String language = LocaleHelper.getLanguage(this);
        android.util.Log.d("MainActivity", "Creating activity with language: " + language);
        
        // Force update resources với ngôn ngữ mới
        LocaleHelper.forceUpdateResources(this);
        
        EdgeToEdge.enable(this);
        
        // Initialize ViewBinding và UserSessionManager
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = new UserSessionManager(this);
        
        // Kiểm tra session khi khởi động
        if (sessionManager.isLoggedIn()) {
            // Không kiểm tra session hết hạn, không gọi logout tự động
            sessionManager.refreshSession(); // Nếu muốn kéo dài session
            android.util.Log.d("MainActivity", "User remains logged in. User ID: " + sessionManager.getUserId());
        }
        
        // Setup Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.container);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            
            // Setup Bottom Navigation with NavigationUI
            NavigationUI.setupWithNavController(binding.navbar, navController);
            
            // Override navigation to handle signed in state
            binding.navbar.setOnItemSelectedListener(item -> {
                if (item.getItemId() == R.id.mnProduct) {
                    // Chuyển sang fragment_product_page thay vì mở activity
                    NavDestination current = navController.getCurrentDestination();
                    if (current == null || current.getId() != R.id.fragment_product_page) {
                        navController.navigate(R.id.fragment_product_page);
                    }
                    return true;
                } else if (item.getItemId() == R.id.fragment_guest_account) {
                    if (sessionManager.isLoggedIn()) {
                        NavDestination current = navController.getCurrentDestination();
                        if (current == null || current.getId() != R.id.fragment_signed_in_account) {
                            navController.navigate(R.id.fragment_signed_in_account);
                        }
                        return true;
                    }
                } else if (item.getItemId() == R.id.fragment_home_page) {
                    // Đảm bảo luôn mở được fragment home
                    NavDestination current = navController.getCurrentDestination();
                    if (current == null || current.getId() != R.id.fragment_home_page) {
                        navController.navigate(R.id.fragment_home_page);
                    }
                    return true;
                } else if (item.getItemId() == R.id.fragment_cart) {
                    // Đảm bảo luôn mở được fragment cart
                    NavDestination current = navController.getCurrentDestination();
                    if (current == null || current.getId() != R.id.fragment_cart) {
                        navController.navigate(R.id.fragment_cart);
                    }
                    return true;
                }
                return NavigationUI.onNavDestinationSelected(item, navController);
            });
        }
        
        // Xử lý intent để chuyển tab tương ứng
        handleIntent(getIntent());
        
        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;
        if (intent.hasExtra("signed_in_user_id")) {
            int userId = intent.getIntExtra("signed_in_user_id", -1);
            String userName = intent.getStringExtra("user_name");
            String userPhone = intent.getStringExtra("user_phone");
            
            // Lưu session cho user
            sessionManager.createLoginSession(userId, userName != null ? userName : "User", userPhone != null ? userPhone : "");
            
            android.util.Log.d("MainActivity", "Received signed_in_user_id: " + userId);
            
            // Chuyển thẳng sang fragment_signed_in_account sau khi đăng nhập thành công
            new Handler().postDelayed(() -> {
                try {
                    navController.navigate(R.id.fragment_signed_in_account);
                    // Set selected item cho navbar
                    binding.navbar.setSelectedItemId(R.id.fragment_guest_account);
                    android.util.Log.d("MainActivity", "Navigated to fragment_signed_in_account and set navbar");
                } catch (Exception e) {
                    android.util.Log.e("MainActivity", "Error navigating to fragment: " + e.getMessage());
                }
            }, 200);
        } else if (intent.getBooleanExtra("open_cart", false)) {
            binding.navbar.setSelectedItemId(R.id.fragment_cart);
        } else if (intent.getBooleanExtra("open_home", false)) {
            binding.navbar.setSelectedItemId(R.id.fragment_home_page);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Đảm bảo ngôn ngữ được áp dụng đúng
        LocaleHelper.onAttach(this);
    }
}