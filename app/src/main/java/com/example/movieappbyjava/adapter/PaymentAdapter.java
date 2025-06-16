package com.example.movieappbyjava.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.movieappbyjava.R;
import com.example.movieappbyjava.model.Payment;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {
    private List<Payment> paymentList;
    public PaymentAdapter(List<Payment> paymentList) {
        this.paymentList = new ArrayList<>(paymentList);
        this.originalList = new ArrayList<>(paymentList);
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payment payment = paymentList.get(position);

        // Định dạng tiền tệ và thời gian
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

        // Hiển thị có nhãn rõ ràng
        holder.tvAmount.setText("Số tiền: " + String.format("%,d VNĐ", payment.getAmount()));
        holder.tvPaymentMethod.setText("Phương thức thanh toán: " + payment.getPaymentMethod());
        holder.tvPaymentStatus.setText("Trạng thái: " + (payment.isPaid() ? "Đã thanh toán" : "Chưa thanh toán"));
        holder.tvPaymentTime.setText("Thời gian: " + sdf.format(payment.getPaymentTime()));
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(payment.getUserId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userName = documentSnapshot.getString("name");
                        holder.tvUserId.setText("Người dùng: " + userName);
                    } else {
                        holder.tvUserId.setText("Người dùng: (Không tìm thấy)");
                    }
                })
                .addOnFailureListener(e -> {
                    holder.tvUserId.setText("Người dùng: (Lỗi)");
                });

    }

    private List<Payment> originalList;
    public void updatePayments(List<Payment> newPayments) {
        this.originalList = new ArrayList<>(newPayments);
        this.paymentList = new ArrayList<>(newPayments);
        notifyDataSetChanged();
    }

    public void filter(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            paymentList = new ArrayList<>(originalList);
        } else {
            List<Payment> filtered = new ArrayList<>();
            for (Payment p : originalList) {
                if (p.getUserId().toLowerCase().contains(keyword.toLowerCase()) ||
                        p.getPaymentMethod().toLowerCase().contains(keyword.toLowerCase())) {
                    filtered.add(p);
                }
            }
            paymentList = filtered;
        }
        notifyDataSetChanged();
    }



    @Override
    public int getItemCount() {
        return paymentList != null ? paymentList.size() : 0;
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView tvAmount, tvPaymentMethod, tvPaymentStatus, tvPaymentTime, tvUserId;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvPaymentMethod = itemView.findViewById(R.id.tv_payment_method);
            tvPaymentStatus = itemView.findViewById(R.id.tv_payment_status);
            tvPaymentTime = itemView.findViewById(R.id.tv_payment_time);
            tvUserId = itemView.findViewById(R.id.tv_user_id);
        }
    }
}