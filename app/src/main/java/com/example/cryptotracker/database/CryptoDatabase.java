package com.example.cryptotracker.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.cryptotracker.database.converters.Converters;
import com.example.cryptotracker.database.dao.AlertDao;
import com.example.cryptotracker.database.dao.CoinDao;
import com.example.cryptotracker.database.dao.HistoricalPriceDao;
import com.example.cryptotracker.database.dao.PortfolioDao;
import com.example.cryptotracker.database.dao.WatchlistDao;
import com.example.cryptotracker.database.entities.AlertEntity;
import com.example.cryptotracker.database.entities.CoinEntity;
import com.example.cryptotracker.database.entities.HistoricalPriceEntity;
import com.example.cryptotracker.database.entities.PortfolioEntity;
import com.example.cryptotracker.database.entities.WatchlistCoinCrossRef;
import com.example.cryptotracker.database.entities.WatchlistEntity;

@Database(
        entities = {
                CoinEntity.class,
                WatchlistEntity.class,
                WatchlistCoinCrossRef.class,
                PortfolioEntity.class,
                AlertEntity.class,
                HistoricalPriceEntity.class
        }, // Тук казваме на Room кои класове трябва да станат таблици в базата.
        version = 6, // Увеличаваме версията, защото добавихме migration от version 5 към version 6.
        exportSchema = false // Засега не караме Room да записва schema файл. По-късно може да го включим за пълна migration история.
)
@TypeConverters({Converters.class}) // Казваме на Room да използва нашите converter методи.
public abstract class CryptoDatabase extends RoomDatabase {

    private static CryptoDatabase instance; // Тук пазим една обща database instance, за да не създаваме база всеки път.

    public abstract CoinDao coinDao(); // Дава достъп до методите за таблицата coins.

    public abstract WatchlistDao watchlistDao(); // Дава достъп до методите за watchlists и връзките между watchlists и coins.

    public abstract PortfolioDao portfolioDao(); // Дава достъп до методите за portfolio таблицата.

    public abstract AlertDao alertDao(); // Дава достъп до методите за alerts таблицата.

    public abstract HistoricalPriceDao historicalPriceDao(); // Дава достъп до методите за historical_prices таблицата.

    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            /*
            Migration означава:
            кажи на Room как старата база да стане нова, без да изтриваме данните.

            Тук казваме:
            ако базата е version 5 и трябва да стане version 6,
            добави нова колона note в таблицата alerts.
             */
            database.execSQL("ALTER TABLE alerts ADD COLUMN note TEXT");
        }
    };

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
                    .addMigrations(MIGRATION_5_6) // Казваме на Room как да мигрира базата от version 5 към version 6.
                    .build(); // Създай database instance с горните настройки.
        }

        return instance; // Върни готовата database instance.
    }
}
