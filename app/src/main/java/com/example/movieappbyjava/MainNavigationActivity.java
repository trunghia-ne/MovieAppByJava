package com.example.movieappbyjava;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

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
}
