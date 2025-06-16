package com.example.movieappbyjava.fragment;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.movieappbyjava.R;

public class CategoryFragment extends Fragment {
    private ImageView btnBack;
    public CategoryFragment() {

    }

    public static CategoryFragment newInstance(String param1, String param2) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_category, container, false);

        LinearLayout cardPhimLe = view.findViewById(R.id.card_phim_le);
        LinearLayout cardPhimBo = view.findViewById(R.id.card_phim_bo);
        LinearLayout cardPhimAnime = view.findViewById(R.id.card_phim_anime);
        LinearLayout cardTvShows = view.findViewById(R.id.card_tvshows);

        // Gán click mở danh sách tương ứng
        cardPhimLe.setOnClickListener(v -> openCategory("phim_le"));
        cardPhimBo.setOnClickListener(v -> openCategory("phim_bo"));
        cardPhimAnime.setOnClickListener(v -> openCategory("anime"));
        cardTvShows.setOnClickListener(v -> openCategory("tvshows"));

        btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    private void openCategory(String categoryKey) {
        CateMovieFragment fragment = CateMovieFragment.newInstance(categoryKey);

        // Ẩn layout chính, hiện container nested
        requireView().findViewById(R.id.category_layout).setVisibility(View.GONE);
        requireView().findViewById(R.id.nestedFragmentContainer).setVisibility(View.VISIBLE);

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.nestedFragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}