package com.example.cryptotracker.repository;

import android.content.Context;

import com.example.cryptotracker.api.CoinGeckoApiService;
import com.example.cryptotracker.api.RetrofitClient;
import com.example.cryptotracker.database.CryptoDatabase;
import com.example.cryptotracker.database.dao.CoinDao;
import com.example.cryptotracker.database.dao.WatchlistDao;
import com.example.cryptotracker.database.entities.CoinEntity;
import com.example.cryptotracker.database.entities.WatchlistCoinCrossRef;
import com.example.cryptotracker.database.entities.WatchlistEntity;
import com.example.cryptotracker.database.mapper.CoinMapper;
import com.example.cryptotracker.model.Coin;
import com.example.cryptotracker.utils.ApiException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CryptoRepository {

    private final CoinGeckoApiService apiService; // API service за заявки към CoinGecko.
    private final CoinDao coinDao; // DAO за четене/запис на coins в Room.
    private final WatchlistDao watchlistDao; // DAO за Favorites/Watchlist логиката.
    private final ExecutorService databaseExecutor; // Background thread за Room операции.

    public CryptoRepository(Context context) {
        /*
        Repository-то получава Context, защото:
        - RetrofitClient има нужда от Context за HTTP cache.
        - CryptoDatabase има нужда от Context, за да отвори Room базата.
         */

        Context appContext = context.getApplicationContext();

        apiService = RetrofitClient.getApiService(appContext);

        CryptoDatabase database = CryptoDatabase.getInstance(appContext);
        coinDao = database.coinDao();
        watchlistDao = database.watchlistDao();

        databaseExecutor = Executors.newSingleThreadExecutor();
    }

    public void loadMarketCoins(RepositoryCallback<List<Coin>> callback) {
        /*
        1. Опитваме да заредим coins от API.
        2. Ако API успее, записваме резултата в Room.
        3. Ако API fail-не, пробваме saved coins от Room.
         */

        Call<List<Coin>> call = apiService.getMarketCoins(
                "usd",
                "market_cap_desc",
                50,
                1,
                false,
                "24h"
        );

        call.enqueue(new Callback<List<Coin>>() {
            @Override
            public void onResponse(Call<List<Coin>> call, Response<List<Coin>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<Coin> coins = response.body();

                    saveCoinsToDatabase(coins);

                    callback.onSuccess(coins);
                } else {
                    loadCoinsFromDatabase(callback, "No coins found from API.");
                }
            }

            @Override
            public void onFailure(Call<List<Coin>> call, Throwable throwable) {
                String errorMessage = getErrorMessage(throwable);
                loadCoinsFromDatabase(callback, errorMessage);
            }
        });
    }

    public void loadCoinDetails(String coinId, RepositoryCallback<JsonObject> callback) {
        /*
        Зарежда подробна информация за един coin.
         */

        Call<JsonObject> call = apiService.getCoinDetails(
                coinId,
                false,
                false,
                true,
                false,
                false,
                false
        );

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                handleJsonResponse(response, callback, "Coin details not found.");
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable throwable) {
                callback.onError(getErrorMessage(throwable));
            }
        });
    }

    public void loadCoinMarketChart(String coinId, RepositoryCallback<JsonObject> callback) {
        /*
        Зарежда historical market chart за coin.
         */

        Call<JsonObject> call = apiService.getCoinMarketChart(
                coinId,
                "usd",
                "7"
        );

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                handleJsonResponse(response, callback, "Historical data not found.");
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable throwable) {
                callback.onError(getErrorMessage(throwable));
            }
        });
    }

    public void loadTrendingCoins(RepositoryCallback<JsonObject> callback) {
        /*
        Зарежда trending coins от CoinGecko.
         */

        Call<JsonObject> call = apiService.getTrendingCoins();

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                handleJsonResponse(response, callback, "Trending coins not found.");
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable throwable) {
                callback.onError(getErrorMessage(throwable));
            }
        });
    }

    public void toggleFavorite(Coin coin, RepositoryCallback<Boolean> callback) {
        /*
        Toggle означава:
        ако coin не е във Favorites -> добави го
        ако coin вече е във Favorites -> махни го
         */

        if (coin == null || coin.getId() == null) {
            callback.onError("Coin cannot be changed.");
            return;
        }

        databaseExecutor.execute(() -> {
            long now = System.currentTimeMillis();

            WatchlistEntity favorites = watchlistDao.getWatchlistByName("Favorites");

            long favoritesId;

            if (favorites == null) {
                favorites = new WatchlistEntity("Favorites", now, now);
                favoritesId = watchlistDao.insertWatchlist(favorites);
            } else {
                favoritesId = favorites.getId();
            }

            int alreadyAdded = watchlistDao.isCoinInWatchlist(favoritesId, coin.getId());

            WatchlistCoinCrossRef crossRef = new WatchlistCoinCrossRef(
                    favoritesId,
                    coin.getId(),
                    now
            );

            if (alreadyAdded > 0) {
                watchlistDao.removeCoinFromWatchlist(crossRef);
                callback.onSuccess(false);
            } else {
                watchlistDao.addCoinToWatchlist(crossRef);
                callback.onSuccess(true);
            }
        });
    }

    public void loadFavoriteCoins(RepositoryCallback<List<Coin>> callback) {
        /*
        Зарежда coins, които са добавени във Favorites.
         */

        databaseExecutor.execute(() -> {
            WatchlistEntity favorites = watchlistDao.getWatchlistByName("Favorites");

            if (favorites == null) {
                callback.onSuccess(new ArrayList<>());
                return;
            }

            List<CoinEntity> favoriteCoinEntities = watchlistDao.getCoinsForWatchlist(favorites.getId());
            List<Coin> favoriteCoins = entityListToCoinList(favoriteCoinEntities);

            callback.onSuccess(favoriteCoins);
        });
    }

    public void loadFavoriteCoinIds(RepositoryCallback<List<String>> callback) {
        /*
        Зарежда само id-тата на coins, които са във Favorites.
        Например: bitcoin, ethereum, solana.

        Това ни трябва, за да знаем кои звезди да покажем жълти.
         */

        databaseExecutor.execute(() -> {
            WatchlistEntity favorites = watchlistDao.getWatchlistByName("Favorites");

            if (favorites == null) {
                callback.onSuccess(new ArrayList<>());
                return;
            }

            List<String> favoriteCoinIds = watchlistDao.getCoinIdsForWatchlist(favorites.getId());

            callback.onSuccess(favoriteCoinIds);
        });
    }

    private void handleJsonResponse(Response<JsonObject> response, RepositoryCallback<JsonObject> callback, String emptyMessage) {
        /*
        Малък helper за endpoints, които връщат JsonObject.
         */

        if (response.isSuccessful() && response.body() != null) {
            callback.onSuccess(response.body());
        } else {
            callback.onError(emptyMessage);
        }
    }

    private void saveCoinsToDatabase(List<Coin> coins) {
        databaseExecutor.execute(() -> {
            List<CoinEntity> coinEntities = CoinMapper.toEntityList(coins);
            coinDao.insertCoins(coinEntities);
        });
    }

    private void loadCoinsFromDatabase(RepositoryCallback<List<Coin>> callback, String originalErrorMessage) {
        /*
        Ако API не работи, пробваме да заредим последните coins от Room.
         */

        databaseExecutor.execute(() -> {
            List<CoinEntity> savedCoinEntities = coinDao.getAllCoinsByMarketCap();

            if (savedCoinEntities != null && !savedCoinEntities.isEmpty()) {
                List<Coin> savedCoins = entityListToCoinList(savedCoinEntities);
                callback.onSuccess(savedCoins);
            } else {
                callback.onError(originalErrorMessage);
            }
        });
    }

    private List<Coin> entityListToCoinList(List<CoinEntity> coinEntities) {
        /*
        Превръща List<CoinEntity> от Room обратно в List<Coin>.
         */

        List<Coin> coins = new ArrayList<>();

        for (CoinEntity entity : coinEntities) {
            Coin coin = new Coin();

            coin.setId(entity.getId());
            coin.setSymbol(entity.getSymbol());
            coin.setName(entity.getName());
            coin.setImage(entity.getImage());
            coin.setCurrentPrice(entity.getCurrentPrice());
            coin.setMarketCap(entity.getMarketCap());
            coin.setTotalVolume(entity.getTotalVolume());
            coin.setPriceChangePercentage24h(entity.getPriceChangePercentage24h());

            coins.add(coin);
        }

        return coins;
    }

    private String getErrorMessage(Throwable throwable) {
        /*
        Превръща technical грешки в по-разбираемо съобщение.
         */

        if (throwable instanceof ApiException) {
            ApiException apiException = (ApiException) throwable;
            return apiException.getUserMessage();
        }

        if (throwable instanceof SocketTimeoutException) {
            return "Request timed out. Showing saved data if available.";
        }

        if (throwable instanceof UnknownHostException) {
            return "No internet connection. Showing saved data if available.";
        }

        if (throwable instanceof JsonSyntaxException) {
            return "Data parsing error. Please try again later.";
        }

        return "Network error. Showing saved data if available.";
    }

    public interface RepositoryCallback<T> {
        /*
        Прост callback interface.
         */

        void onSuccess(T data);

        void onError(String message);
    }
}
