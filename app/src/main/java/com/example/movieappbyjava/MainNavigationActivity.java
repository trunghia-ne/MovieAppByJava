package com.example.movieappbyjava;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.movieappbyjava.fragment.CategoryFragment;
import com.example.movieappbyjava.fragment.FavoritesFragment;
import com.example.movieappbyjava.fragment.HomeFragment;
import com.example.movieappbyjava.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainNavigationActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    // Khai báo các Fragment chính
    private final HomeFragment homeFragment = new HomeFragment();
    private final CategoryFragment categoryFragment = new CategoryFragment();
    private final FavoritesFragment favoritesFragment = new FavoritesFragment();
    private final ProfileFragment profileFragment = new ProfileFragment();

    private Fragment activeFragment = homeFragment;
    private Fragment previousFragment = null; // dùng cho quay lại từ category, favorites...

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();

        // Nếu đang ở fragment con (CateMovieFragment) thì pop về Category
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
            return;
        }

        // Nếu đang ở Favorites hoặc Category → quay lại previousFragment
        if ((activeFragment == favoritesFragment || activeFragment == categoryFragment) && previousFragment != null) {
            switchFragment(previousFragment);
            syncBottomNavWithFragment(previousFragment);
            return;
        }

        // Nếu đang ở fragment khác mà không phải Home → về Home
        if (activeFragment != homeFragment) {
            switchFragment(homeFragment);
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
            return;
        }

        // Mặc định: đóng app
        super.onBackPressed();
    }

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

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String imageUrl = documentSnapshot.getString("image");
                            Log.d("MainNav", "UID = " + uid);
                            Log.d("MainNav", "imageUrl = " + imageUrl);
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(this)
                                        .asBitmap()
                                        .load(imageUrl)
                                        .override(dpToPx(24), dpToPx(24))
                                        .circleCrop()
                                        .into(new CustomTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                // Tạo ImageSpan từ Bitmap
                                                Drawable d = new BitmapDrawable(getResources(), resource);
                                                d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                                                ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);

                                                SpannableString sb = new SpannableString(" ");
                                                sb.setSpan(span, 0, 1, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

                                                bottomNavigationView.getMenu().findItem(R.id.nav_profile).setTitle(sb);
                                                Log.d("MainNav", "Avatar set as title successfully");
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {}
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // fallback nếu lỗi xảy ra
                        e.printStackTrace();
                    });
        }


        // Add tất cả fragment 1 lần duy nhất
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentContainer, profileFragment, "profile").hide(profileFragment)
                .add(R.id.fragmentContainer, favoritesFragment, "favorites").hide(favoritesFragment)
                .add(R.id.fragmentContainer, categoryFragment, "category").hide(categoryFragment)
                .add(R.id.fragmentContainer, homeFragment, "home")
                .commit();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                switchFragment(homeFragment);
                return true;
            } else if (id == R.id.nav_search) {
                previousFragment = activeFragment;
                switchFragment(categoryFragment);
                return true;
            } else if (id == R.id.nav_favorite) {
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

    private void syncBottomNavWithFragment(Fragment fragment) {
        if (fragment == homeFragment) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        } else if (fragment == categoryFragment) {
            bottomNavigationView.setSelectedItemId(R.id.nav_search);
        } else if (fragment == favoritesFragment) {
            bottomNavigationView.setSelectedItemId(R.id.nav_favorite);
        } else if (fragment == profileFragment) {
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        }
    }

    public void navigateToFavorites() {
        previousFragment = activeFragment;
        switchFragment(favoritesFragment);
        bottomNavigationView.setSelectedItemId(R.id.nav_favorite);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public void backFromFavorites() {
        if (previousFragment != null) {
            switchFragment(previousFragment);
            syncBottomNavWithFragment(previousFragment);
        }
    }
}
