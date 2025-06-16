package com.example.movieappbyjava.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieappbyjava.R;
import com.example.movieappbyjava.adapter.UserAdapter;
import com.example.movieappbyjava.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserManagementFragment extends Fragment {

    private static final String TAG = "UserManagementFragment";
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private FirebaseFirestore db;
    private EditText etSearch;
    private Button btnAddUser;

    public UserManagementFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Kiểm tra kết nối đến Firestore
        db.collection("người sử dụng").limit(1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "✅ Kết nối Firestore thành công! Có thể truy vấn dữ liệu.");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Không thể kết nối Firestore hoặc truy vấn thất bại: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Không thể kết nối Firestore!", Toast.LENGTH_LONG).show();
                });

        // Ánh xạ View
        recyclerView = view.findViewById(R.id.rv_user_list);
        etSearch = view.findViewById(R.id.et_search_users);
        btnAddUser = view.findViewById(R.id.btn_add_user);

        // Cài đặt RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), userList);
        recyclerView.setAdapter(userAdapter);

        // Tải dữ liệu người dùng từ Firestore
        loadUsers();

        // Thiết lập tìm kiếm người dùng
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                userAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Xử lý sự kiện nút thêm người dùng
        btnAddUser.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chức năng thêm người dùng sẽ được cài đặt", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadUsers() {
        Log.d(TAG, "🔄 Bắt đầu tải dữ liệu từ collection 'người sử dụng'...");
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "📥 Tác vụ get() đã hoàn tất.");
                    if (task.isSuccessful()) {
                        Log.d(TAG, "✅ Tác vụ THÀNH CÔNG.");

                        int documentCount = task.getResult().size();
                        Log.d(TAG, "📄 Số lượng document tìm thấy: " + documentCount);

                        if (documentCount == 0) {
                            Toast.makeText(getContext(), "Không tìm thấy document nào trong collection.", Toast.LENGTH_LONG).show();
                        }

                        userList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, "➡️ Đang xử lý document ID: " + document.getId());
                            Log.d(TAG, "📄 Dữ liệu document: " + document.getData().toString());

                            try {
                                User user = document.toObject(User.class);
                                userList.add(user);
                                Log.d(TAG, "✅ Chuyển đổi thành công. Username: " + user.getUsername() + ", Email: " + user.getEmail());
                            } catch (Exception e) {
                                Log.e(TAG, "❌ Lỗi khi chuyển đổi document thành User object: ", e);
                            }
                        }

                        userAdapter.setUsers(userList);
                        Log.d(TAG, "🔁 Đã cập nhật adapter với " + userList.size() + " users.");

                    } else {
                        Log.e(TAG, "❌ Tác vụ THẤT BẠI. Lỗi: ", task.getException());
                        Toast.makeText(getContext(), "Lỗi khi tải dữ liệu: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
