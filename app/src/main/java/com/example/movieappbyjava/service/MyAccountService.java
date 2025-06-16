package com.example.movieappbyjava.service;

import android.content.Context;
import android.widget.Toast;

import com.example.movieappbyjava.model.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MyAccountService {
    private static final String TAG = "MyAccountService";
    private static final String USERS_COLLECTION = "users";

    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final Context context;

    public MyAccountService(Context context) {
        this.context = context;
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onFailure(String errorMessage);
    }

    public interface UpdateCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public boolean isUserLoggedIn() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            showToast("Vui lòng đăng nhập để tiếp tục");
            return false;
        }
        return true;
    }

    public String getCurrentUserId() {
        FirebaseUser currentUser = auth.getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

    public void fetchUserData(String userId, UserCallback callback) {
        firestore.collection(USERS_COLLECTION).document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            callback.onSuccess(user);
                        } else {
                            callback.onFailure("Không tìm thấy dữ liệu người dùng");
                        }
                    } else {
                        callback.onFailure("Không tìm thấy dữ liệu người dùng");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Lỗi tải dữ liệu: " + e.getMessage()));
    }

    public void updateUserInfo(String userId, String name, String email, String phone, String imageUrl, UpdateCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", name);
        updates.put("email", email);
        updates.put("phone", phone);
        if (imageUrl != null) {
            updates.put("image", imageUrl);
        }

        firestore.collection(USERS_COLLECTION).document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Cập nhật thất bại: " + e.getMessage()));
    }

    public void changePassword(String currentPassword, String newPassword, UpdateCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            callback.onFailure("Lỗi xác thực người dùng");
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
        user.reauthenticate(credential)
                .addOnSuccessListener(unused -> updatePassword(user, newPassword, callback))
                .addOnFailureListener(e -> callback.onFailure("Xác thực thất bại: " + e.getMessage()));
    }

    private void updatePassword(FirebaseUser user, String newPassword, UpdateCallback callback) {
        user.updatePassword(newPassword)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Đổi mật khẩu thất bại: " + e.getMessage()));
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}