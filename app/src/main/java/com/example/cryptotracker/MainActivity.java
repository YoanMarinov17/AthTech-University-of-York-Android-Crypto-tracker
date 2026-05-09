package com.example.cryptotracker;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.View;


import com.example.cryptotracker.adapter.CoinAdapter;
import com.example.cryptotracker.databinding.ActivityMainBinding;
import com.example.cryptotracker.model.Coin;
import com.example.cryptotracker.repository.CryptoRepository;

import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding; // Binding е мост между activity_main.xml и Java кода.
    private CryptoRepository cryptoRepository; // Repository-то управлява API + Room логиката.
    private CoinAdapter coinAdapter; // Adapter-ът превръща List<Coin> в редове в RecyclerView.
    private boolean isGridMode = false; // false означава list mode, true означава grid mode.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cryptoRepository = new CryptoRepository(this);

        setupRecyclerView();
        setupLayoutSwitchButton();
        loadMarketCoins();
    }

    private void setupRecyclerView() {
        coinAdapter = new CoinAdapter(this::openCoinDetails); // Казваме какво да стане при click върху coin.
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
        showLoading(); // Показваме loading, докато чакаме API/Room.

        cryptoRepository.loadMarketCoins(new CryptoRepository.RepositoryCallback<List<Coin>>() {
            @Override
            public void onSuccess(List<Coin> coins) {
                runOnUiThread(() -> {
                    hideLoading();

                    if (coins == null || coins.isEmpty()) {
                        showStatusMessage("No coins available.");
                    } else {
                        hideStatusMessage();
                        coinAdapter.submitList(coins);
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    hideLoading();
                    showStatusMessage(message);
                    showError(message);
                });
            }
        });
    }


    private void showError(String message) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void openCoinDetails(Coin coin) {
    /*
    Този метод се извиква, когато user натисне coin от списъка.

    Засега го правим просто:
    1. проверяваме дали coin е валиден
    2. викаме details endpoint-а
    3. ако API-то върне успешен отговор, показваме dialog
     */

        if (coin == null || coin.getId() == null) {
            return;
        }

        cryptoRepository.loadCoinDetails(coin.getId(), new CryptoRepository.RepositoryCallback<com.google.gson.JsonObject>() {
            @Override
            public void onSuccess(com.google.gson.JsonObject data) {
                runOnUiThread(() -> {
                    String changeText;

                    if (coin.getPriceChangePercentage24h() == null) {
                        changeText = "N/A";
                    } else {
                        double change = coin.getPriceChangePercentage24h();

                        if (change > 0) {
                            changeText = "Up " + String.format(Locale.US, "%.2f%%", change);
                        } else if (change < 0) {
                            changeText = "Down " + String.format(Locale.US, "%.2f%%", Math.abs(change));
                        } else {
                            changeText = "No change 0.00%";
                        }
                    }

                    String message =
                            "Coin id: " + coin.getId()
                                    + "\nSymbol: " + coin.getSymbol()
                                    + "\nPrice: $" + coin.getCurrentPrice()
                                    + "\nLast 24h: " + changeText;

                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(MainActivity.this)
                            .setTitle(coin.getName())
                            .setMessage(message)
                            .setPositiveButton("OK", null)
                            .show();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> showError(message));
            }
        });
    }

    private void showLoading() {
    /*
    Показваме ProgressBar, докато зареждаме coins.
    Скриваме status text-а, защото още не знаем дали има грешка или празен списък.
     */

        binding.loadingProgressBar.setVisibility(View.VISIBLE);
        binding.statusTextView.setVisibility(View.GONE);
    }

    private void hideLoading() {
        binding.loadingProgressBar.setVisibility(View.GONE);
    }

    private void showStatusMessage(String message) {
    /*
    Това показва текст на екрана при error или empty state.
    Например: "No coins available." или "No internet connection..."
     */

        binding.statusTextView.setText(message);
        binding.statusTextView.setVisibility(View.VISIBLE);
    }

    private void hideStatusMessage() {
        binding.statusTextView.setVisibility(View.GONE);
    }


}
