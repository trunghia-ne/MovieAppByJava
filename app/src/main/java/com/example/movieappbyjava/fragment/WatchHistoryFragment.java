package com.example.movieappbyjava.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // Import để hiển thị thông báo

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieappbyjava.R;
import com.example.movieappbyjava.adapter.WatchHistoryAdapter;
import com.example.movieappbyjava.model.WatchHistory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WatchHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private WatchHistoryAdapter adapter;
    private List<WatchHistory> historyList;
    private FirebaseFirestore firestore;
    // Thêm một TextView để hiển thị thông báo khi không có dữ liệu
    private TextView tvEmptyHistory;

    private static final String TAG = "WatchHistoryFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Sử dụng layout fragment_history.xml có sẵn
        // (Bạn cần thêm một TextView với id là @+id/tvEmptyHistory vào file đó)
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // Ánh xạ các view
        recyclerView = view.findViewById(R.id.recyclerViewHistory);
        // tvEmptyHistory = view.findViewById(R.id.tvEmptyHistory); // Sẽ thêm ở bước sau

        // Cài đặt RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        historyList = new ArrayList<>();
        adapter = new WatchHistoryAdapter(getContext(), historyList); // Dùng Adapter đúng
        recyclerView.setAdapter(adapter);

        // Khởi tạo Firestore
        firestore = FirebaseFirestore.getInstance();

        // Bắt đầu lấy dữ liệu
        fetchWatchHistory();

        return view;
    }

    private void fetchWatchHistory() {
        // 1. Kiểm tra người dùng đã đăng nhập chưa
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e(TAG, "Lỗi: Người dùng chưa đăng nhập. Không thể lấy lịch sử.");
            // Có thể hiển thị thông báo yêu cầu đăng nhập ở đây
            // tvEmptyHistory.setText("Vui lòng đăng nhập để xem lịch sử");
            // tvEmptyHistory.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "Bắt đầu lấy lịch sử cho userId: " + userId);

        // 2. Tạo câu truy vấn đến Firestore
        CollectionReference historyRef = firestore.collection("watch_history");
        historyRef.whereEqualTo("userId", userId)
                .orderBy("watchedAt", Query.Direction.DESCENDING) // Sắp xếp phim mới xem lên đầu
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // 3. Xử lý kết quả trả về thành công
                    Log.d(TAG, "Lấy dữ liệu thành công từ Firestore.");
                    historyList.clear(); // Luôn xóa danh sách cũ trước khi thêm mới

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        // Nếu CÓ dữ liệu
                        Log.d(TAG, "Tìm thấy " + queryDocumentSnapshots.size() + " mục trong lịch sử.");
                        recyclerView.setVisibility(View.VISIBLE);
                        // tvEmptyHistory.setVisibility(View.GONE);

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            WatchHistory history = doc.toObject(WatchHistory.class);
                            historyList.add(history);
                        }
                    } else {
                        // Nếu KHÔNG CÓ dữ liệu
                        Log.d(TAG, "Không tìm thấy mục nào trong lịch sử cho người dùng này.");
                        recyclerView.setVisibility(View.GONE);
                        // tvEmptyHistory.setText("Lịch sử xem phim của bạn trống.");
                        // tvEmptyHistory.setVisibility(View.VISIBLE);
                    }

                    // 4. Cập nhật lại giao diện
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Xử lý khi có lỗi xảy ra (ví dụ: không có quyền truy cập, không có mạng)
                    Log.e(TAG, "Lỗi khi lấy dữ liệu từ Firestore: ", e);
                    // tvEmptyHistory.setText("Lỗi khi tải lịch sử. Vui lòng thử lại.");
                    // tvEmptyHistory.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                });
    }
}