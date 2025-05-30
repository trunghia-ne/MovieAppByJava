package com.example.movieappbyjava;

import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class WatchActivity extends AppCompatActivity {
    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_watch);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        webView = findViewById(R.id.webView);

        // Lấy link từ Intent
        String videoUrl = getIntent().getStringExtra("video_url");
        if (videoUrl != null) {
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient());
            webView.loadUrl(videoUrl);
        }
    }

    public static class PaymentResultActivity extends AppCompatActivity {
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
}