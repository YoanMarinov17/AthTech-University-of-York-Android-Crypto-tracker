package com.example.cryptotracker.api;

import com.example.cryptotracker.utils.ApiException;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class ErrorInterceptor implements Interceptor {
// това е код, който стои между приложението и сървъра и проверява дали отговорът от сървъра е успешен.
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request()); // Пусни заявката да продължи към сървъра и ми върни отговора.
        /*
        chain.request() е текущата заявка.
        chain.proceed(...) казва: “продължи с тази заявка”.
         */

        if (response.isSuccessful()) {
            return response;
        } // Ако респонса е успешен, го върни

        int statusCode = response.code();
        String technicalMessage = "HTTP error " + statusCode;
        String userMessage = getUserMessageForStatusCode(statusCode);

        response.close(); // Когато response-ът е неуспешен и няма да го върнем нормално, го затваряме, за да освободим ресурси.



        throw new ApiException(technicalMessage, statusCode, userMessage);
        // ук не връщаме response. Вместо това хвърляме наша грешка с информацията която сме взели от ApiException
    }

    private String getUserMessageForStatusCode(int statusCode) {
        switch (statusCode) {
            case 400:
                return "Invalid request. Please try again.";
            case 401:
                return "Authentication failed. Please check your API access.";
            case 403:
                return "Access denied. You do not have permission for this request.";
            case 404:
                return "Requested data was not found.";
            case 429:
                return "Too many requests. Please wait a moment and try again.";
            case 500:
            case 502:
            case 503:
            case 504:
                return "CoinGecko server is temporarily unavailable. Please try again later.";
            default:
                return "Something went wrong while loading data.";
        }
    }
}
