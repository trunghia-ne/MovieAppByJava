package com.example.movieappbyjava.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.movieappbyjava.R;

public class ProfileAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] titles;
    private final int[] icons;

    public ProfileAdapter(Context context, String[] titles, int[] icons) {
        super(context, R.layout.list_item_profile, titles);
        this.context = context;
        this.titles = titles;
        this.icons = icons;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            row = inflater.inflate(R.layout.list_item_profile, parent, false);
        }

        TextView textView = row.findViewById(R.id.item_text);
        ImageView iconView = row.findViewById(R.id.item_icon);

        textView.setText(titles[position]);
        iconView.setImageResource(icons[position]);

        return row;
    }
}
