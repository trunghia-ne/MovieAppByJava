package com.example.movieappbyjava.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieappbyjava.R; // Đảm bảo R được import đúng

import java.util.List;

public class SuggestionHistoryAdapter extends RecyclerView.Adapter<SuggestionHistoryAdapter.ViewHolder> {

    private Context context;
    private List<String> items;
    private OnItemClickListener listener;
    private boolean isHistoryAdapter; // Để biết đây là adapter cho lịch sử hay gợi ý

    public interface OnItemClickListener {
        void onItemClick(String query);

        void onDeleteClick(String query, int position); // Cho nút xóa lịch sử
    }

    public SuggestionHistoryAdapter(Context context, List<String> items, boolean isHistoryAdapter, OnItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.isHistoryAdapter = isHistoryAdapter;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_suggestion_or_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String query = items.get(position);
        holder.textViewQuery.setText(query);

        if (isHistoryAdapter) {
            holder.imageViewIcon.setImageResource(R.drawable.ic_history);
            holder.imageViewDelete.setVisibility(View.VISIBLE);
            holder.imageViewDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(query, holder.getAdapterPosition());
                }
            });
        } else {
            holder.imageViewIcon.setImageResource(R.drawable.search);
            holder.imageViewDelete.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(query);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public void updateData(List<String> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (items != null && position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, items.size());
        }
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewIcon;
        TextView textViewQuery;
        ImageView imageViewDelete;

        ViewHolder(View itemView) {
            super(itemView);
            imageViewIcon = itemView.findViewById(R.id.image_view_item_icon);
            textViewQuery = itemView.findViewById(R.id.text_view_item_query);
            imageViewDelete = itemView.findViewById(R.id.image_view_item_delete);
        }
    }
}