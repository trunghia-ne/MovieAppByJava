package com.example.movieappbyjava;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.movieappbyjava.databinding.ActivityMyAccountBinding;
import com.example.movieappbyjava.model.User;
import com.example.movieappbyjava.service.MyAccountService;
import com.example.movieappbyjava.util.ImageUtils;

public class MyAccountActivity extends AppCompatActivity {
    private ActivityMyAccountBinding binding;
    private MyAccountService myAccountService;
    private ImageUtils imageUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        myAccountService = new MyAccountService(this);
        imageUtils = new ImageUtils(this, binding.avatarImageView);

        if (!myAccountService.isUserLoggedIn()) {
            finish();
            return;
        }

        String userId = myAccountService.getCurrentUserId();
        setupUI();
        fetchUserData(userId);
    }

    private void setupUI() {
        binding.changeAvatarTextView.setOnClickListener(v -> imageUtils.showImagePickerDialog());
        binding.avatarImageView.setOnClickListener(v -> imageUtils.showImagePickerDialog());
        binding.saveButton.setOnClickListener(v -> saveUserChanges());
        binding.changePasswordButton.setOnClickListener(v -> changePassword());
    }

    private void fetchUserData(String userId) {
        myAccountService.fetchUserData(userId, new MyAccountService.UserCallback() {
            @Override
            public void onSuccess(User user) {
                binding.nameEditText.setText(user.getUsername());
                binding.emailEditText.setText(user.getEmail());
                binding.phoneEditText.setText(user.getPhone());
                if (user.getImage() != null && !user.getImage().isEmpty()) {
                    imageUtils.loadImageIntoAvatarView(user.getImage());
                } else {
                    binding.avatarImageView.setImageResource(R.drawable.ic_user);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                showToast(errorMessage);
            }
        });
    }

    private void saveUserChanges() {
        String name = binding.nameEditText.getText().toString().trim();
        String email = binding.emailEditText.getText().toString().trim();
        String phone = binding.phoneEditText.getText().toString().trim();

        if (!validateUserInputs(name, email, phone)) {
            return;
        }

        if (imageUtils.getAvatarUri() != null) {
            imageUtils.uploadImageToCloudinary(imageUtils.getAvatarUri(), new ImageUtils.UploadCallback() {
                @Override
                public void onSuccess(String downloadUrl) {
                    updateUserInfo(name, email, phone, downloadUrl);
                }

                @Override
                public void onFailure(String errorMessage) {
                    showToast(errorMessage);
                }
            });
        } else {
            updateUserInfo(name, email, phone, null);
        }
    }

    private boolean validateUserInputs(String name, String email, String phone) {
        if (name.isEmpty()) {
            binding.nameEditText.setError("Vui lòng nhập tên");
            binding.nameEditText.requestFocus();
            return false;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditText.setError("Vui lòng nhập email hợp lệ");
            binding.emailEditText.requestFocus();
            return false;
        }

        if (phone.isEmpty()) {
            binding.phoneEditText.setError("Vui lòng nhập số điện thoại");
            binding.phoneEditText.requestFocus();
            return false;
        }

        return true;
    }

    private void updateUserInfo(String name, String email, String phone, String imageUrl) {
        myAccountService.updateUserInfo(
                myAccountService.getCurrentUserId(),
                name,
                email,
                phone,
                imageUrl,
                new MyAccountService.UpdateCallback() {
                    @Override
                    public void onSuccess() {
                        showToast("Cập nhật hồ sơ thành công");
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        showToast(errorMessage);
                    }
                });
    }

    private void changePassword() {
        String currentPassword = binding.currentPasswordEditText.getText().toString().trim();
        String newPassword = binding.newPasswordEditText.getText().toString().trim();

        if (!validatePasswordInputs(currentPassword, newPassword)) {
            return;
        }

        myAccountService.changePassword(
                currentPassword,
                newPassword,
                new MyAccountService.UpdateCallback() {
                    @Override
                    public void onSuccess() {
                        showToast("Đổi mật khẩu thành công");
                        binding.currentPasswordEditText.setText("");
                        binding.newPasswordEditText.setText("");
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        showToast(errorMessage);
                    }
                });
    }

    private boolean validatePasswordInputs(String currentPassword, String newPassword) {
        if (currentPassword.isEmpty()) {
            binding.currentPasswordEditText.setError("Vui lòng nhập mật khẩu hiện tại");
            binding.currentPasswordEditText.requestFocus();
            return false;
        }

        if (newPassword.isEmpty()) {
            binding.newPasswordEditText.setError("Vui lòng nhập mật khẩu mới");
            binding.newPasswordEditText.requestFocus();
            return false;
        }

        if (newPassword.length() < 6) {
            binding.newPasswordEditText.setError("Mật khẩu mới phải có ít nhất 6 ký tự");
            binding.newPasswordEditText.requestFocus();
            return false;
        }

        return true;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}