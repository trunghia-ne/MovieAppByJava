package com.example.movieappbyjava.fragment;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieappbyjava.MainNavigationActivity;
import com.example.movieappbyjava.R;
import com.example.movieappbyjava.adapter.CollectionAdapter;
import com.example.movieappbyjava.model.ApiClient;
import com.example.movieappbyjava.model.ApiResponseMessage;
import com.example.movieappbyjava.model.CollectionFilm;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesFragment extends Fragment {

    private RecyclerView rvCollections;
    private CollectionAdapter adapter;
    private List<CollectionFilm> collectionList = new ArrayList<>();
    ImageButton btnBack;
    FloatingActionButton fabAdd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        rvCollections = view.findViewById(R.id.rvCollections);
        btnBack = view.findViewById(R.id.btnBack);
        fabAdd = view.findViewById(R.id.fabAddCollection);
        rvCollections.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new CollectionAdapter(getContext(), collectionList, new CollectionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CollectionFilm collection) {
                FilmCollection fragment = FilmCollection.newInstance(collection.getId(), collection.getCollection_name());

                ((AppCompatActivity) requireActivity()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onItemLongClick(CollectionFilm collection) {
                showDeleteDialog(collection);  // ✅ gọi xác nhận xóa
            }
        });


        rvCollections.setAdapter(adapter);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return view;
        }

        loadCollectionsFromApi(currentUser.getUid());

        btnBack.setOnClickListener(v -> {
            ((MainNavigationActivity) requireActivity()).backFromFavorites();
        });

        fabAdd.setOnClickListener(v -> showAddCollectionDialog());

        return view;
    }

    private void showAddCollectionDialog() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_collection, null);
        TextInputEditText editTextCollectionName = dialogView.findViewById(R.id.editTextCollectionName);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(getContext(), "User ID không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add new collection")
                .setView(dialogView)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Add", (dialog, which) -> {
                    String collectionName = editTextCollectionName.getText().toString().trim();
                    if (!collectionName.isEmpty()) {
                        CollectionFilm newCollection = new CollectionFilm();
                        newCollection.setCollection_name(collectionName);
                        newCollection.setUserId(userId);

                        ApiClient.getApiService().addCollection(userId, newCollection)
                                .enqueue(new Callback<ApiResponseMessage>() {
                                    @Override
                                    public void onResponse(Call<ApiResponseMessage> call, Response<ApiResponseMessage> response) {
                                        if (response.isSuccessful() && response.body() != null) {
                                            Toast.makeText(getContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                            loadCollectionsFromApi(userId);
                                        } else {
                                            String errorMsg = "Lỗi server: " + response.code();
                                            try {
                                                if (response.errorBody() != null) {
                                                    String errorBodyString = response.errorBody().string();
                                                    errorMsg = parseMessageFromJson(errorBodyString);
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ApiResponseMessage> call, Throwable t) {
                                        Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(getContext(), "Tên không được để trống", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    // Hàm phụ để parse message từ JSON lỗi
    private String parseMessageFromJson(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            if (jsonObject.has("message")) {
                return jsonObject.getString("message");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonString;  // Nếu parse lỗi, trả nguyên chuỗi
    }



    private void loadCollectionsFromApi(String userId) {
        ApiClient.getApiService().getCollectionsByUser(userId)
                .enqueue(new Callback<List<CollectionFilm>>() {
                    @Override
                    public void onResponse(Call<List<CollectionFilm>> call, Response<List<CollectionFilm>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            collectionList.clear();
                            collectionList.addAll(response.body());
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "Lấy bộ sưu tập thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<CollectionFilm>> call, Throwable t) {
                        Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDeleteDialog(CollectionFilm collection) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa bộ sưu tập")
                .setMessage("Bạn có chắc chắn muốn xóa \"" + collection.getCollection_name() + "\"?")
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Xóa", (dialog, which) -> {
                    ApiClient.getApiService().deleteCollection(collection.getId())
                            .enqueue(new Callback<ApiResponseMessage>() {
                                @Override
                                public void onResponse(Call<ApiResponseMessage> call, Response<ApiResponseMessage> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        Toast.makeText(getContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                        if (currentUser != null) {
                                            loadCollectionsFromApi(currentUser.getUid());
                                        }
                                    } else {
                                        Toast.makeText(getContext(), "Xóa thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ApiResponseMessage> call, Throwable t) {
                                    Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .show();
    }

}

