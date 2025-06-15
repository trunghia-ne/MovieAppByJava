package com.example.movieappbyjava;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.movieappbyjava.fragment.RevenueManagementFragment;
import com.example.movieappbyjava.fragment.UserManagementFragment;

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
            default:
                return new UserManagementFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
