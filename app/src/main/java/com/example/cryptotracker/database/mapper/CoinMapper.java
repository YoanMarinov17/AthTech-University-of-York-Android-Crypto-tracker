package com.example.cryptotracker.database.mapper;

import com.example.cryptotracker.database.entities.CoinEntity;
import com.example.cryptotracker.model.Coin;

import java.util.ArrayList;
import java.util.List;

public class CoinMapper {

    /*
    Когато API върне Coin, не го записваме директно като CoinEntity, защото са различни типове.
    Трябва да го превърнем.
    Mapper е клас, който прави това превръщане:
            Coin -> CoinEntity
            CoinEntity -> Coin
     */

    private CoinMapper() {
    }

    public static CoinEntity toEntity(Coin coin) { // Дай ми Coin от API. Ще ти върна CoinEntity за Room.

        return new CoinEntity(
                coin.getId(),
                coin.getSymbol(),
                coin.getName(),
                coin.getImage(),
                coin.getCurrentPrice(),
                coin.getMarketCap(),
                coin.getTotalVolume(),
                coin.getPriceChangePercentage24h(),
                System.currentTimeMillis()
        );
    }

    public static List<CoinEntity> toEntityList(List<Coin> coins) { // Това превръща списък от API coins в списък от database entities.

        /*
        За всеки Coin в списъка coins:
           превърни го в CoinEntity
            добави го в списъка coinEntities
         */

        List<CoinEntity> coinEntities = new ArrayList<>();

        for (Coin coin : coins) {
            coinEntities.add(toEntity(coin));
        }

        return coinEntities;
    }
}
