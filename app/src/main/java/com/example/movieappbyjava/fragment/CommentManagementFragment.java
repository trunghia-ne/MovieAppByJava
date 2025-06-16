package com.example.movieappbyjava.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieappbyjava.R;
import com.example.movieappbyjava.adapter.AdminCommentAdapter;
import com.example.movieappbyjava.model.ApiClient;
import com.example.movieappbyjava.model.Comment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors; // Thêm import này

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentManagementFragment extends Fragment implements AdminCommentAdapter.OnToggleHideClickListener {

    private RecyclerView recyclerComments;
    private AdminCommentAdapter commentAdapter;
    private List<Comment> commentsList = new ArrayList<>();
    private List<Comment> filteredCommentsList = new ArrayList<>();
    private ProgressBar progressBarLoading;
    private TextView textCommentsHeader;
    private EditText editTextFilter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment_management, container, false);

        recyclerComments = view.findViewById(R.id.recyclerAdminComments);
        progressBarLoading = view.findViewById(R.id.progressBarLoading);
        textCommentsHeader = view.findViewById(R.id.textCommentsHeader);
        editTextFilter = view.findViewById(R.id.editTextFilter);

        // Configure RecyclerView
        recyclerComments.setLayoutManager(new LinearLayoutManager(getContext()));
        commentAdapter = new AdminCommentAdapter(filteredCommentsList, this);
        recyclerComments.setAdapter(commentAdapter);

        // Add filter listener
        editTextFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterComments(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadComments();

        return view;
    }

    private void loadComments() {
        progressBarLoading.setVisibility(View.VISIBLE);
        ApiClient.getApiService().getAllComments().enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                progressBarLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    commentsList.clear();
                    commentsList.addAll(response.body());
                    filterComments(editTextFilter.getText().toString()); // Lọc ngay sau khi tải
                } else {
                    Toast.makeText(getContext(), "Lỗi tải bình luận", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                progressBarLoading.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterComments(String query) {
        filteredCommentsList.clear();
        if (query.isEmpty()) {
            filteredCommentsList.addAll(commentsList.stream()
                    .filter(comment -> !comment.isHidden()) // Chỉ lấy bình luận không bị ẩn
                    .collect(Collectors.toList())); // Thay toList() bằng collect(Collectors.toList())
        } else {
            String lowerCaseQuery = query.toLowerCase();
            filteredCommentsList.addAll(commentsList.stream()
                    .filter(comment -> !comment.isHidden() && comment.getMovieTitle().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList())); // Thay toList() bằng collect(Collectors.toList())
        }
        commentAdapter.notifyDataSetChanged();
        textCommentsHeader.setText("Quản lý bình luận (" + filteredCommentsList.size() + ")");
    }

    @Override
    public void onToggleHideClick(String commentId, boolean isHidden) {
        new AlertDialog.Builder(requireContext())
                .setTitle(isHidden ? "Hiện bình luận" : "Ẩn bình luận")
                .setMessage("Bạn có chắc chắn muốn " + (isHidden ? "hiện" : "ẩn") + " bình luận này?")
                .setPositiveButton("Có", (dialog, which) -> {
                    progressBarLoading.setVisibility(View.VISIBLE);
                    Call<Void> call = isHidden ? ApiClient.getApiService().showComment(commentId) : ApiClient.getApiService().hideComment(commentId);
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            progressBarLoading.setVisibility(View.GONE);
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Bình luận đã được " + (isHidden ? "hiện" : "ẩn"), Toast.LENGTH_SHORT).show();
                                loadComments(); // Tải lại để cập nhật
                            } else {
                                Toast.makeText(getContext(), "Lỗi khi " + (isHidden ? "hiện" : "ẩn") + " bình luận: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            progressBarLoading.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}