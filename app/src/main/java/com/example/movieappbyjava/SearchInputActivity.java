package com.example.movieappbyjava;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieappbyjava.adapter.SuggestionHistoryAdapter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SearchInputActivity extends AppCompatActivity {

    private static final String TAG = "SearchInputActivity";
    private static final String PREFS_NAME = "SearchHistoryPrefs";
    private static final String HISTORY_KEY = "search_history";
    private static final int MAX_HISTORY_ITEMS = 10;

    private ImageButton buttonBack;
    private SearchView searchViewInput;
    private RecyclerView recyclerSuggestions;
    private RecyclerView recyclerHistory;
    private LinearLayout layoutSearchHistory;
    private TextView textViewSearchHistoryTitle;
    private TextView textViewClearHistory;
    private LinearLayout searchBarContainer;

    private SuggestionHistoryAdapter historyAdapter;
    private SuggestionHistoryAdapter suggestionsAdapter;
    private List<String> searchHistoryList;
    private List<String> suggestionList;

    private SharedPreferences sharedPreferences;
    private ActivityResultLauncher<Intent> searchResultsLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_input);

        searchBarContainer = findViewById(R.id.search_bar_container);
        buttonBack = findViewById(R.id.button_back_search_page);
        searchViewInput = findViewById(R.id.search_view_input);
        recyclerSuggestions = findViewById(R.id.recycler_search_suggestions);
        layoutSearchHistory = findViewById(R.id.layout_search_history);
        textViewSearchHistoryTitle = findViewById(R.id.text_view_search_history_title);
        textViewClearHistory = findViewById(R.id.text_view_clear_history);
        recyclerHistory = findViewById(R.id.recycler_search_history);

        if (searchBarContainer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(searchBarContainer, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                int originalTopMarginInDp = 8;
                params.topMargin = insets.top + (int) (originalTopMarginInDp * getResources().getDisplayMetrics().density);
                v.setLayoutParams(params);
                return WindowInsetsCompat.CONSUMED;
            });
        }


        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        searchHistoryList = new ArrayList<>();
        suggestionList = new ArrayList<>();

        setupBackButton();
        setupSearchView();
        setupHistoryRecyclerView();
        setupSuggestionsRecyclerView();
        setupClearHistoryButton();

        loadSearchHistory();

        searchResultsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (searchViewInput != null) {
                searchViewInput.setQuery("", false);
                searchViewInput.clearFocus();
                showHistoryView();
            }
        });

        searchViewInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            EditText searchTextEditTextInSearchView = searchViewInput.findViewById(androidx.appcompat.R.id.search_src_text);
            if (searchTextEditTextInSearchView != null) {
                imm.showSoftInput(searchTextEditTextInSearchView, InputMethodManager.SHOW_IMPLICIT);
            }
        }

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void setupBackButton() {
        if (buttonBack != null) {
            buttonBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }
    }

    private void setupSearchView() {
        if (searchViewInput == null) return;
        searchViewInput.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)) {
                    String trimmedQuery = query.trim();
                    saveQueryToHistory(trimmedQuery);
                    launchSearchResults(trimmedQuery);
                    searchViewInput.clearFocus();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    showHistoryView();
                    hideSuggestionsView();
                } else {
                    hideHistoryView();
                    fetchSuggestions(newText.trim());
                }
                return true;
            }
        });

        ImageView closeButton = searchViewInput.findViewById(androidx.appcompat.R.id.search_close_btn);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> {
                searchViewInput.setQuery("", false);
                searchViewInput.clearFocus();
                showHistoryView();
                hideSuggestionsView();
            });
        }
    }

    private void setupHistoryRecyclerView() {
        if (recyclerHistory == null || searchHistoryList == null) return;
        historyAdapter = new SuggestionHistoryAdapter(this, searchHistoryList, true, new SuggestionHistoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String query) {
                searchViewInput.setQuery(query, true);
            }

            @Override
            public void onDeleteClick(String query, int position) {
                removeQueryFromHistory(query, position);
            }
        });
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerHistory.setAdapter(historyAdapter);
    }

    private void setupSuggestionsRecyclerView() {
        if (recyclerSuggestions == null || suggestionList == null) return;
        suggestionsAdapter = new SuggestionHistoryAdapter(this, suggestionList, false, new SuggestionHistoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String query) {
                searchViewInput.setQuery(query, true);
            }

            @Override
            public void onDeleteClick(String query, int position) { /* Không dùng cho suggestions */ }
        });
        recyclerSuggestions.setLayoutManager(new LinearLayoutManager(this));
        recyclerSuggestions.setAdapter(suggestionsAdapter);
    }

    private void setupClearHistoryButton() {
        if (textViewClearHistory != null) {
            textViewClearHistory.setOnClickListener(v -> {
                clearAllHistory();
            });
        }
    }

    private void loadSearchHistory() {
        if (sharedPreferences == null || searchHistoryList == null || historyAdapter == null)
            return;
        Set<String> historySet = sharedPreferences.getStringSet(HISTORY_KEY, new LinkedHashSet<>());
        searchHistoryList.clear();
        searchHistoryList.addAll(new ArrayList<>(historySet)); // Đảm bảo thứ tự được giữ
        historyAdapter.notifyDataSetChanged();
        updateHistoryViewVisibility();
    }

    private void saveQueryToHistory(String query) {
        if (sharedPreferences == null) return;
        Set<String> historySet = sharedPreferences.getStringSet(HISTORY_KEY, new LinkedHashSet<>());
        List<String> tempList = new ArrayList<>(historySet);
        tempList.remove(query);
        tempList.add(0, query);
        while (tempList.size() > MAX_HISTORY_ITEMS) {
            tempList.remove(tempList.size() - 1);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(HISTORY_KEY, new LinkedHashSet<>(tempList));
        editor.apply();
        loadSearchHistory();
    }

    private void removeQueryFromHistory(String query, int position) {
        if (sharedPreferences == null || historyAdapter == null) return;
        Set<String> historySet = sharedPreferences.getStringSet(HISTORY_KEY, new LinkedHashSet<>());
        List<String> tempList = new ArrayList<>(historySet);
        boolean removedFromPrefs = tempList.remove(query);

        if (removedFromPrefs) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet(HISTORY_KEY, new LinkedHashSet<>(tempList));
            editor.apply();
        }
        historyAdapter.removeItem(position);
        updateHistoryViewVisibility();
    }

    private void clearAllHistory() {
        if (sharedPreferences == null) return;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(HISTORY_KEY);
        editor.apply();
        loadSearchHistory();
    }

    private void updateHistoryViewVisibility() {
        boolean hasHistory = searchHistoryList != null && !searchHistoryList.isEmpty();
        if (textViewSearchHistoryTitle != null)
            textViewSearchHistoryTitle.setVisibility(hasHistory ? View.VISIBLE : View.GONE);
        if (textViewClearHistory != null)
            textViewClearHistory.setVisibility(hasHistory ? View.VISIBLE : View.GONE);
        if (recyclerHistory != null)
            recyclerHistory.setVisibility(hasHistory ? View.VISIBLE : View.GONE);
        if (layoutSearchHistory != null)
            layoutSearchHistory.setVisibility(hasHistory ? View.VISIBLE : View.GONE);

    }

    private void showHistoryView() {
        if (layoutSearchHistory != null) {
            layoutSearchHistory.setVisibility(View.VISIBLE);
            updateHistoryViewVisibility();
        }
    }

    private void hideHistoryView() {
        if (layoutSearchHistory != null) layoutSearchHistory.setVisibility(View.GONE);
    }

    private void showSuggestionsView() {
        if (recyclerSuggestions != null) recyclerSuggestions.setVisibility(View.VISIBLE);
    }

    private void hideSuggestionsView() {
        if (recyclerSuggestions != null) recyclerSuggestions.setVisibility(View.GONE);
    }

    private void fetchSuggestions(String query) {
        Log.d(TAG, "Đang tìm gợi ý cho: " + query);
        if (TextUtils.isEmpty(query)) {
            showHistoryView();
            hideSuggestionsView();
        } else {
            hideHistoryView();
            hideSuggestionsView(); // Hoặc:
        }
    }

    private void launchSearchResults(String query) {
        Intent intent = new Intent(this, SearchResultsActivity.class);
        intent.putExtra(SearchResultsActivity.SEARCH_QUERY, query);
        searchResultsLauncher.launch(intent);
    }

}