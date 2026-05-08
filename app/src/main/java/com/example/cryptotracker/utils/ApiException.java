package com.example.cryptotracker.utils;

import java.io.IOException;

public class ApiException extends IOException {

    /*
    Това е наш custom exception class. (наша собствена грешка)

    Ние си правим собствен тип грешка, за да пазим:
    - HTTP status code;
    - technical message;
    - user-friendly message.

    Важно:
    Наследяваме IOException, а не RuntimeException,
    защото OkHttp/Retrofit очакват network/interceptor грешките
    да бъдат IOException, за да ги пратят към onFailure(),
    вместо приложението да crash-не.
     */

    private final int statusCode;
    private final String userMessage; // С това тук ще показваме грешката по четим и ясен начин за крайния юзър.

    public ApiException(String message, int statusCode, String userMessage) {
        super(message);
        this.statusCode = statusCode;
        this.userMessage = userMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
