package com.example.movieappbyjava;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.movieappbyjava.model.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MyAccountActivity extends AppCompatActivity {

    private ImageView avatarImageView;
    private TextView changeAvatarTextView;
    private EditText nameEditText, emailEditText, phoneEditText;
    private EditText currentPasswordEditText, newPasswordEditText;
    private Button saveButton, changePasswordButton;

    private String userId;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;
    private Uri avatarUri;

    // Launcher cho chọn ảnh từ thư viện và từ file manager (ổ đĩa)
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        avatarUri = selectedImageUri;
                        Glide.with(this).load(avatarUri).circleCrop().into(avatarImageView);
                    }
                }
            });

    // Launcher cho chụp ảnh từ camera (lấy ảnh dạng Bitmap)
    private final ActivityResultLauncher<Intent> captureImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) {
                            // Lưu Bitmap tạm thời và chuyển thành Uri nếu cần upload
                            avatarImageView.setImageBitmap(imageBitmap);
                            // Chú ý: Bạn cần lưu bitmap thành file để có Uri, nếu upload lên Firebase
                            // Ở đây tạm chưa xử lý chuyển Bitmap -> Uri
                            // Bạn có thể bổ sung phương thức saveBitmapToFile() nếu cần
                            avatarUri = null; // Đặt null để tránh upload sai
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        avatarImageView = findViewById(R.id.avatarImageView);
        changeAvatarTextView = findViewById(R.id.changeAvatarTextView);
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        currentPasswordEditText = findViewById(R.id.currentPasswordEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        saveButton = findViewById(R.id.saveButton);
        changePasswordButton = findViewById(R.id.changePasswordButton);

        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = currentUser.getUid();

        fetchUserData(userId);

        changeAvatarTextView.setOnClickListener(v -> showImagePickerOptions());
        avatarImageView.setOnClickListener(v -> showImagePickerOptions());

        saveButton.setOnClickListener(v -> saveUserChanges());
        changePasswordButton.setOnClickListener(v -> changePassword());
    }

    private void showImagePickerOptions() {
        String[] options = {
                "Chọn ảnh từ thư viện",
                "Chụp ảnh mới",
                "Chọn ảnh từ thiết bị (Laptop/Điện thoại)"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn ảnh đại diện")
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
                }).show();
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
        pickImageLauncher.launch(Intent.createChooser(intent, "Chọn ảnh từ thiết bị"));
    }


    private void fetchUserData(String userId) {
        DocumentReference docRef = firestore.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    nameEditText.setText(user.getUsername());
                    emailEditText.setText(user.getEmail());
                    phoneEditText.setText(user.getPhone());

                    if (user.getImage() != null && !user.getImage().isEmpty()) {
                        Glide.with(this)
                                .load(user.getImage())
                                .placeholder(R.drawable.ic_user)
                                .circleCrop()
                                .into(avatarImageView);
                    } else {
                        avatarImageView.setImageResource(R.drawable.ic_user);
                    }
                }
            } else {
                Toast.makeText(this, "Không tìm thấy dữ liệu người dùng", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveUserChanges() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        if (avatarUri != null) {
            uploadImageToFirebase(avatarUri, new UploadCallback() {
                @Override
                public void onUploadSuccess(String downloadUrl) {
                    updateUserInfo(name, email, phone, downloadUrl);
                }

                @Override
                public void onUploadFailure(String errorMessage) {
                    Toast.makeText(MyAccountActivity.this, "Lỗi tải ảnh: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Nếu avatarUri null thì vẫn cập nhật thông tin, giữ ảnh cũ
            updateUserInfo(name, email, phone, null);
        }
    }

    private void updateUserInfo(String name, String email, String phone, @Nullable String imageUrl) {
        User updatedUser = new User();
        updatedUser.setUsername(name);
        updatedUser.setEmail(email);
        updatedUser.setPhone(phone);
        if (imageUrl != null) {
            updatedUser.setImage(imageUrl);
        }

        firestore.collection("users").document(userId)
                .set(updatedUser)
                .addOnSuccessListener(aVoid -> Toast.makeText(MyAccountActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(MyAccountActivity.this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void uploadImageToFirebase(Uri imageUri, UploadCallback callback) {
        if (imageUri == null) {
            callback.onUploadFailure("Uri ảnh rỗng");
            return;
        }
        StorageReference fileRef = storageReference.child("avatars/" + userId + ".jpg");
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    callback.onUploadSuccess(uri.toString());
                }))
                .addOnFailureListener(e -> callback.onUploadFailure(e.getMessage()));
    }

    private interface UploadCallback {
        void onUploadSuccess(String downloadUrl);

        void onUploadFailure(String errorMessage);
    }

    private void changePassword() {
        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();

        if (currentPassword.isEmpty()) {
            currentPasswordEditText.setError("Vui lòng nhập mật khẩu hiện tại");
            currentPasswordEditText.requestFocus();
            return;
        }

        if (newPassword.isEmpty()) {
            newPasswordEditText.setError("Vui lòng nhập mật khẩu mới");
            newPasswordEditText.requestFocus();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "Lỗi xác thực người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

        user.reauthenticate(credential).addOnSuccessListener(unused -> {
            user.updatePassword(newPassword).addOnSuccessListener(unused1 -> {
                Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                currentPasswordEditText.setText("");
                newPasswordEditText.setText("");
            }).addOnFailureListener(e -> Toast.makeText(this, "Đổi mật khẩu thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(this, "Xác thực mật khẩu hiện tại thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
