package com.example.movieappbyjava.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WatchHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private WatchHistoryAdapter adapter;
    private List<WatchHistory> historyList;
    private FirebaseFirestore firestore;

    private static final String TAG = "WatchHistoryFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        historyList = new ArrayList<>();
        adapter = new WatchHistoryAdapter(getContext(), historyList);
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();

        fetchWatchHistory();

        return view;
    }

    private void fetchWatchHistory() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        CollectionReference historyRef = firestore.collection("watch_history");

        historyRef.whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    historyList.clear();

                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            WatchHistory history = doc.toObject(WatchHistory.class);
                            historyList.add(history);
                            Log.d(TAG, "Loaded movie: " + history.getName());
                        }
                    } else {
                        // Nếu không có dữ liệu, thêm test
                        historyList.add(new WatchHistory(
                                userId, "m001", "Test Movie",
                                "https://example.com/poster.jpg",
                                3000, 9000, "Finished",
                                new Date(), "Android"));
                        Log.d(TAG, "No history found. Added test item.");
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi lấy lịch sử xem phim", e);
                });
    }

}