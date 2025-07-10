package com.nhom11.innowave;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.AlertDialog;

import com.nhom11.innowave.databinding.FragmentGuestAccountBinding;

import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentGuestAccount#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentGuestAccount extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    
    private FragmentGuestAccountBinding binding;

    public FragmentGuestAccount() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentGuestAccount.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentGuestAccount newInstance(String param1, String param2) {
        FragmentGuestAccount fragment = new FragmentGuestAccount();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment using ViewBinding
        binding = FragmentGuestAccountBinding.inflate(inflater, container, false);
        
        // Xử lý click cho nút đăng nhập
        binding.btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            startActivity(intent);
        });
        
        // Hiển thị ngôn ngữ hiện tại
        String currentLanguage = LocaleHelper.getLanguage(requireContext());
        if (currentLanguage.equals(LocaleHelper.getVietnamese())) {
            binding.txtLanguage.setText("Tiếng Việt");
            binding.imvCountry.setImageResource(R.drawable.ic_vietnam_flag);
        } else {
            binding.txtLanguage.setText("English");
            binding.imvCountry.setImageResource(R.drawable.ic_english_flag);
        }
        
        // Xử lý click cho các nút cần đăng nhập
        binding.btnInviteFriends.setOnClickListener(v -> {
            showLoginRequiredDialog();
        });
        
        binding.btnHistoryPoints.setOnClickListener(v -> {
            showLoginRequiredDialog();
        });
        
        binding.btnManageOrder.setOnClickListener(v -> {
            showLoginRequiredDialog();
        });
        
        binding.btnManageAddress.setOnClickListener(v -> {
            showLoginRequiredDialog();
        });
        
        // Xử lý click cho các nút không cần đăng nhập
        binding.btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            startActivity(intent);
        });
        
        binding.btnPolicy.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PolicyActivity.class);
            startActivity(intent);
        });
        
        binding.btnChangeLanguage.setOnClickListener(v -> {
            android.util.Log.d("FragmentGuestAccount", "Language button clicked!");
            
            // Lấy ngôn ngữ hiện tại
            String currentLang = LocaleHelper.getLanguage(requireContext());
            android.util.Log.d("FragmentGuestAccount", "Current language: " + currentLang);
            
            String newLanguage;
            String languageName;
            int flagResource;
            
            if (currentLang.equals(LocaleHelper.getVietnamese())) {
                newLanguage = LocaleHelper.getEnglish();
                languageName = "English";
                flagResource = R.drawable.ic_english_flag;
                android.util.Log.d("FragmentGuestAccount", "Switching to English");
            } else {
                newLanguage = LocaleHelper.getVietnamese();
                languageName = "Tiếng Việt";
                flagResource = R.drawable.ic_vietnam_flag;
                android.util.Log.d("FragmentGuestAccount", "Switching to Vietnamese");
            }
            
            android.util.Log.d("FragmentGuestAccount", "Changing to language: " + newLanguage);
            
            // Thay đổi ngôn ngữ
            LocaleHelper.setLocale(requireContext(), newLanguage);
            
            // Force update resources
            LocaleHelper.forceUpdateResources(requireActivity());
            
            // Cập nhật giao diện ngay lập tức
            binding.txtLanguage.setText(languageName);
            binding.imvCountry.setImageResource(flagResource);
            
            // Hiển thị thông báo
            android.widget.Toast.makeText(requireContext(), 
                "Language changed to " + languageName, 
                android.widget.Toast.LENGTH_SHORT).show();
            
            // Restart activity để áp dụng thay đổi
            android.util.Log.d("FragmentGuestAccount", "Restarting activity...");
            LocaleHelper.forceRestartActivity(requireActivity());
        });
        
        return binding.getRoot();
    }
    
    /**
     * Hiển thị dialog yêu cầu đăng nhập
     */
    private void showLoginRequiredDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(getString(R.string.notification))
               .setMessage(getString(R.string.login_required_message))
               .setPositiveButton(getString(R.string.login), (dialog, which) -> {
                   // Chuyển đến trang đăng nhập
                   Intent intent = new Intent(getActivity(), SignInActivity.class);
                   startActivity(intent);
               })
               .setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                   dialog.dismiss();
               })
               .setCancelable(true);
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}