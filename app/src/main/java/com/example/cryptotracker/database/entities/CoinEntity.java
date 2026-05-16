package com.example.cryptotracker.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "coins")
public class CoinEntity {

    @PrimaryKey
    @NonNull
    private String id;

    private String symbol;
    private String name;
    private String image;
    private Double currentPrice;
    private Long marketCap;
    private Long totalVolume;
    private Double priceChangePercentage24h;
    private Long lastUpdated;

    public CoinEntity() {
    }

    @Ignore
    public CoinEntity(@NonNull String id, String symbol, String name, String image,
                      Double currentPrice, Long marketCap, Long totalVolume,
                      Double priceChangePercentage24h, Long lastUpdated) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.image = image;
        this.currentPrice = currentPrice;
        this.marketCap = marketCap;
        this.totalVolume = totalVolume;
        this.priceChangePercentage24h = priceChangePercentage24h;
        this.lastUpdated = lastUpdated;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(Double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public Long getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(Long marketCap) {
        this.marketCap = marketCap;
    }

    public Long getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(Long totalVolume) {
        this.totalVolume = totalVolume;
    }

    public Double getPriceChangePercentage24h() {
        return priceChangePercentage24h;
    }

    public void setPriceChangePercentage24h(Double priceChangePercentage24h) {
        this.priceChangePercentage24h = priceChangePercentage24h;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
