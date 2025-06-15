package com.example.movieappbyjava.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.movieappbyjava.R;
import androidx.fragment.app.Fragment;

public class UserManagementFragment extends Fragment {

    public UserManagementFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_management, container, false);
    }
}
