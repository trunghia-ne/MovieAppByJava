package com.example.movieappbyjava.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.movieappbyjava.R;
import com.example.movieappbyjava.adapter.PaymentAdapter;
import com.example.movieappbyjava.model.ApiClient;
import com.example.movieappbyjava.model.Payment;
import com.example.movieappbyjava.network.ApiService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.ArrayList;
import java.util.List;

public class RevenueManagementFragment extends Fragment {

    private RecyclerView rvRevenueList;
    private TextView tvTotalRevenue;
    private PaymentAdapter paymentAdapter;
    private List<Payment> paymentList;

    public RevenueManagementFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_revenue_management, container, false);

        // Initialize views
        tvTotalRevenue = view.findViewById(R.id.tv_total_revenue);
        rvRevenueList = view.findViewById(R.id.rv_revenue_list);
        rvRevenueList.setLayoutManager(new LinearLayoutManager(getContext()));
        paymentList = new ArrayList<>();
        paymentAdapter = new PaymentAdapter(paymentList);
        rvRevenueList.setAdapter(paymentAdapter);
        SearchView searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                paymentAdapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                paymentAdapter.filter(newText);
                return true;
            }
        });
        // Fetch payments from API
        fetchPayments();

        return view;
    }

    private void fetchPayments() {
        // Gọi API lấy tổng doanh thu
        ApiClient.getApiService().getTotalRevenue().enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful() && response.body() != null) {
                    long total = response.body();
                    tvTotalRevenue.setText(String.format("Tổng doanh thu: %,d VNĐ", total));
                }
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi doanh thu: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Gọi API lấy danh sách thanh toán
        ApiClient.getApiService().getPayments().enqueue(new Callback<List<Payment>>() {
            @Override
            public void onResponse(Call<List<Payment>> call, Response<List<Payment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Payment> payments = response.body();
                    paymentAdapter.updatePayments(payments);
                }
            }

            @Override
            public void onFailure(Call<List<Payment>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi danh sách: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}