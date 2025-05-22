package com.example.movieappbyjava;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;

import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {
    TextView registerNow;
    private EditText emailEditText, passwordEditText;
    private Button loginBtn;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginBtn = findViewById(R.id.loginBtn);

        mAuth = FirebaseAuth.getInstance();  // Khởi tạo FirebaseAuth

        loginBtn.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if(email.isEmpty()) {
                Toasty.error(LoginActivity.this, "Email không được để trống", Toasty.LENGTH_SHORT, true).show();
                return;
            }

            if(password.isEmpty()) {
                Toasty.error(LoginActivity.this, "Mật khẩu không được để trống", Toasty.LENGTH_SHORT, true).show();
                return;
            }

            // Nếu hợp lệ thì gọi đăng nhập Firebase
            loginWithEmailPassword(email, password);
        });


        TextView signUpText = findViewById(R.id.signUpText);
        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginWithEmailPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toasty.success(LoginActivity.this, "Đăng nhập thành công!", Toasty.LENGTH_SHORT, true).show();
                        Intent intent = new Intent(LoginActivity.this, HomeActaivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toasty.error(LoginActivity.this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toasty.LENGTH_LONG, true).show();
                    }
                });
    }
}