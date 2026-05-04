package com.example.cryptotracker.api;

import android.content.Context;

import com.example.cryptotracker.utils.NetworkUtils;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class CacheInterceptor implements Interceptor {

    // Когато има интернет, позволяваме response-ът да се счита fresh за 60 секунди.
    private static final int ONLINE_CACHE_SECONDS = 60;

    // Ако няма интернет, позволяваме да се използват cached данни до 7 дни назад.
    private static final int OFFLINE_CACHE_DAYS = 7;

    private final Context context;

    public CacheInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        // взимаме текущата заявка.
        Request request = chain.request();

        if (!NetworkUtils.isNetworkAvailable(context)) {
            CacheControl cacheControl = new CacheControl.Builder()
                    .onlyIfCached() // Не ходи до интернет. Вземи само от cache.
                    .maxStale(OFFLINE_CACHE_DAYS, java.util.concurrent.TimeUnit.DAYS) // Позволи cached response да е стар до 7 дни.
                    .build();

            request = request.newBuilder()
                    .cacheControl(cacheControl)
                    .build();
        }

        Response response = chain.proceed(request); // изпращаме заявката нататък.

        if (NetworkUtils.isNetworkAvailable(context)) {
            return response.newBuilder()
                    .header("Cache-Control", "public, max-age=" + ONLINE_CACHE_SECONDS)
                    .removeHeader("Pragma")
                    .build(); // Този response може да се кешира за 60 секунди.

        } else {
            return response.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=" + OFFLINE_CACHE_DAYS * 24 * 60 * 60)
                    .removeHeader("Pragma")
                    .build(); // Използвай cached version, ако има такава.

        }
    }
}
