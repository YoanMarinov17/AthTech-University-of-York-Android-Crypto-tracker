package com.example.cryptotracker.model;

import com.google.gson.annotations.SerializedName;

public class Coin {

    private String id;
    private String symbol;
    private String name;
    private String image;

    @SerializedName("current_price")
    private Double currentPrice;

    @SerializedName("market_cap")
    private Long marketCap;

    @SerializedName("total_volume")
    private Long totalVolume;

    @SerializedName("price_change_percentage_24h")
    private Double priceChangePercentage24h;

    public Coin() {
    }

    public Coin(String id, String symbol, String name, String image, Double currentPrice,
                Long marketCap, Long totalVolume, Double priceChangePercentage24h) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.image = image;
        this.currentPrice = currentPrice;
        this.marketCap = marketCap;
        this.totalVolume = totalVolume;
        this.priceChangePercentage24h = priceChangePercentage24h;
    }

    public String getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public Double getCurrentPrice() {
        return currentPrice;
    }

    public Long getMarketCap() {
        return marketCap;
    }

    public Long getTotalVolume() {
        return totalVolume;
    }

    public Double getPriceChangePercentage24h() {
        return priceChangePercentage24h;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setCurrentPrice(Double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setMarketCap(Long marketCap) {
        this.marketCap = marketCap;
    }

    public void setTotalVolume(Long totalVolume) {
        this.totalVolume = totalVolume;
    }

    public void setPriceChangePercentage24h(Double priceChangePercentage24h) {
        this.priceChangePercentage24h = priceChangePercentage24h;
    }
}
