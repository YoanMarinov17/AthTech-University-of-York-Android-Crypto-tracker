package com.example.cryptotracker.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.cryptotracker.database.dao.CoinDao;
import com.example.cryptotracker.database.entities.CoinEntity;

@Database(entities = {CoinEntity.class}, version = 1, exportSchema = false)
/*
Тази база съдържа таблицата CoinEntity.
Версията на базата е 1.
Не export-ваме schema файл засега.
 */
public abstract class CryptoDatabase extends RoomDatabase {

    private static CryptoDatabase instance;

    public abstract CoinDao coinDao();

    public static synchronized CryptoDatabase getInstance(Context context) { // synchronized -> Ако два thread-а поискат базата едновременно, не позволявай да се създадат две instances.

        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            CryptoDatabase.class,
                            "crypto_database"
                    )
                    .build();
        }

        return instance;
    }
}
