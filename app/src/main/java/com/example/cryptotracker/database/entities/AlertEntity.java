package com.example.cryptotracker.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "alerts") // Казваме на Room, че този клас ще бъде таблица с име alerts.
public class AlertEntity {

    @PrimaryKey(autoGenerate = true) // Room сам ще генерира уникално id за всеки alert.
    private long id;

    @NonNull
    private String coinId = ""; // CoinGecko id на coin-а, за който е alert-ът. Например "bitcoin".

    private Double targetPrice; // Цената, при която alert-ът трябва да се активира.

    private String conditionType; // Например "above" или "below" - дали следим цена над или под targetPrice.

    private String note;


    private Boolean isActive; // Дали alert-ът е активен. Ако е false, няма да го проверяваме.

    private Long createdAt; // Кога е създаден alert-ът.

    private Long updatedAt; // Кога е променен последно alert-ът.

    public AlertEntity() {
    }

    @Ignore
    public AlertEntity(@NonNull String coinId, Double targetPrice, String conditionType,
                       Boolean isActive, Long createdAt, Long updatedAt) {
        this.coinId = coinId;
        this.targetPrice = targetPrice;
        this.conditionType = conditionType;
        this.note = null;
        this.isActive = isActive;
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

    public Double getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(Double targetPrice) {
        this.targetPrice = targetPrice;
    }

    public String getConditionType() {
        return conditionType;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note){
        this.note = note;
    }
}
