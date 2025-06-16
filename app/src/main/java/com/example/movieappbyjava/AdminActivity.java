package com.example.movieappbyjava;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;

public class AdminActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ViewPager2 viewPager;
    private AdminViewPagerAdapter viewPagerAdapter;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.adminDrawer);
        viewPager = findViewById(R.id.adminViewPager);
        toolbar = findViewById(R.id.adminToolbar);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        viewPagerAdapter = new AdminViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        // Khi chọn item trong drawer -> chuyển tab
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_users) {
                viewPager.setCurrentItem(0);
            } else if (id == R.id.nav_revenue) {
                viewPager.setCurrentItem(1);
            } else if (id == R.id.nav_comments) { // Thêm tab quản lý bình luận
                viewPager.setCurrentItem(2);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }
}