package com.example.cryptotracker;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cryptotracker.adapter.CoinAdapter;
import com.example.cryptotracker.databinding.ActivityMainBinding;
import com.example.cryptotracker.model.Coin;
import com.example.cryptotracker.repository.CryptoRepository;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding; // Binding е мост между activity_main.xml и Java кода.
    private CryptoRepository cryptoRepository; // Repository-то управлява API + Room логиката.
    private CoinAdapter coinAdapter; // Adapter-ът превръща List<Coin> в редове в RecyclerView.

    @Override
    protected void onCreate(Bundle savedInstanceState) { // Android извиква onCreate, когато екранът се създава.
        super.onCreate(savedInstanceState); // Изпълнява стандартното Android създаване на Activity.

        binding = ActivityMainBinding.inflate(getLayoutInflater()); // Превръща XML layout-а в binding object.
        setContentView(binding.getRoot()); // Показва layout-а на екрана.

        cryptoRepository = new CryptoRepository(this); // Създаваме data слоя, който зарежда coins.

        setupRecyclerView(); // Първо подготвяме списъка.
        loadMarketCoins(); // После зареждаме данните.
    }

    private void setupRecyclerView() {
        coinAdapter = new CoinAdapter(); // Създаваме adapter-а.
        binding.coinsRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // Редовете ще са един под друг.
        binding.coinsRecyclerView.setAdapter(coinAdapter); // Свързваме RecyclerView с adapter-а.
    }

    private void loadMarketCoins() {
        coinAdapter.showLoading(); // Показваме loading ред вътре в списъка.

        cryptoRepository.loadMarketCoins(new CryptoRepository.RepositoryCallback<List<Coin>>() {
            @Override
            public void onSuccess(List<Coin> coins) {
                runOnUiThread(() -> showCoins(coins)); // UI промени винаги се правят на main thread.
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> showError(message)); // Ако има грешка, я показваме на екрана.
            }
        });
    }

    private void showCoins(List<Coin> coins) {
        coinAdapter.showCoins(coins); // Adapter-ът сам решава дали да покаже coins или empty ред.
    }

    private void showError(String message) {
        coinAdapter.showError(message); // Показваме error ред вътре в списъка.
    }
}
