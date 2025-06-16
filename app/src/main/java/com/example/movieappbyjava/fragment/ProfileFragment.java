package com.example.movieappbyjava.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.movieappbyjava.MainNavigationActivity;
import com.example.movieappbyjava.MyAccountActivity;
import com.example.movieappbyjava.R;
import com.example.movieappbyjava.adapter.ProfileAdapter;
import com.example.movieappbyjava.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private TextView tvUserName;
    private TextView tvPhone;
    private ImageView imgAvatar;

    private final String[] menuItems = {"My Account", "Collections", "History Watched", "Logout"};
    private final int[] icons = {
            R.drawable.ic_user,
            R.drawable.ic_favorite,
            R.drawable.ic_movie,
            R.drawable.ic_logout
    };

    public ProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvUserName = view.findViewById(R.id.tvUserName);
        tvPhone = view.findViewById(R.id.tvPhone);
        imgAvatar = view.findViewById(R.id.imgAvatar); // Gán ID ảnh

        ListView listView = view.findViewById(R.id.profileList);
        ProfileAdapter adapter = new ProfileAdapter(requireContext(), menuItems, icons);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((adapterView, itemView, position, id) -> {
            switch (menuItems[position]) {
                case "My Account":
                    startActivity(new Intent(requireContext(), MyAccountActivity.class));
                    break;
                case "History Watched":
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, new WatchHistoryFragment())
                            .addToBackStack(null)
                            .commit();
                    break;
                case "Collections":
                    if (getActivity() instanceof MainNavigationActivity) {
                        ((MainNavigationActivity) getActivity()).navigateToFavorites();
                    }
                    break;
                case "Logout":
                    FirebaseAuth.getInstance().signOut();
                    requireActivity().finish();
                    break;
            }
        });

        loadUserInfo();
        return view;
    }

    private void loadUserInfo() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            tvUserName.setText(user.getUsername());
                            tvPhone.setText(user.getPhone());

                            // Load avatar từ Cloudinary
                            if (user.getImage() != null && !user.getImage().isEmpty()) {
                                Log.d("ProfileFragment", "Loading image URL: " + user.getImage());
                                Glide.with(requireContext())
                                        .load(user.getImage())
                                        .circleCrop()
                                        .placeholder(R.drawable.ic_user)
                                        .into(imgAvatar);
                            } else {
                                imgAvatar.setImageResource(R.drawable.ic_user);
                            }


                            Log.d("UserFirestore", "User loaded: " + user.getUsername());
                        }
                    } else {
                        showDefaultInfo("Không tìm thấy dữ liệu người dùng!");
                    }
                })
                .addOnFailureListener(e -> {
                    showDefaultInfo("Lỗi kết nối đến Firestore!");
                    Log.e("UserFirestore", "Lỗi khi lấy dữ liệu: ", e);
                });
    }

    private void showDefaultInfo(String message) {
        tvUserName.setText("Tên người dùng");
        tvPhone.setText("Không rõ");
        imgAvatar.setImageResource(R.drawable.ic_user);
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
