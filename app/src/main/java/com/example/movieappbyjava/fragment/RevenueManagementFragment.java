package com.example.movieappbyjava.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.movieappbyjava.R;
import androidx.fragment.app.Fragment;

public class RevenueManagementFragment extends Fragment {

    public RevenueManagementFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_revenue_management, container, false);
    }
}
