package com.example.movieappbyjava.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.movieappbyjava.MyAccountActivity;
import com.example.movieappbyjava.ProfileAdapter;
import com.example.movieappbyjava.R;
import com.example.movieappbyjava.model.ApiClient;
import com.example.movieappbyjava.model.User;
import com.example.movieappbyjava.network.UserApi;
import com.google.firebase.auth.FirebaseAuth;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private TextView tvUserName;
    private TextView tvPhone;

    private final String[] menuItems = {"My Account", "Your Favorites", "History Watched", "Logout"};
    private final int[] icons = {
            R.drawable.ic_user,
            R.drawable.ic_favorite,
            R.drawable.ic_movie,
            R.drawable.ic_logout
    };

    private UserApi userApi;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvUserName = view.findViewById(R.id.tvUserName);
        tvPhone = view.findViewById(R.id.tvPhone);
        ListView listView = view.findViewById(R.id.profileList);

        ProfileAdapter adapter = new ProfileAdapter(requireContext(), menuItems, icons);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((adapterView, itemView, position, id) -> {
            String item = menuItems[position];
            switch (item) {
                case "My Account":
                    startActivity(new Intent(requireContext(), MyAccountActivity.class));
                    break;
                case "Logout":
                    FirebaseAuth.getInstance().signOut();
                    requireActivity().finish(); // hoặc chuyển sang LoginActivity nếu cần
                    break;
            }
        });

        loadUserInfo();
        return view;
    }

    private void loadUserInfo() {
        userApi = ApiClient.getUserApi();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Call<User> call = userApi.getUserById(userId);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    tvUserName.setText(user.getUsername());
                    tvPhone.setText(user.getPhone());
                } else {
                    tvUserName.setText("Tên người dùng");
                    tvPhone.setText("Không rõ");
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.e("PROFILE_FRAGMENT", "Lỗi tải thông tin: " + t.getMessage(), t);
                tvUserName.setText("Tên người dùng");
                tvPhone.setText("Không rõ");
            }
        });
    }
}
