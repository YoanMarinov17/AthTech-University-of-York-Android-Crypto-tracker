package com.example.cryptotracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.example.cryptotracker.R;
import com.example.cryptotracker.databinding.ItemCoinBinding;
import com.example.cryptotracker.model.Coin;

import java.util.ArrayList;
import java.util.List;

public class CoinAdapter extends ListAdapter<Coin, CoinViewHolder> {

    private OnCoinClickListener onCoinClickListener;
    private OnFavoriteClickListener onFavoriteClickListener;
    private List<String> favoriteCoinIds = new ArrayList<>(); // Тук пазим id-тата на coins, които са във Favorites.

    public CoinAdapter(OnCoinClickListener onCoinClickListener, OnFavoriteClickListener onFavoriteClickListener) {
        super(DIFF_CALLBACK);
        this.onCoinClickListener = onCoinClickListener;
        this.onFavoriteClickListener = onFavoriteClickListener;
    }

    public void setFavoriteCoinIds(List<String> favoriteCoinIds) {
        /*
        MainActivity подава тук id-тата на favorite coins.
        После adapter-ът обновява редовете, за да покаже правилните звезди.
         */

        if (favoriteCoinIds == null) {
            this.favoriteCoinIds = new ArrayList<>();
        } else {
            this.favoriteCoinIds = favoriteCoinIds;
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CoinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        /*
        RecyclerView вика този метод, когато му трябва нов ред.
        Тук превръщаме item_coin.xml в реален View.
         */

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemCoinBinding binding = ItemCoinBinding.inflate(inflater, parent, false);

        return new CoinViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CoinViewHolder holder, int position) {
        Coin coin = getItem(position);

        holder.bind(coin);
        setStarIcon(holder, coin); // Показваме жълта или празна звезда според Favorites.

        holder.itemView.setOnClickListener(view -> {
            if (onCoinClickListener != null) {
                onCoinClickListener.onCoinClick(coin);
            }
        });

        holder.itemView.findViewById(R.id.favoriteButton).setOnClickListener(view -> {
            /*
            Adapter-ът само подава click-а към MainActivity.
            MainActivity ще реши дали звездата да стане жълта или празна.
             */

            if (onFavoriteClickListener != null) {
                onFavoriteClickListener.onFavoriteClick(coin, view);
            }
        });
    }

    private void setStarIcon(CoinViewHolder holder, Coin coin) {
        /*
        Ако coin id-то е във favoriteCoinIds, звездата е жълта.
        Ако не е, звездата е празна.
         */

        android.widget.ImageButton starButton = holder.itemView.findViewById(R.id.favoriteButton);

        if (coin != null && favoriteCoinIds.contains(coin.getId())) {
            starButton.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            starButton.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }

    private static final DiffUtil.ItemCallback<Coin> DIFF_CALLBACK = new DiffUtil.ItemCallback<Coin>() {
        @Override
        public boolean areItemsTheSame(@NonNull Coin oldCoin, @NonNull Coin newCoin) {
            /*
            Това пита: един и същ coin ли е?
            Сравняваме по id, защото id-то е най-стабилно.
             */

            return oldCoin.getId() != null && oldCoin.getId().equals(newCoin.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Coin oldCoin, @NonNull Coin newCoin) {
            /*
            Това пита: данните на coin-а същите ли са?
            Използваме isSame(...), защото някои стойности от API-то може да са null.
             */

            return isSame(oldCoin.getName(), newCoin.getName())
                    && isSame(oldCoin.getSymbol(), newCoin.getSymbol())
                    && isSame(oldCoin.getCurrentPrice(), newCoin.getCurrentPrice());
        }
    };

    public interface OnCoinClickListener {
        void onCoinClick(Coin coin);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Coin coin, View starView);
    }

    private static boolean isSame(Object oldValue, Object newValue) {
        if (oldValue == null && newValue == null) {
            return true;
        }

        if (oldValue == null || newValue == null) {
            return false;
        }

        return oldValue.equals(newValue);
    }
}
