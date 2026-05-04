package com.example.cryptotracker.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.cryptotracker.database.entities.CoinEntity;

import java.util.List;

@Dao // Това е DAO interface.T ук има database операции. Data Access Object

public interface CoinDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Ако вече има coin със същото id, замени стария запис с новия.

    void insertCoins(List<CoinEntity> coins);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCoin(CoinEntity coin);

    @Query("SELECT * FROM coins ORDER BY marketCap DESC")
    List<CoinEntity> getAllCoinsByMarketCap();

    @Query("SELECT * FROM coins WHERE name LIKE '%' || :searchText || '%' OR symbol LIKE '%' || :searchText || '%' ORDER BY marketCap DESC")
    List<CoinEntity> searchCoins(String searchText);

    @Query("SELECT * FROM coins ORDER BY currentPrice DESC")
    List<CoinEntity> getCoinsByPriceDesc();

    @Query("SELECT * FROM coins ORDER BY priceChangePercentage24h DESC")
    List<CoinEntity> getTopGainers();

    @Query("SELECT * FROM coins ORDER BY priceChangePercentage24h ASC")
    List<CoinEntity> getTopLosers();

    @Query("SELECT * FROM coins WHERE id = :coinId LIMIT 1")
    CoinEntity getCoinById(String coinId);

    @Query("DELETE FROM coins")
    void deleteAllCoins();
}
