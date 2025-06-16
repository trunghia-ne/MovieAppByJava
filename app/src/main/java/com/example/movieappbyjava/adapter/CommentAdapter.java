package com.example.movieappbyjava.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.movieappbyjava.R;
import com.example.movieappbyjava.model.ApiClient;
import com.example.movieappbyjava.model.Comment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private static final int MAX_REPLY_LENGTH = 200; // Giới hạn độ dài phản hồi
    private List<Comment> comments;
    private final String currentUserId;
    private final String movieSlug;
    private final String movieTitle;
    private Set<String> expandedComments = new HashSet<>();
    private Map<String, List<Comment>> repliesCache = new HashMap<>();

    public CommentAdapter(List<Comment> comments, String currentUserId, String movieSlug, String movieTitle) {
        this.comments = comments != null ? new ArrayList<>(comments) : new ArrayList<>();
        this.currentUserId = currentUserId;
        this.movieSlug = movieSlug != null ? movieSlug : "";
        this.movieTitle = movieTitle != null ? movieTitle : "";
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageAvatar;
        TextView textUser, textComment, textTime, textCharCount;
        RatingBar ratingUser;
        ImageView btnReply;
        LinearLayout layoutReplies;
        LinearLayout layoutReplyInput;
        EditText editReply;
        Button btnSubmitReply;
        ProgressBar progressBarReplyLoading;

        public ViewHolder(View itemView) {
            super(itemView);
            imageAvatar = itemView.findViewById(R.id.imageAvatar);
            textUser = itemView.findViewById(R.id.textUser);
            textComment = itemView.findViewById(R.id.textComment);
            textTime = itemView.findViewById(R.id.textTime);
            textCharCount = itemView.findViewById(R.id.textCharCount);
            ratingUser = itemView.findViewById(R.id.ratingUser);
            btnReply = itemView.findViewById(R.id.btnReply);
            layoutReplies = itemView.findViewById(R.id.layoutReplies);
            layoutReplyInput = itemView.findViewById(R.id.layoutReplyInput);
            editReply = itemView.findViewById(R.id.editReply);
            btnSubmitReply = itemView.findViewById(R.id.btnSubmitReply);
            progressBarReplyLoading = itemView.findViewById(R.id.progressBarReplyLoading);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Comment comment = comments.get(position);

        if (comment.getUserId() != null && comment.getUserId().equals(currentUserId)) {
            holder.textUser.setText(comment.getUsername() + " (Bạn)");
            holder.textUser.setTextColor(Color.parseColor("#2196F3"));
        } else {
            holder.textUser.setText(comment.getUsername() != null ? comment.getUsername() : "Người dùng ẩn danh");
            holder.textUser.setTextColor(Color.parseColor("#333333"));
        }

        holder.textComment.setText(comment.getComment() != null ? comment.getComment() : "");

        Double rating = comment.getRating();
        holder.ratingUser.setRating(rating != null ? (float) (double) rating : 0f);

        holder.textTime.setText(getTimeAgo(comment.getTimestamp()));

        String avatarUrl = comment.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(avatarUrl)
                    .transform(new CircleCrop())
                    .placeholder(R.drawable.ic_user_placeholder)
                    .error(R.drawable.ic_user_placeholder)
                    .into(holder.imageAvatar);
        } else {
            holder.imageAvatar.setImageResource(R.drawable.ic_user_placeholder);
        }

        // Kiểm tra trạng thái đăng nhập
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            holder.layoutReplyInput.setVisibility(View.GONE);
            holder.btnReply.setOnClickListener(v -> {
                Toast.makeText(holder.itemView.getContext(), "Vui lòng đăng nhập để phản hồi", Toast.LENGTH_SHORT).show();
            });
        } else {
            // Thêm TextWatcher để theo dõi và kiểm tra độ dài phản hồi
            holder.editReply.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    String replyText = s.toString().trim();
                    holder.btnSubmitReply.setEnabled(!replyText.isEmpty() && replyText.length() <= MAX_REPLY_LENGTH);
                    holder.textCharCount.setText(replyText.length() + "/" + MAX_REPLY_LENGTH);
                    if (replyText.length() > MAX_REPLY_LENGTH) {
                        holder.textCharCount.setTextColor(Color.RED);
                        Toast.makeText(holder.itemView.getContext(),
                                "Phản hồi vượt quá " + MAX_REPLY_LENGTH + " ký tự",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        holder.textCharCount.setTextColor(Color.parseColor("#666666"));
                    }
                }
            });

            holder.btnReply.setOnClickListener(v -> {
                if (expandedComments.contains(comment.getId())) {
                    expandedComments.remove(comment.getId());
                    holder.layoutReplies.setVisibility(View.GONE);
                    holder.layoutReplyInput.setVisibility(View.GONE);
                    holder.editReply.setText(""); // Xóa nội dung khi đóng
                    holder.textCharCount.setText("0/" + MAX_REPLY_LENGTH);
                    holder.textCharCount.setTextColor(Color.parseColor("#666666"));
                } else {
                    expandedComments.add(comment.getId());
                    holder.layoutReplies.setVisibility(View.VISIBLE);
                    holder.layoutReplyInput.setVisibility(View.VISIBLE);
                    loadReplies(comment.getId(), holder.layoutReplies);
                }
            });
        }

        holder.btnSubmitReply.setOnClickListener(v -> {
            String replyText = holder.editReply.getText().toString().trim();
            if (replyText.isEmpty()) {
                Toast.makeText(holder.itemView.getContext(), "Vui lòng nhập nội dung phản hồi", Toast.LENGTH_SHORT).show();
                return;
            }
            if (replyText.length() > MAX_REPLY_LENGTH) {
                Toast.makeText(holder.itemView.getContext(),
                        "Phản hồi không được vượt quá " + MAX_REPLY_LENGTH + " ký tự",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            submitReply(comment.getId(), replyText, holder);
        });
    }

    @Override
    public int getItemCount() {
        return comments != null ? comments.size() : 0;
    }

    private String getTimeAgo(Date date) {
        if (date == null) {
            Log.w("GET_TIME_AGO", "Timestamp is null, returning default value");
            return "Vừa xong";
        }
        long timeAgo = System.currentTimeMillis() - date.getTime();
        if (timeAgo < 60000) return "Vừa xong";
        else if (timeAgo < 3600000) return (int) (timeAgo / 60000) + " phút trước";
        else if (timeAgo < 86400000) return (int) (timeAgo / 3600000) + " giờ trước";
        else if (timeAgo < 604800000) return (int) (timeAgo / 86400000) + " ngày trước";
        else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            return sdf.format(date);
        }
    }

    private void loadReplies(String commentId, LinearLayout container) {
        Log.d("LOAD_REPLIES", "Loading replies for commentId: " + commentId);
        if (commentId == null || commentId.isEmpty()) {
            Log.e("LOAD_REPLIES", "Invalid commentId");
            Toast.makeText(container.getContext(), "Không thể tải phản hồi: ID không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (repliesCache.containsKey(commentId)) {
            displayReplies(repliesCache.get(commentId), container, commentId);
            return;
        }

        container.removeAllViews();
        ProgressBar progressBar = new ProgressBar(container.getContext());
        progressBar.setPadding(16, 8, 16, 8);
        container.addView(progressBar);

        ApiClient.getApiService().getReplies(commentId).enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                container.removeAllViews();
                if (response.isSuccessful() && response.body() != null) {
                    List<Comment> replies = response.body();
                    repliesCache.put(commentId, replies);
                    displayReplies(replies, container, commentId);
                } else {
                    Log.e("LOAD_REPLIES", "Error code: " + response.code());
                    TextView errorText = new TextView(container.getContext());
                    errorText.setText("Không thể tải phản hồi");
                    errorText.setTextColor(Color.parseColor("#FF0000"));
                    errorText.setPadding(16, 8, 16, 8);
                    container.addView(errorText);
                    String errorMessage = response.code() == 404 ? "Không có phản hồi" : "Lỗi tải phản hồi (Mã lỗi: " + response.code() + ")";
                    Toast.makeText(container.getContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                container.removeAllViews();
                Log.e("LOAD_REPLIES", "Network failure: " + t.getMessage(), t);
                TextView errorText = new TextView(container.getContext());
                errorText.setText("Lỗi kết nối mạng");
                errorText.setTextColor(Color.parseColor("#FF0000"));
                errorText.setPadding(16, 8, 16, 8);
                container.addView(errorText);
                Toast.makeText(container.getContext(), "Lỗi kết nối mạng, vui lòng thử lại", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayReplies(List<Comment> replies, LinearLayout container, String commentId) {
        if (replies.isEmpty()) {
            TextView noRepliesText = new TextView(container.getContext());
            noRepliesText.setText("Chưa có phản hồi");
            noRepliesText.setTextColor(Color.parseColor("#666666"));
            noRepliesText.setPadding(16, 8, 16, 8);
            container.addView(noRepliesText);
        } else {
            container.removeAllViews(); // Xóa tất cả view cũ trước khi thêm mới
            for (Comment reply : replies) {
                // Inflate layout cho phản hồi
                View replyView = LayoutInflater.from(container.getContext())
                        .inflate(R.layout.item_reply, container, false);
                TextView textUser = replyView.findViewById(R.id.textUser);
                TextView textComment = replyView.findViewById(R.id.textComment); // Sử dụng textComment
                TextView textTime = replyView.findViewById(R.id.textTime);
                ImageView btnDeleteReply = replyView.findViewById(R.id.btnDeleteReply);
                ImageView btnEditReply = replyView.findViewById(R.id.btnEditReply);

                // Đặt nội dung
                textUser.setText(reply.getUsername() + (reply.getUserId().equals(currentUserId) ? " (Bạn)" : ""));
                textComment.setText(reply.getComment() != null ? reply.getComment() : "");
                textTime.setText(getTimeAgo(reply.getTimestamp()));

                // Hiển thị nút chỉnh sửa và xóa nếu là chủ phản hồi
                if (reply.getUserId().equals(currentUserId)) {
                    btnDeleteReply.setVisibility(View.VISIBLE);
                    btnEditReply.setVisibility(View.VISIBLE);

                    btnDeleteReply.setOnClickListener(v -> {
                        new AlertDialog.Builder(container.getContext())
                                .setTitle("Xóa phản hồi")
                                .setMessage("Bạn có chắc muốn xóa phản hồi này?")
                                .setPositiveButton("Xóa", (dialog, which) -> {
                                    ApiClient.getApiService().deleteReview(reply.getId()).enqueue(new Callback<Void>() {
                                        @Override
                                        public void onResponse(Call<Void> call, Response<Void> response) {
                                            if (response.isSuccessful()) {
                                                Toast.makeText(container.getContext(), "Đã xóa phản hồi", Toast.LENGTH_SHORT).show();
                                                repliesCache.remove(commentId);
                                                loadReplies(commentId, container);
                                            } else {
                                                Log.e("DELETE_REPLY", "Error code: " + response.code());
                                                String errorMessage = response.code() == 400 ? "Dữ liệu không hợp lệ" : "Lỗi server (code: " + response.code() + ")";
                                                Toast.makeText(container.getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Void> call, Throwable t) {
                                            Log.e("DELETE_REPLY", "Network failure: " + t.getMessage());
                                            Toast.makeText(container.getContext(), "Lỗi kết nối mạng, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                })
                                .setNegativeButton("Hủy", null)
                                .show();
                    });

                    btnEditReply.setOnClickListener(v -> {
                        EditText editText = new EditText(container.getContext());
                        editText.setText(reply.getComment());
                        new AlertDialog.Builder(container.getContext())
                                .setTitle("Chỉnh sửa phản hồi")
                                .setView(editText)
                                .setPositiveButton("Lưu", (dialog, which) -> {
                                    String updatedText = editText.getText().toString().trim();
                                    if (updatedText.isEmpty()) {
                                        Toast.makeText(container.getContext(), "Phản hồi không được để trống", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    if (updatedText.length() > MAX_REPLY_LENGTH) {
                                        Toast.makeText(container.getContext(),
                                                "Phản hồi không được vượt quá " + MAX_REPLY_LENGTH + " ký tự",
                                                Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    Comment updatedReply = new Comment();
                                    updatedReply.setComment(updatedText);
                                    updatedReply.setUserId(reply.getUserId());
                                    ApiClient.getApiService().updateReply(reply.getId(), updatedReply).enqueue(new Callback<Comment>() {
                                        @Override
                                        public void onResponse(Call<Comment> call, Response<Comment> response) {
                                            if (response.isSuccessful()) {
                                                Toast.makeText(container.getContext(), "Đã cập nhật phản hồi", Toast.LENGTH_SHORT).show();
                                                repliesCache.remove(commentId);
                                                loadReplies(commentId, container);
                                            } else {
                                                Log.e("EDIT_REPLY", "Error code: " + response.code());
                                                String errorMessage = response.code() == 400 ? "Dữ liệu không hợp lệ" : "Lỗi server (code: " + response.code() + ")";
                                                Toast.makeText(container.getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Comment> call, Throwable t) {
                                            Log.e("EDIT_REPLY", "Network failure: " + t.getMessage());
                                            Toast.makeText(container.getContext(), "Lỗi kết nối mạng, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                })
                                .setNegativeButton("Hủy", null)
                                .show();
                    });
                } else {
                    btnDeleteReply.setVisibility(View.GONE);
                    btnEditReply.setVisibility(View.GONE);
                }

                container.addView(replyView);
            }
        }
    }

    private void submitReply(String parentId, String replyText, ViewHolder holder) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(holder.itemView.getContext(), "Vui lòng đăng nhập để gửi phản hồi", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        holder.btnSubmitReply.setEnabled(false);
        holder.progressBarReplyLoading.setVisibility(View.VISIBLE);

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String username = "Người dùng";
                    if (documentSnapshot.exists()) {
                        username = documentSnapshot.getString("name") != null ? documentSnapshot.getString("name") : "Người dùng";
                    }

                    Comment reply = new Comment();
                    reply.setParentId(parentId);
                    reply.setUserId(userId);
                    reply.setUsername(username);
                    reply.setComment(replyText);
                    reply.setSlug(movieSlug);
                    reply.setMovieTitle(movieTitle);
                    reply.setTimestamp(new Date());

                    ApiClient.getApiService().addReply(reply).enqueue(new Callback<Comment>() {
                        @Override
                        public void onResponse(Call<Comment> call, Response<Comment> response) {
                            holder.btnSubmitReply.setEnabled(true);
                            holder.progressBarReplyLoading.setVisibility(View.GONE);
                            if (response.isSuccessful() && response.body() != null) {
                                Comment newReply = response.body();
                                holder.editReply.setText("");
                                holder.textCharCount.setText("0/" + MAX_REPLY_LENGTH);
                                holder.textCharCount.setTextColor(Color.parseColor("#666666"));
                                InputMethodManager imm = (InputMethodManager) holder.itemView.getContext()
                                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(holder.editReply.getWindowToken(), 0);
                                repliesCache.remove(parentId);
                                loadReplies(parentId, holder.layoutReplies);
                                Toast.makeText(holder.itemView.getContext(), "Phản hồi đã được gửi!", Toast.LENGTH_SHORT).show();
                                Log.d("SUBMIT_REPLY", "Reply submitted for parentId: " + parentId);
                            } else {
                                Log.e("SUBMIT_REPLY", "Error code: " + response.code());
                                String errorMessage;
                                try {
                                    String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                                    Log.e("SUBMIT_REPLY", "Error body: " + errorBody);
                                    errorMessage = response.code() == 400 ? "Dữ liệu không hợp lệ, vui lòng kiểm tra lại" : "Lỗi server (code: " + response.code() + ")";
                                } catch (IOException e) {
                                    Log.e("SUBMIT_REPLY", "Error reading error body", e);
                                    errorMessage = "Lỗi xử lý phản hồi";
                                }
                                Toast.makeText(holder.itemView.getContext(), errorMessage, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Comment> call, Throwable t) {
                            holder.btnSubmitReply.setEnabled(true);
                            holder.progressBarReplyLoading.setVisibility(View.GONE);
                            Log.e("SUBMIT_REPLY", "Network failure: " + t.getMessage(), t);
                            Toast.makeText(holder.itemView.getContext(), "Lỗi kết nối mạng, vui lòng thử lại sau", Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    holder.btnSubmitReply.setEnabled(true);
                    holder.progressBarReplyLoading.setVisibility(View.GONE);
                    Log.e("FETCH_USER", "Error fetching user info: " + e.getMessage());
                    Toast.makeText(holder.itemView.getContext(), "Lỗi khi lấy thông tin người dùng", Toast.LENGTH_LONG).show();
                });
    }

    public void addComment(Comment comment) {
        if (comments != null) {
            comments.add(0, comment);
            notifyItemInserted(0);
        }
    }

    public void updateComment(Comment updatedComment) {
        if (comments != null) {
            for (int i = 0; i < comments.size(); i++) {
                Comment comment = comments.get(i);
                if (comment.getUserId() != null && comment.getUserId().equals(updatedComment.getUserId())) {
                    comments.set(i, updatedComment);
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    public void updateComments(List<Comment> newComments) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return comments.size();
            }

            @Override
            public int getNewListSize() {
                return newComments != null ? newComments.size() : 0;
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return comments.get(oldItemPosition).getId().equals(newComments.get(newItemPosition).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return comments.get(oldItemPosition).equals(newComments.get(newItemPosition));
            }
        });
        comments.clear();
        if (newComments != null) {
            comments.addAll(newComments);
        }
        diffResult.dispatchUpdatesTo(this);
    }

    public void clearComments() {
        if (comments != null) {
            comments.clear();
            notifyDataSetChanged();
        }
    }
}