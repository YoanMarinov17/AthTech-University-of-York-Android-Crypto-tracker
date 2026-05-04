package com.example.cryptotracker.api;

import android.content.Context;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://api.coingecko.com/api/v3/";
    private static final long CACHE_SIZE = 10 * 1024 * 1024; // 10 MB cache

    private static Retrofit retrofit;

    private RetrofitClient() {
    }

    public static Retrofit getRetrofitInstance(Context context) {

        if (retrofit == null) {
            OkHttpClient okHttpClient = createOkHttpClient(context.getApplicationContext()); // Тук създавам инстанция на метода отдолу createOkHttpClient
            // и после го задавам като client на инстанцията retrofit

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        /* Ако още няма Retrofit:
        направи OkHttpClient
        направи Retrofit
        кажи му base URL
        кажи му да използва OkHttpClient
        кажи му да използва Gson
         Върни Retrofit
         */

        return retrofit;
    }

    private static OkHttpClient createOkHttpClient(Context context) {
        //OkHttpClient е реалния http клиент

        //HttpLoggingInterceptor е interceptor, който записва в Logcat какви заявки и отговори минават.
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(); // Interceptor е като контролна точка, през която минава заявката или отговорът.
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        File cacheDirectory = new File(context.getCacheDir(), "http-cache"); // Тук казваме къде Android да пази HTTP cache файловете.
        Cache cache = new Cache(cacheDirectory, CACHE_SIZE); // Тук създаваме OkHttp cache с размер 10 MB.

        return new OkHttpClient.Builder()
                .cache(cache) // Казваме на OkHttpClient да използва този cache за HTTP response-и.
                .addInterceptor(new CacheInterceptor(context)) // решава дали да използва cache при offline.
                .addInterceptor(new RetryInterceptor()) // Първо retry решава дали да пробва пак. / пробва пак при временни грешки
                .addInterceptor(new ErrorInterceptor()) // После error interceptor превръща окончателната грешка в ApiException. /  превръща неуспешния response в смислена грешка.
                .addInterceptor(loggingInterceptor) // Logging interceptor логва заявките.
                .connectTimeout(15, TimeUnit.SECONDS) // Колко време чакаме да се свържем със сървъра.
                .readTimeout(20, TimeUnit.SECONDS) // Колко време чакаме да получим отговор.
                .writeTimeout(20, TimeUnit.SECONDS) // Колко време чакаме при изпращане на данни.
                .build();
    }

    public static CoinGeckoApiService getApiService(Context context) {
        return getRetrofitInstance(context).create(CoinGeckoApiService.class);
    }

}
