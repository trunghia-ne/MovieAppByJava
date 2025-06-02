package com.example.movieappbyjava.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

    private final String[] menuItems = {"My Account", "History Watched", "Logout"};
    private final int[] icons = {
            R.drawable.ic_user,
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

        Log.d("UserApi", "Fetching user data for userId: " + userId);

        Call<User> call = userApi.getUserById(userId);
        Log.d("UserApi", "Request URL: " + call.request().url()); // Di chuyển log URL vào đây

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                Log.d("UserApi", "Response received, code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    Log.i("UserApi", "User data retrieved: " + user.getUsername() + ", Phone: " + user.getPhone());
                    tvUserName.setText(user.getUsername());
                    tvPhone.setText(user.getPhone());
                } else {
                    Log.w("UserApi", "Failed to retrieve user data. Response code: " + response.code() + ", Message: " + response.message());
                    tvUserName.setText("Tên người dùng");
                    tvPhone.setText("Không rõ");
                    Toast.makeText(requireContext(), "Không tìm thấy dữ liệu người dùng!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.e("UserApi", "API call failed: " + t.getMessage(), t);
                tvUserName.setText("Tên người dùng");
                tvPhone.setText("Không rõ");
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}