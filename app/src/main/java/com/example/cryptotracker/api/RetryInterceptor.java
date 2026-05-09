package com.example.cryptotracker.api;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RetryInterceptor implements Interceptor {
    // Ако заявката се провали временно, пробваме пак след малко.
    // Това се нарича exponential backoff: 1 секунда, после 2, после 4.

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_DELAY_MS = 1000; // 1000ms = 1 sec

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request(); // Взимаме текущата заявка. Пример: GET /coins/markets

        IOException lastException = null; // Тук пазим последната network грешка, ако има такава.

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                Response response = chain.proceed(request); // Изпращаме заявката.

                /*
                Ако response е успешен, го връщаме веднага.
                Ако status code не е подходящ за retry, пак го връщаме.
                 */
                if (response.isSuccessful() || !shouldRetry(response.code())) {
                    return response;
                }

                /*
                Ако това е последният опит, не затваряме response-а.
                Връщаме го към ErrorInterceptor, за да направи хубаво error message.
                 */
                if (attempt == MAX_RETRIES) {
                    return response;
                }

                response.close(); // Затваряме неуспешния response преди следващия retry.
            } catch (IOException exception) {
                lastException = exception; // Например: няма интернет, timeout, connection problem.

                if (attempt == MAX_RETRIES) {
                    throw lastException; // Ако няма повече опити, връщаме последната грешка.
                }
            }

            waitBeforeRetry(attempt); // Изчакваме малко преди следващия опит.
        }

        throw new IOException("Request failed after retries");
    }

    private boolean shouldRetry(int statusCode) {
        // Тези status codes обикновено са временни проблеми.
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
            Thread.sleep(delay); // Спираме само background thread-а, не UI thread-а.
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("Retry interrupted", exception);
        }
    }
}
