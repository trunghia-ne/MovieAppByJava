package com.example.movieappbyjava;

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
    private EditText edtName, edtEmail, edtPassword;
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

        // Xử lý đăng ký khi bấm nút
        btnRegister.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (name.isEmpty()) {
                edtName.setError("Họ tên không được để trống");
                return;
            }
            if (email.isEmpty()) {
                edtEmail.setError("Email không được để trống");
                return;
            }
            if (password.isEmpty()) {
                edtPassword.setError("Mật khẩu không được để trống");
                return;
            }
            if (password.length() < 6) {
                edtPassword.setError("Mật khẩu phải ít nhất 6 ký tự");
                return;
            }

            // Gọi hàm đăng ký Firebase
            registerUser(email, password);
        });
    }

    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Lấy uid của user mới tạo
                            String uid = user.getUid();

                            // Tạo data profile mở rộng
                            Map<String, Object> profile = new HashMap<>();
                            profile.put("name", edtName.getText().toString().trim());
                            profile.put("email", email);
                            // Nếu có số điện thoại, địa chỉ bạn cũng có thể thêm tương tự
                            // profile.put("phone", "0123456789");
                            // profile.put("address", "Địa chỉ...");

                            // Lưu dữ liệu profile vào Firestore
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users").document(uid)
                                    .set(profile)
                                    .addOnSuccessListener(aVoid -> {
                                        Toasty.success(RegisterActivity.this, "Đăng ký thành công!", Toasty.LENGTH_SHORT, true).show();
                                        finish(); // Quay về LoginActivity
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