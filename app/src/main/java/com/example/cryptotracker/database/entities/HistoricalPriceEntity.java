package com.example.cryptotracker.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "historical_prices") // Таблица за исторически цени, които ще се ползват за offline charts.
public class HistoricalPriceEntity {

    @PrimaryKey(autoGenerate = true) // Room сам ще генерира уникално id за всяка price точка.
    private long id;

    @NonNull
    private String coinId = ""; // CoinGecko id на coin-а, например "bitcoin".

    private Long timestamp; // Времето на тази price точка. Обикновено е milliseconds timestamp.

    private Double price; // Цената на coin-а в този момент.

    private String currency; // В каква валута е цената, например "usd" или "eur".

    private Long cachedAt; // Кога сме записали тази historical price точка в Room.

    public HistoricalPriceEntity() {
    }

    @Ignore
    public HistoricalPriceEntity(@NonNull String coinId, Long timestamp, Double price, String currency, Long cachedAt) {
        this.coinId = coinId;
        this.timestamp = timestamp;
        this.price = price;
        this.currency = currency;
        this.cachedAt = cachedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getCoinId() {
        return coinId;
    }

    public void setCoinId(@NonNull String coinId) {
        this.coinId = coinId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Long getCachedAt() {
        return cachedAt;
    }

    public void setCachedAt(Long cachedAt) {
        this.cachedAt = cachedAt;
    }
}
