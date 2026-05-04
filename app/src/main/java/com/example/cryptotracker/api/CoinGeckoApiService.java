package com.example.cryptotracker.api;

import com.example.cryptotracker.model.Coin;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CoinGeckoApiService {

    @GET("coins/markets")
    Call<List<Coin>> getMarketCoins(
            @Query("vs_currency") String currency,
            @Query("order") String order,
            @Query("per_page") int perPage,
            @Query("page") int page,
            @Query("sparkline") boolean sparkline,
            @Query("price_change_percentage") String priceChangePercentage
    );
    @GET("coins/{id}")
    Call<JsonObject> getCoinDetails(
            @Path("id") String coinId,
            @Query("localization") boolean localization,
            @Query("tickers") boolean tickers,
            @Query("market_data") boolean marketData,
            @Query("community_data") boolean communityData,
            @Query("developer_data") boolean developerData,
            @Query("sparkline") boolean sparkline
    );

    @GET("coins/{id}/market_chart")
    Call<JsonObject> getCoinMarketChart(
            @Path("id") String coinId,
            @Query("vs_currency") String currency,
            @Query("days") String days
    );

    @GET("search/trending")
    Call<JsonObject> getTrendingCoins();
}

