package com.example.cryptotracker;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.cryptotracker.databinding.ActivityCoinDetailBinding;
import com.example.cryptotracker.model.Coin;
import com.example.cryptotracker.repository.CryptoRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Locale;

public class CoinDetailActivity extends AppCompatActivity {

    public static final String EXTRA_COIN_ID = "coin_id";
    public static final String EXTRA_COIN_NAME = "coin_name";
    public static final String EXTRA_COIN_SYMBOL = "coin_symbol";
    public static final String EXTRA_COIN_IMAGE = "coin_image";
    public static final String EXTRA_COIN_PRICE = "coin_price";
    public static final String EXTRA_COIN_CHANGE = "coin_change";
    public static final String EXTRA_COIN_MARKET_CAP = "coin_market_cap";
    public static final String EXTRA_COIN_VOLUME = "coin_volume";

    private ActivityCoinDetailBinding binding;
    private CryptoRepository cryptoRepository;
    private Coin coin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCoinDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cryptoRepository = new CryptoRepository(this);
        coin = getCoinFromIntent();

        binding.statusTextView.setVisibility(View.GONE);

        setupBackButton();
        setupFavoriteButton();
        showBasicCoinInfo();
        loadFavoriteState();
        loadCoinDetails();
        loadMarketChart();
    }

    private Coin getCoinFromIntent() {
        Coin selectedCoin = new Coin();

        selectedCoin.setId(getIntent().getStringExtra(EXTRA_COIN_ID));
        selectedCoin.setName(getIntent().getStringExtra(EXTRA_COIN_NAME));
        selectedCoin.setSymbol(getIntent().getStringExtra(EXTRA_COIN_SYMBOL));
        selectedCoin.setImage(getIntent().getStringExtra(EXTRA_COIN_IMAGE));

        if (getIntent().hasExtra(EXTRA_COIN_PRICE)) {
            selectedCoin.setCurrentPrice(getIntent().getDoubleExtra(EXTRA_COIN_PRICE, 0));
        }

        if (getIntent().hasExtra(EXTRA_COIN_CHANGE)) {
            selectedCoin.setPriceChangePercentage24h(getIntent().getDoubleExtra(EXTRA_COIN_CHANGE, 0));
        }

        if (getIntent().hasExtra(EXTRA_COIN_MARKET_CAP)) {
            selectedCoin.setMarketCap(getIntent().getLongExtra(EXTRA_COIN_MARKET_CAP, 0));
        }

        if (getIntent().hasExtra(EXTRA_COIN_VOLUME)) {
            selectedCoin.setTotalVolume(getIntent().getLongExtra(EXTRA_COIN_VOLUME, 0));
        }

        return selectedCoin;
    }

    private void setupBackButton() {
        binding.backButton.setOnClickListener(view -> finish());
    }

    private void setupFavoriteButton() {
        binding.favoriteDetailButton.setOnClickListener(view -> toggleFavorite(view));
    }

    private void showBasicCoinInfo() {
        binding.coinNameTextView.setText(getTextOrDefault(coin.getName()));
        binding.coinSymbolTextView.setText(getSymbolText());
        binding.priceTextView.setText(formatPrice(coin.getCurrentPrice()));
        binding.changeTextView.setText(formatChange(coin.getPriceChangePercentage24h()));
        binding.changeTextView.setTextColor(getChangeColor(coin.getPriceChangePercentage24h()));
        binding.marketCapTextView.setText("Market cap: " + formatLongValue(coin.getMarketCap()));
        binding.volumeTextView.setText("Volume: " + formatLongValue(coin.getTotalVolume()));

        Glide.with(this)
                .load(coin.getImage())
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(binding.coinImageView);
    }

    private String getSymbolText() {
        if (coin.getSymbol() == null || coin.getSymbol().isEmpty()) {
            return "N/A";
        }

        return coin.getSymbol().toUpperCase(Locale.ROOT);
    }

    private void loadFavoriteState() {
        cryptoRepository.loadFavoriteCoinIds(new CryptoRepository.RepositoryCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> favoriteCoinIds) {
                runOnUiThread(() -> {
                    boolean isFavorite = favoriteCoinIds != null && favoriteCoinIds.contains(coin.getId());
                    setFavoriteIcon(isFavorite);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> setFavoriteIcon(false));
            }
        });
    }

    private void toggleFavorite(View starView) {
        cryptoRepository.toggleFavorite(coin, new CryptoRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isFavorite) {
                runOnUiThread(() -> setFavoriteIcon(isFavorite));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> showStatusMessage(message));
            }
        });
    }

    private void setFavoriteIcon(boolean isFavorite) {
        ImageButton favoriteButton = binding.favoriteDetailButton;

        if (isFavorite) {
            favoriteButton.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            favoriteButton.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }

    private void loadCoinDetails() {
        if (coin.getId() == null) {
            binding.statusTextView.setText("Coin details not available.");
            return;
        }

        binding.loadingProgressBar.setVisibility(View.VISIBLE);

        cryptoRepository.loadCoinDetails(coin.getId(), new CryptoRepository.RepositoryCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject data) {
                runOnUiThread(() -> {
                    binding.loadingProgressBar.setVisibility(View.GONE);
                    showApiDetails(data);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    binding.loadingProgressBar.setVisibility(View.GONE);
                    showStatusMessage(message);
                });
            }
        });
    }

    private void showApiDetails(JsonObject data) {
        JsonObject marketData = getObject(data, "market_data");

        binding.athTextView.setText("All time high: " + formatUsdFromObject(getObject(marketData, "ath")));
        binding.atlTextView.setText("All time low: " + formatUsdFromObject(getObject(marketData, "atl")));
        binding.supplyTextView.setText("Circulating supply: " + formatDoubleValue(getDouble(marketData, "circulating_supply")));

        String description = getDescription(data);
        binding.descriptionTextView.setText(description);
    }

    private void loadMarketChart() {
        if (coin.getId() == null) {
            return;
        }

        cryptoRepository.loadCoinMarketChart(coin.getId(), new CryptoRepository.RepositoryCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject data) {
                runOnUiThread(() -> showChartInfo(data));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> binding.chartTextView.setText("Chart data not available."));
            }
        });
    }

    private void showChartInfo(JsonObject data) {
        JsonArray prices = data.getAsJsonArray("prices");

        if (prices == null || prices.size() == 0) {
            binding.chartTextView.setText("Chart data not available.");
            return;
        }

        binding.chartTextView.setText("7 day price points: " + prices.size());
    }

    private String getDescription(JsonObject data) {
        JsonObject description = getObject(data, "description");

        if (description == null || !description.has("en") || description.get("en").isJsonNull()) {
            return "No description available.";
        }

        String htmlDescription = description.get("en").getAsString();

        if (htmlDescription.trim().isEmpty()) {
            return "No description available.";
        }

        return Html.fromHtml(htmlDescription, Html.FROM_HTML_MODE_LEGACY).toString().trim();
    }

    private JsonObject getObject(JsonObject object, String key) {
        if (object == null || !object.has(key) || object.get(key).isJsonNull()) {
            return null;
        }

        return object.getAsJsonObject(key);
    }

    private Double getDouble(JsonObject object, String key) {
        if (object == null || !object.has(key) || object.get(key).isJsonNull()) {
            return null;
        }

        return object.get(key).getAsDouble();
    }

    private String formatUsdFromObject(JsonObject object) {
        Double value = getDouble(object, "usd");
        return formatPrice(value);
    }

    private String formatPrice(Double price) {
        if (price == null) {
            return "N/A";
        }

        return String.format(Locale.US, "$%.2f", price);
    }

    private String formatChange(Double change) {
        if (change == null) {
            return "N/A";
        }

        if (Math.abs(change) < 0.005) {
            return "0.00%";
        }

        return String.format(Locale.US, "%.2f%%", change);
    }

    private int getChangeColor(Double change) {
        if (change == null || Math.abs(change) < 0.005) {
            return Color.DKGRAY;
        }

        if (change > 0) {
            return Color.rgb(0, 128, 0);
        }

        return Color.rgb(180, 0, 0);
    }

    private String formatLongValue(Long value) {
        if (value == null) {
            return "N/A";
        }

        return String.format(Locale.US, "%,d", value);
    }

    private String formatDoubleValue(Double value) {
        if (value == null) {
            return "N/A";
        }

        return String.format(Locale.US, "%,.2f", value);
    }

    private String getTextOrDefault(String text) {
        if (text == null || text.isEmpty()) {
            return "N/A";
        }

        return text;
    }

    private void showStatusMessage(String message) {
        binding.statusTextView.setText(message);
        binding.statusTextView.setVisibility(View.VISIBLE);
    }
}
