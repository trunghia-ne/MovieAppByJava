package com.example.movieappbyjava.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ImageUtils {
    private static final String CLOUDINARY_CLOUD_NAME = "ddrqfuaji";
    private static final String CLOUDINARY_API_KEY = "269786956118767";
    private static final String CLOUDINARY_API_SECRET = "BE2OLRq9PSwXEg7RnjAqnCOQtBM";
    private static final String AVATAR_PREFIX = "avatar_";
    private static final String IMAGE_EXTENSION = ".jpg";

    private final Context context;
    private Uri avatarUri;
    private final ImageView avatarImageView;
    private final ActivityResultLauncher<Intent> pickImageLauncher;
    private final ActivityResultLauncher<Intent> captureImageLauncher;

    public ImageUtils(Context context, ImageView avatarImageView) {
        this.context = context;
        this.avatarImageView = avatarImageView;
        this.pickImageLauncher = registerPickImageLauncher();
        this.captureImageLauncher = registerCaptureImageLauncher();
    }

    public Uri getAvatarUri() {
        return avatarUri;
    }

    public void showImagePickerDialog() {
        String[] options = {
                "Chọn ảnh từ thư viện",
                "Chụp ảnh mới",
                "Chọn ảnh từ thiết bị"
        };
        new AlertDialog.Builder(context)
                .setTitle("Chọn ảnh đại diện")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            pickImageFromGallery();
                            break;
                        case 1:
                            captureImageWithCamera();
                            break;
                        case 2:
                            pickImageFromFileManager();
                            break;
                    }
                })
                .show();
    }

    private ActivityResultLauncher<Intent> registerPickImageLauncher() {
        return ((AppCompatActivity) context).registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            avatarUri = selectedImageUri;
                            loadImageIntoAvatarView(selectedImageUri);
                        }
                    }
                });
    }

    private ActivityResultLauncher<Intent> registerCaptureImageLauncher() {
        return ((AppCompatActivity) context).registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            Bitmap imageBitmap = (Bitmap) extras.get("data");
                            if (imageBitmap != null) {
                                avatarUri = saveBitmapToCache(imageBitmap);
                                loadImageIntoAvatarView(avatarUri);
                            }
                        }
                    }
                });
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void captureImageWithCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        captureImageLauncher.launch(intent);
    }

    private void pickImageFromFileManager() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        pickImageLauncher.launch(Intent.createChooser(intent, "Chọn ảnh"));
    }

    public void uploadImageToCloudinary(Uri imageUri, UploadCallback callback) {
        new Thread(() -> {
            try {
                File file = uriToFile(imageUri);
                if (file == null) {
                    runOnUiThread(() -> callback.onFailure("Không thể chuyển đổi ảnh thành tệp"));
                    return;
                }

                Map<String, String> config = new HashMap<>();
                config.put("cloud_name", CLOUDINARY_CLOUD_NAME);
                config.put("api_key", CLOUDINARY_API_KEY);
                config.put("api_secret", CLOUDINARY_API_SECRET);

                Cloudinary cloudinary = new Cloudinary(config);
                Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.emptyMap());
                String imageUrl = uploadResult.get("secure_url").toString();

                runOnUiThread(() -> {
                    loadImageIntoAvatarView(imageUrl);
                    callback.onSuccess(imageUrl);
                });
            } catch (Exception e) {
                runOnUiThread(() -> callback.onFailure("Lỗi tải ảnh: " + e.getMessage()));
            }
        }).start();
    }

    private File uriToFile(Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile(AVATAR_PREFIX + UUID.randomUUID(), IMAGE_EXTENSION, context.getCacheDir());
            tempFile.deleteOnExit();

            try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        } catch (Exception e) {
            return null;
        }
    }

    private Uri saveBitmapToCache(Bitmap bitmap) {
        try {
            File cachePath = new File(context.getCacheDir(), "images");
            if (!cachePath.exists()) cachePath.mkdirs();
            File file = new File(cachePath, "camera_image" + IMAGE_EXTENSION);
            try (FileOutputStream stream = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            }
            return Uri.fromFile(file);
        } catch (Exception e) {
            return null;
        }
    }

    public void loadImageIntoAvatarView(Object imageSource) {
        Glide.with(context)
                .load(imageSource)
                .circleCrop()
                .into(avatarImageView);
    }

    private void runOnUiThread(Runnable runnable) {
        ((AppCompatActivity) context).runOnUiThread(runnable);
    }

    public interface UploadCallback {
        void onSuccess(String downloadUrl);
        void onFailure(String errorMessage);
    }
}