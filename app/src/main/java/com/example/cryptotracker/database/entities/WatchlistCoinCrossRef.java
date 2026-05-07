package com.example.cryptotracker.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(
        tableName = "watchlist_coin_cross_ref",
        primaryKeys = {"watchlistId", "coinId"}
)
public class WatchlistCoinCrossRef {

    private long watchlistId;

    @NonNull
    private String coinId;

    private Long addedAt;

    public WatchlistCoinCrossRef() {
        coinId = "";
    }

    public WatchlistCoinCrossRef(long watchlistId, @NonNull String coinId, Long addedAt) {
        this.watchlistId = watchlistId;
        this.coinId = coinId;
        this.addedAt = addedAt;
    }

    public long getWatchlistId() {
        return watchlistId;
    }

    public void setWatchlistId(long watchlistId) {
        this.watchlistId = watchlistId;
    }

    @NonNull
    public String getCoinId() {
        return coinId;
    }

    public void setCoinId(@NonNull String coinId) {
        this.coinId = coinId;
    }

    public Long getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Long addedAt) {
        this.addedAt = addedAt;
    }
}
