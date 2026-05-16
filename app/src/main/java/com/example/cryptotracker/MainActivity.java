package com.example.cryptotracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cryptotracker.adapter.CoinAdapter;
import com.example.cryptotracker.databinding.ActivityMainBinding;
import com.example.cryptotracker.model.Coin;
import com.example.cryptotracker.repository.CryptoRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private CryptoRepository cryptoRepository;
    private CoinAdapter coinAdapter;

    private boolean isGridMode = false;
    private boolean showingFavorites = false;

    private List<Coin> currentCoins = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cryptoRepository = new CryptoRepository(this);

        setupRecyclerView();
        setupLayoutSwitchButton();
        setupFavoritesButton();
        setupSearch();
        setupCategoryChips();

        loadMarketCoins();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (coinAdapter != null) {
            loadFavoriteCoinIds();
        }
    }

    private void setupRecyclerView() {
        coinAdapter = new CoinAdapter(this::openCoinDetails, this::toggleFavorite);

        binding.coinsRecyclerView.setAdapter(coinAdapter);
        binding.coinsRecyclerView.setItemAnimator(new DefaultItemAnimator());

        showListLayout();
    }

    private void setupLayoutSwitchButton() {
        binding.layoutSwitchButton.setOnClickListener(view -> {
            if (isGridMode) {
                showListLayout();
            } else {
                showGridLayout();
            }
        });
    }

    private void setupFavoritesButton() {
        binding.favoritesButton.setOnClickListener(view -> {
            if (showingFavorites) {
                showingFavorites = false;
                binding.favoritesButton.setText("Favourites");
                loadMarketCoins();
            } else {
                showingFavorites = true;
                binding.favoritesButton.setText("All Coins");
                loadFavoriteCoins();
            }
        });
    }

    private void setupSearch() {
        /*
        При всяка промяна в search полето,
        филтрираме текущия списък.
         */

        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                filterCoins(text.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void setupCategoryChips() {
        /*
        Когато user избере Top/Gainers/Losers,
        филтрираме текущия списък наново.
         */

        binding.categoryChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            filterCoins(getSearchText());
        });
    }

    private void showListLayout() {
        isGridMode = false;
        binding.layoutSwitchButton.setText("Grid");
        binding.coinsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void showGridLayout() {
        isGridMode = true;
        binding.layoutSwitchButton.setText("List");
        binding.coinsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    }

    private void loadMarketCoins() {
        showLoading();

        cryptoRepository.loadMarketCoins(new CryptoRepository.RepositoryCallback<List<Coin>>() {
            @Override
            public void onSuccess(List<Coin> coins) {
                runOnUiThread(() -> showCoinsResult(coins, "No coins available."));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> showErrorResult(message));
            }
        });
    }

    private void loadFavoriteCoins() {
        /*
        Favorites идват от Room, не от API.
         */

        showLoading();

        cryptoRepository.loadFavoriteCoins(new CryptoRepository.RepositoryCallback<List<Coin>>() {
            @Override
            public void onSuccess(List<Coin> coins) {
                runOnUiThread(() -> showCoinsResult(coins, "No favorite coins yet."));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> showErrorResult(message));
            }
        });
    }

    private void showCoinsResult(List<Coin> coins, String emptyMessage) {
        /*
        Общ метод за показване на coins.
        Използва се и за All Coins, и за Favorites.
         */

        hideLoading();

        if (coins == null || coins.isEmpty()) {
            currentCoins = new ArrayList<>();
            coinAdapter.submitList(new ArrayList<>());
            showStatusMessage(emptyMessage);
            return;
        }

        hideStatusMessage();

        currentCoins = coins;
        filterCoins(getSearchText());
        loadFavoriteCoinIds();
    }

    private void showErrorResult(String message) {
        hideLoading();
        showStatusMessage(message);
        showError(message);
    }

    private void loadFavoriteCoinIds() {
        /*
        Зареждаме id-тата на favorite coins.
        Така adapter-ът знае кои звезди да покаже жълти.
         */

        cryptoRepository.loadFavoriteCoinIds(new CryptoRepository.RepositoryCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> favoriteCoinIds) {
                runOnUiThread(() -> coinAdapter.setFavoriteCoinIds(favoriteCoinIds));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> showSimpleMessage("Favorites", message));
            }
        });
    }

    private String getSearchText() {
        if (binding.searchEditText.getText() == null) {
            return "";
        }

        return binding.searchEditText.getText().toString();
    }

    private void filterCoins(String searchText) {
        /*
        Филтрираме по:
        1. search текст
        2. избрана категория: Top / Gainers / Losers
         */

        if (currentCoins == null || currentCoins.isEmpty()) {
            return;
        }

        String search = searchText.toLowerCase(Locale.ROOT).trim();
        List<Coin> filteredCoins = new ArrayList<>();

        for (Coin coin : currentCoins) {
            if (matchesSearch(coin, search) && matchesSelectedCategory(coin)) {
                filteredCoins.add(coin);
            }
        }

        sortBySelectedCategory(filteredCoins);
        showFilteredCoins(filteredCoins);
    }

    private boolean matchesSearch(Coin coin, String search) {
        String name = coin.getName();
        String symbol = coin.getSymbol();

        boolean matchesName = name != null && name.toLowerCase(Locale.ROOT).contains(search);
        boolean matchesSymbol = symbol != null && symbol.toLowerCase(Locale.ROOT).contains(search);

        return search.isEmpty() || matchesName || matchesSymbol;
    }

    private boolean matchesSelectedCategory(Coin coin) {
        if (binding.gainersChip.isChecked()) {
            return coin.getPriceChangePercentage24h() != null
                    && coin.getPriceChangePercentage24h() > 0;
        }

        if (binding.losersChip.isChecked()) {
            return coin.getPriceChangePercentage24h() != null
                    && coin.getPriceChangePercentage24h() < 0;
        }

        return true;
    }

    private void sortBySelectedCategory(List<Coin> coins) {
        if (binding.gainersChip.isChecked()) {
            Collections.sort(coins, (coin1, coin2) ->
                    Double.compare(
                            coin2.getPriceChangePercentage24h(),
                            coin1.getPriceChangePercentage24h()
                    )
            );
        } else if (binding.losersChip.isChecked()) {
            Collections.sort(coins, (coin1, coin2) ->
                    Double.compare(
                            coin1.getPriceChangePercentage24h(),
                            coin2.getPriceChangePercentage24h()
                    )
            );
        }
    }

    private void showFilteredCoins(List<Coin> filteredCoins) {
        if (filteredCoins.isEmpty()) {
            coinAdapter.submitList(new ArrayList<>());
            showStatusMessage("No matching coins.");
            return;
        }

        hideStatusMessage();

        coinAdapter.submitList(filteredCoins, () -> {
            binding.coinsRecyclerView.scrollToPosition(0);
        });
    }

    private void openCoinDetails(Coin coin) {
        if (coin == null || coin.getId() == null) {
            return;
        }

        Intent intent = new Intent(this, CoinDetailActivity.class);

        intent.putExtra(CoinDetailActivity.EXTRA_COIN_ID, coin.getId());
        intent.putExtra(CoinDetailActivity.EXTRA_COIN_NAME, coin.getName());
        intent.putExtra(CoinDetailActivity.EXTRA_COIN_SYMBOL, coin.getSymbol());
        intent.putExtra(CoinDetailActivity.EXTRA_COIN_IMAGE, coin.getImage());

        if (coin.getCurrentPrice() != null) {
            intent.putExtra(CoinDetailActivity.EXTRA_COIN_PRICE, coin.getCurrentPrice());
        }

        if (coin.getPriceChangePercentage24h() != null) {
            intent.putExtra(CoinDetailActivity.EXTRA_COIN_CHANGE, coin.getPriceChangePercentage24h());
        }

        if (coin.getMarketCap() != null) {
            intent.putExtra(CoinDetailActivity.EXTRA_COIN_MARKET_CAP, coin.getMarketCap());
        }

        if (coin.getTotalVolume() != null) {
            intent.putExtra(CoinDetailActivity.EXTRA_COIN_VOLUME, coin.getTotalVolume());
        }

        startActivity(intent);
    }

    private void toggleFavorite(Coin coin, View starView) {
        if (coin == null) {
            return;
        }

        cryptoRepository.toggleFavorite(coin, new CryptoRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isFavorite) {
                runOnUiThread(() -> handleFavoriteResult(coin, starView, isFavorite));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> showSimpleMessage("Favorites", message));
            }
        });
    }

    private void handleFavoriteResult(Coin coin, View starView, Boolean isFavorite) {
        ImageButton starButton = (ImageButton) starView;

        if (isFavorite) {
            starButton.setImageResource(android.R.drawable.btn_star_big_on);
            showSimpleMessage("Favorites", coin.getName() + " added to Favorites.");
        } else {
            starButton.setImageResource(android.R.drawable.btn_star_big_off);
            showSimpleMessage("Favorites", coin.getName() + " removed from Favorites.");
        }

        loadFavoriteCoinIds();

        if (showingFavorites) {
            loadFavoriteCoins();
        }
    }

    private void showSimpleMessage(String title, String message) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showError(String message) {
        showSimpleMessage("Error", message);
    }

    private void showLoading() {
        binding.loadingProgressBar.setVisibility(View.VISIBLE);
        binding.statusTextView.setVisibility(View.GONE);
    }

    private void hideLoading() {
        binding.loadingProgressBar.setVisibility(View.GONE);
    }

    private void showStatusMessage(String message) {
        binding.statusTextView.setText(message);
        binding.statusTextView.setVisibility(View.VISIBLE);
    }

    private void hideStatusMessage() {
        binding.statusTextView.setVisibility(View.GONE);
    }
}
