package com.example.movieappbyjava;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.movieappbyjava.model.ApiClient;
import com.example.movieappbyjava.model.User;
import com.example.movieappbyjava.network.UserApi;
import com.google.firebase.auth.FirebaseAuth;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    String[] menuItems = {"My Account", "Your Favorites", "History Watched", "Logout"};
    private TextView tvUserName;

    int[] icons = {
            R.drawable.ic_user,
            R.drawable.ic_favorite,
            R.drawable.ic_movie,
            R.drawable.ic_logout
    };

    private UserApi userApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MyAccount", "MyAccountActivity started");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ListView listView = findViewById(R.id.profileList);
        ProfileAdapter adapter = new ProfileAdapter(this, menuItems, icons);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((adapterView, view, position, id) -> {
            String item = menuItems[position];
            switch (item) {
                case "My Account":
                    startActivity(new Intent(ProfileActivity.this, MyAccountActivity.class));
                    break;
                case "Your Favorites":
                    startActivity(new Intent(ProfileActivity.this, Favorites.class));
                    break;
                case "Logout":
                    FirebaseAuth.getInstance().signOut();
                    finish(); // hoặc chuyển sang LoginActivity
                    break;
            }
        });

        TextView tvUserName = findViewById(R.id.tvUserName);
        TextView tvPhone = findViewById(R.id.tvPhone); // thêm dòng này

        userApi = ApiClient.getUserApi();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Call<User> call = userApi.getUserById(userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    tvUserName.setText(user.getUsername());
                    tvPhone.setText(user.getPhone()); // cập nhật số điện thoại từ API
                } else {
                    tvUserName.setText("Tên người dùng");
                    tvPhone.setText("Không rõ");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                tvUserName.setText("Tên người dùng");
                tvPhone.setText("Không rõ");
            }
        });
    }
}
