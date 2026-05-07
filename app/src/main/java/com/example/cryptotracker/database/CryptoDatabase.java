package com.example.cryptotracker.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.cryptotracker.database.dao.CoinDao;
import com.example.cryptotracker.database.dao.WatchlistDao;
import com.example.cryptotracker.database.entities.CoinEntity;
import com.example.cryptotracker.database.entities.WatchlistCoinCrossRef;
import com.example.cryptotracker.database.entities.WatchlistEntity;
import com.example.cryptotracker.database.dao.PortfolioDao;
import com.example.cryptotracker.database.entities.PortfolioEntity;


@Database(
        entities = {
                CoinEntity.class,
                WatchlistEntity.class,
                WatchlistCoinCrossRef.class,
                PortfolioEntity.class
        }, // Тук казваме на Room кои класове трябва да станат таблици в базата.
        version = 3, // Увеличаваме версията, защото добавихме нови таблици към базата.
        exportSchema = false // Засега не караме Room да записва schema файл. По-късно може да го включим за migrations.
)
public abstract class CryptoDatabase extends RoomDatabase {

    private static CryptoDatabase instance; // Тук пазим една обща database instance, за да не създаваме база всеки път.

    public abstract CoinDao coinDao(); // Дава достъп до методите за таблицата coins.

    public abstract WatchlistDao watchlistDao(); // Дава достъп до методите за watchlists и връзките между watchlists и coins.

    public abstract PortfolioDao portfolioDao(); // Дава достъп до методите за portfolio таблицата.

    public static synchronized CryptoDatabase getInstance(Context context) {
        /*
        synchronized означава:
        ако два thread-а поискат базата едновременно,
        не позволявай да се създадат две различни instances.
         */

        if (instance == null) { // Ако базата още не е създадена, създай я.
            instance = Room.databaseBuilder(
                            context.getApplicationContext(), // Използваме application context, защото базата живее дълго.
                            CryptoDatabase.class, // Казваме кой е главният Room database class.
                            "crypto_database" // Това е името на database файла в storage-а на приложението.
                    )
                    .fallbackToDestructiveMigration() // Временно: ако schema-та се промени, Room ще изтрие старата база и ще създаде нова.
                    .build(); // Създай database instance с горните настройки.
        }

        return instance; // Върни готовата database instance.
    }
}
