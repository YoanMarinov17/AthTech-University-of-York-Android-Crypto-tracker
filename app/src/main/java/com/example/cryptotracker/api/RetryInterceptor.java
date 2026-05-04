package com.example.cryptotracker.api;



import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RetryInterceptor implements Interceptor {
// Ако заявката се провали, не се отказваме веднага. Пробваме пак след малко. Това се нарича exponential backoff

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_DELAY_MS = 1000; // 1000ms = 1 sec

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request(); // Тук взимаме текущата заявка. = GET /coins/markets

        IOException lastException = null; // lastException пази последната network грешка, ако има такава.

        Response response = null; // response пази последния response, ако сървърът е върнал нещо.

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                response = chain.proceed(request); // Това изпраща заявката нататък.


                /*
                Ако response е успешен, върни го.
                Или ако response кодът не е от тези, които си струва да retry-ваме, пак го върни.
                 */
                if (response.isSuccessful() || !shouldRetry(response.code())) {
                    return response;
                }
                response.close();
            } catch (IOException exception) {
                /*
                Тук влизаме ако има проблем като:
                няма интернет;
                timeout;
                connection failure.
                 */
                lastException = exception; // Запазваме последната грешка.
            }

            if (attempt < MAX_RETRIES) {
                waitBeforeRetry(attempt);
            }
        }

        if (lastException != null) {
            throw lastException; // Ако имаме вече грешка, която е записана малко по-горе, хвърли я
        }

        return response;
    }

    private boolean shouldRetry(int statusCode) {
        return statusCode == 408 ||
                statusCode == 429 ||
                statusCode == 500 ||
                statusCode == 502 ||
                statusCode == 503 ||
                statusCode == 504;
    }

    private void waitBeforeRetry(int attempt) throws IOException {
        long delay = INITIAL_DELAY_MS * (long) Math.pow(2, attempt);

        try {
            Thread.sleep(delay); // Това кара текущия background thread да изчака.
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("Retry interrupted", exception); // Thread-ът беше прекъснат, спри retry логиката и хвърли IOException.
        }
    }
}
