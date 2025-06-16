package com.example.movieappbyjava;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class RegisterActivity extends AppCompatActivity {
    private EditText edtName, edtEmail, edtPassword, edtRePassword;
    private Button btnRegister;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Xử lý nút Sign In
        TextView txtSignIn = findViewById(R.id.txtSignIn);
        txtSignIn.setOnClickListener(v -> {
            finish(); // Quay về LoginActivity
        });

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ view
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);
        edtRePassword = findViewById(R.id.edtRePassword);

        // Xử lý đăng ký khi bấm nút
        btnRegister.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String repassword = edtRePassword.getText().toString().trim();

            if (name.isEmpty()) {
                Toasty.error(this, "Họ tên không được để trống", Toasty.LENGTH_SHORT, true).show();
                return;
            }

            if (email.isEmpty()) {
                Toasty.error(this, "Email không được để trống", Toasty.LENGTH_SHORT, true).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toasty.error(this, "Email không đúng định dạng", Toasty.LENGTH_SHORT, true).show();
                return;
            }

            if (password.isEmpty()) {
                Toasty.error(this, "Mật khẩu không được để trống", Toasty.LENGTH_SHORT, true).show();
                return;
            }

            if (password.length() < 8) {
                Toasty.error(this, "Mật khẩu phải ít nhất 8 ký tự", Toasty.LENGTH_SHORT, true).show();
                return;
            }

            if (!password.equals(repassword)) {
                Toasty.error(this, "Mật khẩu nhập lại không khớp", Toasty.LENGTH_SHORT, true).show();
                return;
            }

            registerUser(email, password);
        });
    }

    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            String name = edtName.getText().toString().trim();

                            Map<String, Object> profile = new HashMap<>();
                            profile.put("name", name);
                            profile.put("email", email);

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users").document(uid)
                                    .set(profile)
                                    .addOnSuccessListener(aVoid -> {
                                        // ✅ Tạo mã OTP
                                        String otpCode = String.valueOf((int)(Math.random() * 900000) + 100000);

                                        // ✅ Gửi email OTP bằng GmailSender
                                        new Thread(() -> {
                                            try {
                                                GmailSender sender = new GmailSender();
                                                sender.sendOTP(email, otpCode);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }).start();

                                        // ✅ Lưu OTP vào Firestore (dựa vào email hoặc uid)
                                        db.collection("otp_codes").document(email)
                                                .set(new HashMap<String, Object>() {{
                                                    put("otp", otpCode);
                                                    put("timestamp", System.currentTimeMillis());
                                                }})
                                                .addOnSuccessListener(unused -> {
                                                    Toasty.success(RegisterActivity.this, "Vui lòng kiểm tra email để xác minh OTP", Toasty.LENGTH_LONG, true).show();

                                                    // ✅ Điều hướng sang VerifyOtpActivity
                                                    Intent intent = new Intent(RegisterActivity.this, VerifyOtpActivity.class);
                                                    intent.putExtra("email", email);
                                                    startActivity(intent);
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toasty.error(RegisterActivity.this, "Lưu mã OTP thất bại: " + e.getMessage(), Toasty.LENGTH_LONG, true).show();
                                                });

                                    })
                                    .addOnFailureListener(e -> {
                                        Toasty.error(RegisterActivity.this, "Lưu thông tin người dùng thất bại: " + e.getMessage(), Toasty.LENGTH_LONG, true).show();
                                    });
                        }
                    } else {
                        Toasty.error(RegisterActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toasty.LENGTH_LONG, true).show();
                    }
                });
    }


}