package com.nhom11.innowave;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.nhom11.innowave.databinding.ActivitySearchByImageBinding;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.common.util.concurrent.ListenableFuture;
import androidx.camera.view.PreviewView;

public class SearchByImageActivity extends AppCompatActivity {
    private ActivitySearchByImageBinding binding;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private Uri capturedImageUri;
    private PreviewView previewView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchByImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        previewView = binding.cameraPreview;
        // EdgeToEdge.enable(this); // Nếu cần giữ
        // Xử lý insets nếu cần
        // ViewCompat.setOnApplyWindowInsetsListener(binding.main, ...);

        cameraExecutor = Executors.newSingleThreadExecutor();
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            Boolean camGranted = result.getOrDefault(Manifest.permission.CAMERA, false);
            Boolean storageGranted = result.getOrDefault(Manifest.permission.READ_EXTERNAL_STORAGE, false);
            if (Boolean.TRUE.equals(camGranted) && Boolean.TRUE.equals(storageGranted)) {
                startCamera();
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền để sử dụng chức năng này", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri selectedImage = result.getData().getData();
                if (selectedImage != null) {
                    // TODO: Xử lý ảnh đã chọn từ gallery (ví dụ: preview hoặc gửi đi tìm kiếm)
                    Toast.makeText(this, "Đã chọn ảnh từ gallery", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Sự kiện nút back
        binding.btnBack.setOnClickListener(v -> finish());
        // Sự kiện nút gallery
        binding.btnGallery.setOnClickListener(v -> openGallery());
        // Sự kiện nút chụp ảnh
        binding.btnCapture.setOnClickListener(v -> takePhoto());
        // Kiểm tra/cấp quyền và khởi động camera
        if (hasPermissions()) {
            startCamera();
        } else {
            requestPermissions();
        }
    }

    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermissions() {
        permissionLauncher.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE});
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder().build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    private void takePhoto() {
        if (imageCapture == null) return;
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(
                getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new android.content.ContentValues()
        ).build();
        imageCapture.takePicture(outputOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                runOnUiThread(() -> Toast.makeText(SearchByImageActivity.this, "Đã chụp ảnh", Toast.LENGTH_SHORT).show());
                // TODO: Xử lý ảnh đã chụp (outputFileResults.getSavedUri())
            }
            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                runOnUiThread(() -> Toast.makeText(SearchByImageActivity.this, "Chụp ảnh thất bại", Toast.LENGTH_SHORT).show());
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) cameraExecutor.shutdown();
    }
}