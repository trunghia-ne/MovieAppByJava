package com.example.movieappbyjava.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.movieappbyjava.R;
import com.example.movieappbyjava.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final Context context;
    private List<User> userList;
    private List<User> userListFull; // Dùng để lưu danh sách gốc cho việc tìm kiếm

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        this.userListFull = new ArrayList<>(userList);
    }

    public void setUsers(List<User> users) {
        this.userList = users;
        this.userListFull = new ArrayList<>(users);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.username.setText(user.getUsername());
        holder.email.setText(user.getEmail());

        // Sử dụng Glide để tải ảnh
        Glide.with(context)
                .load(user.getImage())
                .placeholder(R.drawable.ic_launcher_background) // ảnh mặc định
                .error(R.drawable.ic_launcher_background) // ảnh khi lỗi
                .into(holder.avatar);

        // Xử lý sự kiện xóa
        holder.deleteButton.setOnClickListener(v -> {
            deleteUser(user, position);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    private void deleteUser(User user, int position) {
        // Giả sử mỗi user có một ID duy nhất là email
        FirebaseFirestore.getInstance().collection("users")
                .document(user.getEmail()) // Cần một ID duy nhất để xóa
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Xóa người dùng thành công", Toast.LENGTH_SHORT).show();
                    userList.remove(position);
                    userListFull.remove(user);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, userList.size());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Hàm lọc danh sách người dùng theo từ khóa tìm kiếm
    public void filter(String text) {
        userList.clear();
        if (text.isEmpty()) {
            userList.addAll(userListFull);
        } else {
            String lowerCaseQuery = text.toLowerCase();
            List<User> filteredList = userListFull.stream()
                    .filter(user -> user.getUsername().toLowerCase().contains(lowerCaseQuery) ||
                            user.getEmail().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
            userList.addAll(filteredList);
        }
        notifyDataSetChanged();
    }


    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView username, email;
        ImageButton deleteButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.iv_user_avatar);
            username = itemView.findViewById(R.id.tv_username);
            email = itemView.findViewById(R.id.tv_user_email);
            deleteButton = itemView.findViewById(R.id.btn_delete_user);
        }
    }
}
