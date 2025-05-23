package com.example.movieappbyjava;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {
    String[] menuItems = {"My Account", "Your Favorites", "History Watched", "Logout"};
    private TextView tvUserName;
    int[] icons = {
            R.drawable.ic_user,
            R.drawable.ic_favorite,
            R.drawable.ic_movie,
            R.drawable.ic_logout
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                    finish(); // hoặc chuyển về LoginActivity
                    break;
            }
        });

        tvUserName = findViewById(R.id.tvUserName);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        String name = (doc.exists() && doc.contains("name")) ? doc.getString("name") : "Tên người dùng";
                        tvUserName.setText(name);
                    })
                    .addOnFailureListener(e -> tvUserName.setText("Tên người dùng"));
        } else {
            tvUserName.setText("Tên người dùng");
        }
    }
}