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

public class ProfileActivity extends AppCompatActivity {
    String[] menuItems = {"My Account", "Your Favorites", "History Watched", "Logout"};
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
                    // startActivity(new Intent(...));
                    break;
                case "Logout":
                    finish(); // hoặc chuyển về LoginActivity
                    break;
            }
        });

    }
}