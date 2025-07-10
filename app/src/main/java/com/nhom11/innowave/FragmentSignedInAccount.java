package com.nhom11.innowave;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nhom11.innowave.databinding.FragmentSignedInAccountBinding;
import com.nhom11.models.User;
import com.nhom11.innowave.MyDatabase;
import android.app.AlertDialog;
import androidx.navigation.fragment.NavHostFragment;
import android.content.Intent;
import android.content.res.Configuration;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentSignedInAccount#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentSignedInAccount extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private MyDatabase db;
    private FragmentSignedInAccountBinding binding;
    private UserSessionManager sessionManager;

    public FragmentSignedInAccount() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentSignedInAccount.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentSignedInAccount newInstance(String param1, String param2) {
        FragmentSignedInAccount fragment = new FragmentSignedInAccount();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new MyDatabase(requireContext());
        sessionManager = new UserSessionManager(requireContext());
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment using ViewBinding
        binding = FragmentSignedInAccountBinding.inflate(inflater, container, false);
        
        android.util.Log.d("FragmentSignedInAccount", "Fragment created");
        
        // Hiển thị thông tin user từ session
        if (sessionManager.isLoggedIn()) {
            String userName = sessionManager.getUserName();
            String userPhone = sessionManager.getUserPhone();
            if (userName != null && !userName.trim().isEmpty() && !userName.equals("User")) {
                binding.txtUserName.setText(userName);
            } else {
                binding.txtUserName.setText(userPhone);
            }
            
            // Hiển thị ngôn ngữ hiện tại
            String currentLanguage = LocaleHelper.getLanguage(requireContext());
            if (currentLanguage.equals(LocaleHelper.getVietnamese())) {
                binding.txtLanguage.setText("Tiếng Việt");
                binding.imvCountry.setImageResource(R.drawable.ic_vietnam_flag);
            } else {
                binding.txtLanguage.setText("English");
                binding.imvCountry.setImageResource(R.drawable.ic_english_flag);
            }
            
            // Thêm click listener cho nút quản lý địa chỉ
            binding.btnManageAddress.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ManageAddressActivity.class);
                startActivity(intent);
            });
            
            // Thêm click listener cho nút mời bạn bè
            binding.btnInviteFriends.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), InviteFriendsActivity.class);
                startActivity(intent);
            });
            
            // Thêm click listener cho nút thiết bị của tôi
            binding.btnMyDevice.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), AddDeviceActivity.class);
                startActivity(intent);
            });
            
            // Thêm click listener cho nút chat
            binding.btnChat.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ChatActivity.class);
                startActivity(intent);
            });
            
            // Thêm click listener cho nút chính sách
            binding.btnPolicy.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), PolicyActivity.class);
                startActivity(intent);
            });
            
            // Thêm click listener cho nút thay đổi ngôn ngữ
            binding.btnChangeLanguage.setOnClickListener(v -> {
                android.util.Log.d("FragmentSignedInAccount", "Language button clicked!");
                
                // Lấy ngôn ngữ hiện tại
                String currentLang = LocaleHelper.getLanguage(requireContext());
                String contextLang = LocaleHelper.getCurrentLanguage(requireContext());
                android.util.Log.d("FragmentSignedInAccount", "Current language from SharedPrefs: " + currentLang);
                android.util.Log.d("FragmentSignedInAccount", "Current language from context: " + contextLang);
                
                String newLanguage;
                String languageName;
                int flagResource;
                
                if (currentLang.equals(LocaleHelper.getVietnamese())) {
                    newLanguage = LocaleHelper.getEnglish();
                    languageName = "English";
                    flagResource = R.drawable.ic_english_flag;
                    android.util.Log.d("FragmentSignedInAccount", "Switching to English");
                } else {
                    newLanguage = LocaleHelper.getVietnamese();
                    languageName = "Tiếng Việt";
                    flagResource = R.drawable.ic_vietnam_flag;
                    android.util.Log.d("FragmentSignedInAccount", "Switching to Vietnamese");
                }
                
                android.util.Log.d("FragmentSignedInAccount", "Changing to language: " + newLanguage);
                
                // Thay đổi ngôn ngữ
                LocaleHelper.setLocale(requireContext(), newLanguage);
                
                // Force update resources
                LocaleHelper.forceUpdateResources(requireActivity());
                
                // Test string resource
                String testString = getString(R.string.login_required_title);
                android.util.Log.d("FragmentSignedInAccount", "Test string: " + testString);
                
                // Kiểm tra ngôn ngữ sau khi đổi
                String newContextLang = LocaleHelper.getCurrentLanguage(requireContext());
                android.util.Log.d("FragmentSignedInAccount", "New context language: " + newContextLang);
                
                // Cập nhật giao diện ngay lập tức
                binding.txtLanguage.setText(languageName);
                binding.imvCountry.setImageResource(flagResource);
                
                // Hiển thị thông báo
                android.widget.Toast.makeText(requireContext(), 
                    "Language changed to " + languageName, 
                    android.widget.Toast.LENGTH_SHORT).show();
                
                // Restart activity để áp dụng thay đổi
                android.util.Log.d("FragmentSignedInAccount", "Restarting activity...");
                LocaleHelper.forceRestartActivity(requireActivity());
            });
            
            // Thêm click listener cho nút đăng xuất
            binding.btnSignOut.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        // Xóa session
                        sessionManager.logout();
                        // Ghi log hoạt động
                        LogActivity logActivity = new LogActivity(requireContext());
                        logActivity.logActivity(sessionManager.getUserId(), "LOGOUT", "User logged out");
                        // Chuyển về fragment guest account bằng NavController
                        NavHostFragment.findNavController(FragmentSignedInAccount.this)
                            .navigate(R.id.fragment_guest_account);
                        android.widget.Toast.makeText(requireContext(), "Đã đăng xuất thành công", android.widget.Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                    .show();
            });
            
            // Thêm click listener cho nút quản lý đơn hàng
            binding.btnManageOrder.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ManageOrderActivity.class);
                startActivity(intent);
            });
            
        } else {
            android.util.Log.e("FragmentSignedInAccount", "User not logged in");
        }
        return binding.getRoot();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}