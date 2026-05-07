package com.example.cryptotracker;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cryptotracker.api.CoinGeckoApiService;
import com.example.cryptotracker.api.RetrofitClient;
import com.example.cryptotracker.database.CryptoDatabase;
import com.example.cryptotracker.database.dao.CoinDao;
import com.example.cryptotracker.database.entities.CoinEntity;
import com.example.cryptotracker.database.mapper.CoinMapper;
import com.example.cryptotracker.databinding.ActivityMainBinding;
import com.example.cryptotracker.model.Coin;
import com.example.cryptotracker.utils.ApiException;

import com.example.cryptotracker.database.dao.WatchlistDao;
import com.example.cryptotracker.database.entities.WatchlistCoinCrossRef;
import com.example.cryptotracker.database.entities.WatchlistEntity;


import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding; // Тя ни позволява да достъпваме неща от activity_main.xml. Binding е удобен мост между XML и Java.

    /*
    С binding -> binding.apiResultTextView
    Без binding -> TextView textView = findViewById(R.id.apiResultTextView);
     */

    private CoinDao coinDao; // coinDao ще използваме за работа с таблицата coins.

    private WatchlistDao watchlistDao; // watchlistDao ще използваме за работа с watchlists и връзката watchlist -> coin.

    private ExecutorService databaseExecutor; // databaseExecutor ще използваме, за да пускаме database операции във background thread.

    @Override
    protected void onCreate(Bundle savedInstanceState) { // onCreate е метод, който Android извиква, когато екранът се създава.
        super.onCreate(savedInstanceState); // Android, изпълни нормалното поведение за създаване на Activity.

        binding = ActivityMainBinding.inflate(getLayoutInflater()); // Android взима xml файла и го превръща в Java binding object
        setContentView(binding.getRoot()); // Покажи този XML layout на екрана.
        /*
        binding.getRoot() е най-горният view в XML-а. В моя случай това е ConstraintLayout.
         */

        CryptoDatabase database = CryptoDatabase.getInstance(this); // Вземи database instance.
        coinDao = database.coinDao(); // Вземи CoinDao от базата.
        watchlistDao = database.watchlistDao(); // // Вземи WatchlistDao от базата.

        databaseExecutor = Executors.newSingleThreadExecutor(); // Създай един background thread за database работа.

        testMarketCoinsApi(); // Когато екранът се отвори, пробвай да заредиш coins от API.
    }

    private void testMarketCoinsApi() {
        binding.apiResultTextView.setText("Loading coins..."); // Покажи текста "Loading coins..."
        /*
        android:text в XML = начален текст.
        setText в Java = смяна на текста по време на работа.
         */

        // Дай ми обект, чрез който мога да извиквам CoinGecko API методите.
        CoinGeckoApiService apiService = RetrofitClient.getApiService(this);
        // CoinGeckoApiService е интерфейсът който създадохме и тази променлива ни дава достъп до апи-методите
        // RetrofitClient.getApiService(this) -> RetrofitClient, дай ми готов CoinGeckoApiService.
        /*
        RetrofitClient е класът, който сглобява Retrofit, OkHttp, Gson, cache и interceptors.
        getApiService(...) е методът, който прави това:
        "return getRetrofitInstance(context).create(CoinGeckoApiService.class);"
        Тоест Retrofit взима нашия interface и създава реален обект, който може да изпраща заявки.
         */

        Call<List<Coin>> call = apiService.getMarketCoins(
                "usd",
                "market_cap_desc",
                10,
                1,
                false,
                "24h"
        );

        /*
        Създай променлива call.
        Тя е API заявка.
        Когато успее, ще върне List<Coin>.
         */

        call.enqueue(new Callback<List<Coin>>() { // Изпрати тази заявка асинхронно. / Пусни заявката във фонов режим и не блокирай екрана.
            // Callback = Код, който ще се изпълни по-късно, когато заявката приключи.
            /*
            Когато приключиш, извикай един от тези два метода:
            onResponse
            onFailure
             */

            @Override
            public void onResponse(Call<List<Coin>> call, Response<List<Coin>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    /*
                    response.isSuccessful -> проверява дали HTTP code е между 200 и 299.
                    response.body() != null -> проверява дали response има тяло. (List of coins)
                    !response.body().isEmpty() -> проверява дали списъкът не е празен.
                     */

                    List<Coin> coins = response.body(); // Запазваме списъка от API-то в променлива, за да не пишем response.body() много пъти.

                    saveCoinsToDatabase(coins); // Изпращаме coins към метод, който ще ги запише в Room базата.

                    Coin firstCoin = coins.get(0); // Вземи първия coin от списъка.
                    Coin secondCoin = coins.get(1); // Вземи втория coin от списъка.
                    Coin thirdCoin = coins.get(2); // Вземи третия coin от списъка.

                    String result =
                            firstCoin.getName()
                                    + "\nPrice: $" + firstCoin.getCurrentPrice()
                                    + "\n24h change: " + firstCoin.getPriceChangePercentage24h() + "%"
                                    + "\n\n"
                                    + secondCoin.getName()
                                    + "\nPrice: $" + secondCoin.getCurrentPrice()
                                    + "\n24h change: " + secondCoin.getPriceChangePercentage24h() + "%"
                                    + "\n\n"
                                    + thirdCoin.getName()
                                    + "\nPrice: $" + thirdCoin.getCurrentPrice()
                                    + "\n24h change: " + thirdCoin.getPriceChangePercentage24h() + "%";

                    binding.apiResultTextView.setText(result);

                } else {
                    binding.apiResultTextView.setText("No coins found.");
                }
            }

            @Override
            public void onFailure(Call<List<Coin>> call, Throwable throwable) { // Throwable throwable е обектът, който описва грешката.
                if (throwable instanceof ApiException) { // Тази грешка от нашия тип ApiException ли е?

                    ApiException apiException = (ApiException) throwable;
                    binding.apiResultTextView.setText(apiException.getUserMessage());
                } else {
                    binding.apiResultTextView.setText("Network error. Please check your internet connection.");
                }
            }
        });
    }

    private void saveCoinsToDatabase(List<Coin> coins) {
        /*
        Този метод записва coins от API-то в Room базата.

        Важно:
        Room database операции не трябва да се правят на main thread,
        защото могат да са бавни и да блокират UI.
         */

        databaseExecutor.execute(() -> { // Пусни този код във background thread.
            List<CoinEntity> coinEntities = CoinMapper.toEntityList(coins); // Превърни List<Coin> от API-то в List<CoinEntity> за Room.

            coinDao.insertCoins(coinEntities); // Запиши coins в таблицата coins чрез CoinDao.
            /*
            insertCoins идва от CoinDao.
            CoinDao е interface-ът, в който описваме database операциите.
            Room генерира реалния код зад този метод.
             */

            loadCoinsFromDatabase();
            testWatchlistDatabase();


        });
    }

    private void loadCoinsFromDatabase() {
        databaseExecutor.execute(() -> {
            List<CoinEntity> savedCoins = coinDao.getAllCoinsByMarketCap();

            runOnUiThread(() -> { // runOnUiThread(() -> {

                binding.apiResultTextView.append("\n\nSaved in Room: " + savedCoins.size() + " coins");
            });
        });
    }

    private void testWatchlistDatabase() {
    /*
    Това е временен тест за watchlist частта.
    Целта е да проверим дали можем:
    1. да създадем watchlist
    2. да добавим coin към него
    3. да прочетем coins от този watchlist
     */

        databaseExecutor.execute(() -> { // Room операции се правят във background thread, за да не блокират UI.
            Long now = System.currentTimeMillis(); // Взимаме текущото време, за да го запишем като createdAt/updatedAt.

            WatchlistEntity favorites = new WatchlistEntity("Favorites", now, now); // Създаваме нов watchlist с име Favorites.

            long watchlistId = watchlistDao.insertWatchlist(favorites); // Записваме watchlist-а в Room и получаваме неговото auto-generated id.

            WatchlistCoinCrossRef bitcoinInFavorites = new WatchlistCoinCrossRef(
                    watchlistId,
                    "bitcoin",
                    now
            ); // Създаваме връзка: този watchlist съдържа coin с id bitcoin.

            watchlistDao.addCoinToWatchlist(bitcoinInFavorites); // Записваме връзката в таблицата watchlist_coin_cross_ref.

            int coinCount = watchlistDao.getCoinCountForWatchlist(watchlistId); // Проверяваме колко coins има в този watchlist.

            runOnUiThread(() -> { // Връщаме се на main thread, защото ще променяме TextView.
                binding.apiResultTextView.append("\n\nWatchlist test: Favorites has " + coinCount + " coin(s)");
            });
        });
    }

}
