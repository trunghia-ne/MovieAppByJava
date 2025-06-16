package com.example.movieappbyjava;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.movieappbyjava.fragment.RevenueManagementFragment;
import com.example.movieappbyjava.fragment.UserManagementFragment;
import com.example.movieappbyjava.fragment.CommentManagementFragment; // Tạo mới fragment này

import androidx.annotation.NonNull;

public class AdminViewPagerAdapter extends FragmentStateAdapter {

    public AdminViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new UserManagementFragment();
            case 1:
                return new RevenueManagementFragment();
            case 2:
                return new CommentManagementFragment(); // Thêm fragment quản lý bình luận
            default:
                return new UserManagementFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Tăng lên 3 tab
    }
}