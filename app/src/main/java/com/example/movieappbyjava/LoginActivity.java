package com.example.movieappbyjava;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.movieappbyjava.network.ApiService;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 100;

    private EditText emailEditText, passwordEditText;
    private Button loginBtn;
    private Button googleSignInBtn;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

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
        googleSignInBtn = findViewById(R.id.googleSignInBtn); // nút Google

        mAuth = FirebaseAuth.getInstance();

        // Google Sign-In config
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // tự động có từ google-services.json
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        loginBtn.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty()) {
                Toasty.error(this, "Email không được để trống", Toasty.LENGTH_SHORT, true).show();
                return;
            }
            if (password.isEmpty()) {
                Toasty.error(this, "Mật khẩu không được để trống", Toasty.LENGTH_SHORT, true).show();
                return;
            }

            loginWithEmailPassword(email, password);
        });

        googleSignInBtn.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        findViewById(R.id.signUpText).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        findViewById(R.id.forgotPasswordText).setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                Toasty.error(this, "Vui lòng nhập email để đặt lại mật khẩu", Toasty.LENGTH_SHORT, true).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toasty.success(this, "Email đặt lại mật khẩu đã được gửi!", Toasty.LENGTH_LONG, true).show();
                        } else {
                            Toasty.error(this, "Gửi thất bại: " + task.getException().getMessage(), Toasty.LENGTH_LONG, true).show();
                        }
                    });
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/") // localhost của máy phát triển với Android Emulator
                .addConverterFactory(ScalarsConverterFactory.create()) // Vì trả về String thuần
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        apiService.getHello().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()) {
                    String message = response.body();
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "API call failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void loginWithEmailPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(this, HomeActaivity.class));
                        finish();
                    } else {
                        Toasty.error(this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toasty.LENGTH_LONG, true).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toasty.error(this, "Đăng nhập Google thất bại: " + e.getMessage(), Toasty.LENGTH_LONG, true).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toasty.success(this, "Đăng nhập Google thành công!", Toasty.LENGTH_SHORT, true).show();
                        startActivity(new Intent(this, HomeActaivity.class));
                        finish();
                    } else {
                        Toasty.error(this, "Lỗi Firebase: " + task.getException().getMessage(), Toasty.LENGTH_LONG, true).show();
                    }
                });
    }
}
