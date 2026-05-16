package com.example.cryptotracker.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "portfolio") // Казваме на Room, че този клас ще бъде таблица с име portfolio.
public class PortfolioEntity {

    @PrimaryKey(autoGenerate = true) // Room сам ще генерира уникално id за всеки portfolio запис.
    private long id;

    @NonNull
    private String coinId = ""; // CoinGecko id на coin-а, например "bitcoin". Така знаем кой coin притежаваме.

    private Double amount; // Колко от този coin има потребителят. Например 0.05 BTC.

    private Double averageBuyPrice; // Средната цена, на която потребителят е купил coin-а.

    private Long createdAt; // Кога е създаден portfolio записът.

    private Long updatedAt; // Кога е обновен последно portfolio записът.

    public PortfolioEntity() {
    }

    @Ignore
    public PortfolioEntity(@NonNull String coinId, Double amount, Double averageBuyPrice, Long createdAt, Long updatedAt) {
        this.coinId = coinId;
        this.amount = amount;
        this.averageBuyPrice = averageBuyPrice;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getAverageBuyPrice() {
        return averageBuyPrice;
    }

    public void setAverageBuyPrice(Double averageBuyPrice) {
        this.averageBuyPrice = averageBuyPrice;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
