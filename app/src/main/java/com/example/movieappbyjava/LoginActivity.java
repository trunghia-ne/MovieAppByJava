package com.example.movieappbyjava;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.movieappbyjava.network.ApiService;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 100;
    private EditText emailEditText, passwordEditText;
    private ImageView togglePassword;
    private Button googleSignInBtn;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    final boolean[] isPasswordVisible = {false};

    FrameLayout loginFrame;
    TextView loginBtnText;
    ProgressBar loginProgress;

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
        googleSignInBtn = findViewById(R.id.googleSignInBtn);
        togglePassword = findViewById(R.id.togglePassword);
        loginFrame = findViewById(R.id.loginFrame);
        loginBtnText = findViewById(R.id.loginBtnText);
        loginProgress = findViewById(R.id.LoginProgress);

        mAuth = FirebaseAuth.getInstance();

        togglePassword.setOnClickListener(v -> {
            isPasswordVisible[0] = !isPasswordVisible[0];
            if (isPasswordVisible[0]) {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePassword.setImageResource(R.drawable.ic_eye_open);
            } else {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePassword.setImageResource(R.drawable.ic_eye_closed);
            }
            passwordEditText.setSelection(passwordEditText.getText().length());
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        loginFrame.setOnClickListener(v -> {
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

            loginBtnText.setVisibility(View.INVISIBLE);
            loginProgress.setVisibility(View.VISIBLE);
            loginFrame.setEnabled(false);

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
    }

    private void loginWithEmailPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    loginBtnText.setVisibility(View.VISIBLE);
                    loginProgress.setVisibility(View.GONE);
                    loginFrame.setEnabled(true);

                    if (task.isSuccessful()) {
                            FirebaseFirestore.getInstance().collection("users")
                                    .whereEqualTo("email", email)
                                    .get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        if (!querySnapshot.isEmpty()) {
                                            DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                                            String role = doc.getString("role");
                                            if ("admin".equals(role)) {
                                                startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                                            } else {
                                                startActivity(new Intent(LoginActivity.this, MainNavigationActivity.class));
                                            }
                                            finish();
                                        } else {
                                            Toasty.error(this, "Không tìm thấy người dùng trong Firestore.", Toasty.LENGTH_LONG, true).show();
                                        }
                                    });

                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthInvalidUserException) {
                            Toasty.error(this, "Tài khoản không tồn tại hoặc đã bị xoá.", Toasty.LENGTH_LONG, true).show();
                        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            Toasty.error(this, "Tài khoản hoặc mật khẩu không đúng.", Toasty.LENGTH_LONG, true).show();
                        } else {
                            Toasty.error(this, "Đăng nhập thất bại: " + e.getLocalizedMessage(), Toasty.LENGTH_LONG, true).show();
                        }
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
                        startActivity(new Intent(this, MainNavigationActivity.class));
                        finish();
                    } else {
                        Toasty.error(this, "Lỗi Firebase: " + task.getException().getMessage(), Toasty.LENGTH_LONG, true).show();
                    }
                });
    }
}
