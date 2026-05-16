package com.example.cryptotracker.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.cryptotracker.database.entities.CoinEntity;
import com.example.cryptotracker.database.entities.WatchlistCoinCrossRef;
import com.example.cryptotracker.database.entities.WatchlistEntity;

import java.util.List;

@Dao // Казваме на Room, че това е DAO - мястото с методи за работа с базата.
public interface WatchlistDao {

    @Insert // Вкарва нов watchlist в таблицата watchlists.
    long insertWatchlist(WatchlistEntity watchlist); // Връща id-то на новосъздадения watchlist.

    @Insert(onConflict = OnConflictStrategy.IGNORE) // Ако coin-ът вече е в този watchlist, не crash-вай, а игнорирай.
    void addCoinToWatchlist(WatchlistCoinCrossRef crossRef); // Добавя връзка между watchlist и coin.

    @Delete // Изтрива конкретен ред от таблицата watchlist_coin_cross_ref.
    void removeCoinFromWatchlist(WatchlistCoinCrossRef crossRef); // Махаме coin от watchlist.

    @Query("SELECT * FROM watchlists ORDER BY updatedAt DESC") // Вземи всички watchlists, подредени по последна промяна.
    List<WatchlistEntity> getAllWatchlists();

    @Query("SELECT * FROM watchlists WHERE id = :watchlistId LIMIT 1") // Вземи само един watchlist по неговото id.
    WatchlistEntity getWatchlistById(long watchlistId);

    @Query("DELETE FROM watchlist_coin_cross_ref WHERE watchlistId = :watchlistId") // Изтрий всички coin връзки за даден watchlist.
    void deleteCoinsForWatchlist(long watchlistId);

    @Query("DELETE FROM watchlists WHERE id = :watchlistId") // Изтрий самия watchlist от таблицата watchlists.
    void deleteWatchlistById(long watchlistId);

    @Query("SELECT coins.* FROM coins " +
            "INNER JOIN watchlist_coin_cross_ref " +
            "ON coins.id = watchlist_coin_cross_ref.coinId " +
            "WHERE watchlist_coin_cross_ref.watchlistId = :watchlistId " +
            "ORDER BY coins.marketCap DESC") // Вземи всички coins, които принадлежат на конкретен watchlist.
    List<CoinEntity> getCoinsForWatchlist(long watchlistId);

    @Query("SELECT COUNT(*) FROM watchlist_coin_cross_ref WHERE watchlistId = :watchlistId") // Преброй колко coins има в даден watchlist.
    int getCoinCountForWatchlist(long watchlistId);

    @Query("SELECT * FROM watchlists WHERE name = :name LIMIT 1")
    WatchlistEntity getWatchlistByName(String name);

    @Query("SELECT COUNT(*) FROM watchlist_coin_cross_ref WHERE watchlistId = :watchlistId AND coinId = :coinId")
    int isCoinInWatchlist(long watchlistId, String coinId);

    @Query("SELECT coinId FROM watchlist_coin_cross_ref WHERE watchlistId = :watchlistId")
    List<String> getCoinIdsForWatchlist(long watchlistId);

}
