package com.example.cryptotracker.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cryptotracker.database.entities.PortfolioEntity;

import java.util.List;

@Dao // Казваме на Room, че това е DAO с операции за таблицата portfolio.
public interface PortfolioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Ако има конфликт, Room ще замени стария запис с новия.
    void insertPortfolioItem(PortfolioEntity portfolioItem); // Добавя coin към portfolio-то.

    @Update // Обновява съществуващ portfolio запис.
    void updatePortfolioItem(PortfolioEntity portfolioItem);

    @Delete // Изтрива конкретен portfolio запис.
    void deletePortfolioItem(PortfolioEntity portfolioItem);

    @Query("SELECT * FROM portfolio ORDER BY updatedAt DESC") // Вземи всички portfolio записи, подредени по последна промяна.
    List<PortfolioEntity> getAllPortfolioItems();

    @Query("SELECT * FROM portfolio WHERE coinId = :coinId LIMIT 1") // Вземи portfolio запис за конкретен coin.
    PortfolioEntity getPortfolioItemByCoinId(String coinId);

    @Query("DELETE FROM portfolio WHERE coinId = :coinId") // Изтрий portfolio запис по coin id.
    void deletePortfolioItemByCoinId(String coinId);

    @Query("SELECT COUNT(*) FROM portfolio") // Преброй колко различни coins има в portfolio-то.
    int getPortfolioItemCount();
}
