package com.nhom11.innowave;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.EditText;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom11.adapters.ProductRecyclerAdapter;
import com.nhom11.innowave.databinding.FragmentHomePageBinding;
import com.nhom11.innowave.MyDatabase;
import com.nhom11.innowave.LoginAlertDialog;
import com.nhom11.innowave.AddToCartDialog;
import com.nhom11.innowave.UserSessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentHomePage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentHomePage extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentHomePageBinding binding;
    private MyDatabase myDatabase;

    private Uri cameraImageUri;
    private ActivityResultLauncher<String[]> permissionLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    public FragmentHomePage() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentHomePage.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentHomePage newInstance(String param1, String param2) {
        FragmentHomePage fragment = new FragmentHomePage();
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
        binding = FragmentHomePageBinding.inflate(inflater, container, false);
        
        myDatabase = new MyDatabase(requireContext());
        List<MyDatabase.Product> allProducts = myDatabase.getAllProducts();

        // Flash Sale Section - Top 3 products with highest discount
        List<MyDatabase.Product> flashProducts = getTopDiscountProducts(allProducts, 3);
        for (MyDatabase.Product p : flashProducts) {
            addProductToScrollView(binding.flashSaleScroll, p);
        }

        // Newest Section - 3 random products
        List<MyDatabase.Product> newestProducts = getRandomProducts(allProducts, 3);
        for (MyDatabase.Product p : newestProducts) {
            addProductToScrollView(binding.newestScroll, p);
        }

        // View All button
        binding.tvViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProductPageActivity.class);
            startActivity(intent);
        });

        // Search bar - mở ProductPageActivity khi nhập ký tự đầu tiên
        binding.etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1 && before == 0) { // Chỉ khi nhập ký tự đầu tiên
                    Intent intent = new Intent(getActivity(), ProductPageActivity.class);
                    intent.putExtra("search_query", s.toString());
                    intent.putExtra("focus_search", true);
                    startActivity(intent);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Random Products Grid - 6 random products
        RecyclerView rv = binding.randomProductsRecycler;
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));
        
        List<MyDatabase.Product> randomProducts = getRandomProducts(allProducts, 6);
        // Convert MyDatabase.Product to com.nhom11.models.Product
        List<com.nhom11.models.Product> convertedProducts = new ArrayList<>();
        for (MyDatabase.Product p : randomProducts) {
            convertedProducts.add(new com.nhom11.models.Product(
                p.product_id, p.name, p.category_id, p.default_variant_id,
                p.thumbnail_url, p.description, p.original_price, p.discounted_price,
                p.is_active, p.created_at, p.updated_at
            ));
        }
        ProductRecyclerAdapter adapter = new ProductRecyclerAdapter(getContext(), convertedProducts);
        rv.setAdapter(adapter);
        adapter.setOnItemClickListener(product -> {
            Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
            intent.putExtra("product_id", product.product_id);
            startActivity(intent);
        });

        // Đăng ký launcher xin quyền
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            Boolean camGranted = result.getOrDefault(Manifest.permission.CAMERA, false);
            Boolean storageGranted = result.getOrDefault(Manifest.permission.READ_EXTERNAL_STORAGE, false);
            if (Boolean.TRUE.equals(camGranted) && Boolean.TRUE.equals(storageGranted)) {
                showImageSourceDialog();
            } else {
                android.widget.Toast.makeText(requireContext(), "Bạn cần cấp quyền để sử dụng chức năng này", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        // Đăng ký launcher mở camera
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            // XÓA: binding.imvPreview.setImageURI(cameraImageUri);
            // XÓA: binding.imvPreview.setVisibility(View.VISIBLE);
        });
        // Đăng ký launcher mở gallery
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            // XÓA: binding.imvPreview.setImageURI(selectedImage);
            // XÓA: binding.imvPreview.setVisibility(View.VISIBLE);
        });
        // XÓA: if (binding.imvPreview == null) { ... binding.imvPreview = imvPreview; }
        // Sự kiện click ic_Camera
        binding.icCamera.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SearchByImageActivity.class);
            startActivity(intent);
        });

        return binding.getRoot();
    }

    private void addProductToScrollView(HorizontalScrollView scrollView, MyDatabase.Product p) {
        View item = LayoutInflater.from(getContext()).inflate(R.layout.product_item, null);
        
        // Giữ nguyên kích thước vốn có của product_item.xml (165dp width)
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
            (int) (165 * getResources().getDisplayMetrics().density), // 165dp
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 16, 0); // Add margin between items
        item.setLayoutParams(params);
        
        bindProductItem(item, p);
        ((android.widget.LinearLayout) scrollView.getChildAt(0)).addView(item);
    }

    private void bindProductItem(View item, MyDatabase.Product p) {
        ImageView img = item.findViewById(R.id.imvThumbnail);
        TextView tvName = item.findViewById(R.id.txtProductName);
        TextView tvPrice = item.findViewById(R.id.tvDiscountedPrice);
        TextView tvOriginalPrice = item.findViewById(R.id.tvOriginalPrice);
        TextView tvDiscount = item.findViewById(R.id.tvDiscount);
        ImageView ivCart = item.findViewById(R.id.ivCart);

        // Set product data
        if (p.thumbnail_url != null) {
            img.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(p.thumbnail_url, 0, p.thumbnail_url.length));
        }
        tvName.setText(p.name);
        tvPrice.setText(String.format("%,.0fđ", p.discounted_price));
        
        // Handle original price display
        if (p.discounted_price < p.original_price) {
            tvOriginalPrice.setText(String.format("%,.0fđ", p.original_price));
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            tvOriginalPrice.setVisibility(android.view.View.VISIBLE);
            
            // Calculate discount percentage
            int discountPercent = (int) ((p.original_price - p.discounted_price) / p.original_price * 100);
            tvDiscount.setText("-" + discountPercent + "%");
            tvDiscount.setVisibility(android.view.View.VISIBLE);
        } else {
            tvOriginalPrice.setVisibility(android.view.View.GONE);
            tvDiscount.setVisibility(android.view.View.GONE);
        }

        // Set click listener for product item
        item.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
            intent.putExtra("product_id", p.product_id);
            startActivity(intent);
        });

        // Set click listener for icCart
        ivCart.setOnClickListener(v -> {
            UserSessionManager sessionManager = new UserSessionManager(getContext());
            if (!sessionManager.isLoggedIn()) {
                // Hiện dialog yêu cầu đăng nhập
                LoginAlertDialog loginDialog = new LoginAlertDialog(getContext());
                loginDialog.show();
            } else {
                // Hiện dialog thêm vào giỏ hàng
                AddToCartDialog cartDialog = new AddToCartDialog(getContext(), p);
                cartDialog.show();
            }
        });
    }

    private List<MyDatabase.Product> getTopDiscountProducts(List<MyDatabase.Product> products, int limit) {
        List<MyDatabase.Product> sorted = new ArrayList<>(products);
        sorted.sort((p1, p2) -> {
            double discount1 = p1.original_price - p1.discounted_price;
            double discount2 = p2.original_price - p2.discounted_price;
            return Double.compare(discount2, discount1); // Descending order
        });
        return sorted.subList(0, Math.min(limit, sorted.size()));
    }

    private List<MyDatabase.Product> getRandomProducts(List<MyDatabase.Product> products, int limit) {
        List<MyDatabase.Product> copy = new java.util.ArrayList<>(products);
        Collections.shuffle(copy, new Random());
        return copy.subList(0, Math.min(limit, copy.size()));
    }
    
    private void showImageSourceDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Chọn nguồn ảnh")
            .setItems(new CharSequence[]{"Chụp ảnh", "Chọn từ thư viện"}, (dialog, which) -> {
                if (which == 0) {
                    // Camera
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraImageUri = createImageUri();
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                    cameraLauncher.launch(intent);
                } else {
                    // Gallery
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    galleryLauncher.launch(intent);
                }
            })
            .show();
    }
    private Uri createImageUri() {
        android.content.ContentValues values = new android.content.ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        return requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}