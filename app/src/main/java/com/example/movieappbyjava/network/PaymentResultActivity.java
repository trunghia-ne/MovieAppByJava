package com.example.movieappbyjava.network;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PaymentResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri data = getIntent().getData();
        if (data != null && "movieapp".equals(data.getScheme())) {
            String host = data.getHost();
            if ("payment-success".equals(host)) {
                Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_LONG).show();
                // TODO: mở màn hình xem phim hoặc quay về trang chính
            } else if ("payment-failed".equals(host)) {
                Toast.makeText(this, "Thanh toán thất bại!", Toast.LENGTH_LONG).show();
            }
        }

        // Kết thúc activity nếu không có giao diện riêng
        finish();
    }
}

