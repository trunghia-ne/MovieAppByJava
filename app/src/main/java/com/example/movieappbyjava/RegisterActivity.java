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
                        Toasty.success(RegisterActivity.this, "Đăng ký thành công!", Toasty.LENGTH_SHORT, true).show();
                        finish(); // Quay lại LoginActivity hoặc bạn có thể tự động đăng nhập luôn
                    } else {
                        Toasty.error(RegisterActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toasty.LENGTH_LONG, true).show();
                    }
                });
    }
}