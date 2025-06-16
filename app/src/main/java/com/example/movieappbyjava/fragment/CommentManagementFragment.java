package com.example.movieappbyjava.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentManagementFragment extends Fragment implements AdminCommentAdapter.OnToggleHideClickListener {

    private RecyclerView recyclerComments;
    private AdminCommentAdapter commentAdapter;
    private List<Comment> commentsList = new ArrayList<>();
    private ProgressBar progressBarLoading;
    private TextView textCommentsHeader;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment_management, container, false);

        recyclerComments = view.findViewById(R.id.recyclerAdminComments);
        progressBarLoading = view.findViewById(R.id.progressBarLoading);
        textCommentsHeader = view.findViewById(R.id.textCommentsHeader);

        // Configure RecyclerView
        recyclerComments.setLayoutManager(new LinearLayoutManager(getContext()));
        commentAdapter = new AdminCommentAdapter(commentsList, this);
        recyclerComments.setAdapter(commentAdapter);

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
                    commentAdapter.notifyDataSetChanged();
                    textCommentsHeader.setText("Quản lý bình luận (" + commentsList.size() + ")");
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
                                loadComments();
                            } else {
                                Toast.makeText(getContext(), "Lỗi khi " + (isHidden ? "hiện" : "ẩn") + " bình luận", Toast.LENGTH_SHORT).show();
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