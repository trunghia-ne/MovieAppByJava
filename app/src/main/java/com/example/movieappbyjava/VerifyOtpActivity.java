package com.example.movieappbyjava;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import es.dmoral.toasty.Toasty;

public class VerifyOtpActivity extends AppCompatActivity {

    private EditText edtOtp;
    private Button btnVerify;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        edtOtp = findViewById(R.id.edtOtp);
        btnVerify = findViewById(R.id.btnVerify);

        // Lấy email từ intent
        email = getIntent().getStringExtra("email");

        btnVerify.setOnClickListener(v -> {
            String inputOtp = edtOtp.getText().toString().trim();

            if (inputOtp.isEmpty()) {
                Toasty.error(this, "Vui lòng nhập mã OTP", Toasty.LENGTH_SHORT, true).show();
                return;
            }

            // Lấy OTP từ Firestore để so sánh
            FirebaseFirestore.getInstance()
                    .collection("otp_codes")
                    .document(email)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String correctOtp = doc.getString("otp");

                            if (inputOtp.equals(correctOtp)) {
                                Toasty.success(this, "Xác minh thành công!", Toasty.LENGTH_SHORT, true).show();

                                // ✅ Chuyển sang màn hình chính
                                startActivity(new Intent(this, MainNavigationActivity.class));
                                finish();

                                // Xoá OTP sau khi xác minh (không bắt buộc)
                                FirebaseFirestore.getInstance()
                                        .collection("otp_codes")
                                        .document(email)
                                        .delete();

                            } else {
                                Toasty.error(this, "Mã OTP không chính xác", Toasty.LENGTH_SHORT, true).show();
                            }
                        } else {
                            Toasty.error(this, "Không tìm thấy mã OTP. Vui lòng thử lại.", Toasty.LENGTH_LONG, true).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toasty.error(this, "Lỗi xác minh OTP: " + e.getMessage(), Toasty.LENGTH_LONG, true).show();
                    });
        });
    }
}
