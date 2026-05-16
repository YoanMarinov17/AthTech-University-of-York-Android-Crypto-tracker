package com.example.cryptotracker.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cryptotracker.database.entities.AlertEntity;

import java.util.List;

@Dao // Казваме на Room, че това е DAO с операции за таблицата alerts.
public interface AlertDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Ако има конфликт, Room ще замени стария alert с новия.
    void insertAlert(AlertEntity alert); // Добавя нов alert в базата.

    @Update // Обновява съществуващ alert.
    void updateAlert(AlertEntity alert);

    @Delete // Изтрива конкретен alert.
    void deleteAlert(AlertEntity alert);

    @Query("SELECT * FROM alerts ORDER BY createdAt DESC") // Взима всички alerts, подредени от най-нов към най-стар.
    List<AlertEntity> getAllAlerts();

    @Query("SELECT * FROM alerts WHERE isActive = 1 ORDER BY createdAt DESC") // Взима само активните alerts.
    List<AlertEntity> getActiveAlerts();

    @Query("SELECT * FROM alerts WHERE coinId = :coinId ORDER BY createdAt DESC") // Взима alerts за конкретен coin.
    List<AlertEntity> getAlertsForCoin(String coinId);

    @Query("SELECT * FROM alerts WHERE id = :alertId LIMIT 1") // Взима един alert по неговото id.
    AlertEntity getAlertById(long alertId);

    @Query("UPDATE alerts SET isActive = 0, updatedAt = :updatedAt WHERE id = :alertId") // Изключва alert, без да го трие от базата.
    void disableAlert(long alertId, Long updatedAt);

    @Query("DELETE FROM alerts WHERE id = :alertId") // Изтрива alert по id.
    void deleteAlertById(long alertId);
}
