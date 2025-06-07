package com.example.movieappbyjava;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.movieappbyjava.fragment.CategoryFragment;
import com.example.movieappbyjava.fragment.FavoritesFragment;
import com.example.movieappbyjava.fragment.HomeFragment;
import com.example.movieappbyjava.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainNavigationActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    // Khai báo các Fragment
    private final HomeFragment homeFragment = new HomeFragment();
    private final CategoryFragment categoryFragment = new CategoryFragment();
    private final FavoritesFragment favoritesFragment = new FavoritesFragment();
    private final ProfileFragment profileFragment = new ProfileFragment();

    private Fragment activeFragment = homeFragment;
    private Fragment previousFragment = null;  // để quay lại từ Favorites
    String avatarUrl = "https://static.vecteezy.com/system/resources/previews/018/765/757/original/user-profile-icon-in-flat-style-member-avatar-illustration-on-isolated-background-human-permission-sign-business-concept-vector.jpg";

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_navigation);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        Glide.with(this)
                .asBitmap()
                .load(avatarUrl)
                .override(dpToPx(24), dpToPx(24)) // Resize ảnh về 24dp
                .circleCrop()
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Drawable circularDrawable = new BitmapDrawable(getResources(), resource);
                        bottomNavigationView.getMenu().findItem(R.id.nav_profile).setIcon(circularDrawable);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });

        // Thêm tất cả Fragment chỉ 1 lần duy nhất
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentContainer, profileFragment, "profile").hide(profileFragment)
                .add(R.id.fragmentContainer, favoritesFragment, "favorites").hide(favoritesFragment)
                .add(R.id.fragmentContainer, categoryFragment, "category").hide(categoryFragment)
                .add(R.id.fragmentContainer, homeFragment, "home") // Home là active mặc định
                .commit();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                switchFragment(homeFragment);
                return true;
            } else if (id == R.id.nav_search) {
                switchFragment(categoryFragment);
                return true;
            } else if (id == R.id.nav_favorite) {
                // Ghi nhớ fragment hiện tại để khi back có thể quay lại
                previousFragment = activeFragment;
                switchFragment(favoritesFragment);
                return true;
            } else if (id == R.id.nav_profile) {
                switchFragment(profileFragment);
                return true;
            }

            return false;
        });
    }

    private void switchFragment(Fragment target) {
        if (target != activeFragment) {
            getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .show(target)
                    .commit();
            activeFragment = target;
        }
    }

    // Gọi từ FavoritesFragment khi người dùng nhấn nút back
    public void backFromFavorites() {
        if (previousFragment != null) {
            switchFragment(previousFragment);

            // Đồng bộ icon bottom nav
            if (previousFragment == homeFragment) {
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            } else if (previousFragment == profileFragment) {
                bottomNavigationView.setSelectedItemId(R.id.nav_profile);
            } else if (previousFragment == categoryFragment) {
                bottomNavigationView.setSelectedItemId(R.id.nav_search);
            }
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public void navigateToFavorites() {
        previousFragment = activeFragment; // nếu cần quay lại từ Favorites
        switchFragment(favoritesFragment);
        bottomNavigationView.setSelectedItemId(R.id.nav_favorite); // đồng bộ nút được chọn
    }
}
