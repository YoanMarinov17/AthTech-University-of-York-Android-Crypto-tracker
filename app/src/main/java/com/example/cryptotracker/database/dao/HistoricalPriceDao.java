package com.example.cryptotracker.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.cryptotracker.database.entities.HistoricalPriceEntity;

import java.util.List;

@Dao // DAO за работа с таблицата historical_prices.
public interface HistoricalPriceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Записва списък с historical price точки.
    void insertHistoricalPrices(List<HistoricalPriceEntity> prices);

    @Query("SELECT * FROM historical_prices WHERE coinId = :coinId AND currency = :currency ORDER BY timestamp ASC") // Взима historical prices за coin, подредени по време.
    List<HistoricalPriceEntity> getHistoricalPricesForCoin(String coinId, String currency);

    @Query("DELETE FROM historical_prices WHERE coinId = :coinId AND currency = :currency") // Изтрива старите historical prices за coin и валута.
    void deleteHistoricalPricesForCoin(String coinId, String currency);

    @Query("SELECT COUNT(*) FROM historical_prices WHERE coinId = :coinId AND currency = :currency") // Проверява колко historical точки имаме кеширани.
    int getHistoricalPriceCount(String coinId, String currency);
}
