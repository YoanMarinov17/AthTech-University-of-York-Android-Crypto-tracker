package com.example.cryptotracker.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cryptotracker.R;
import com.example.cryptotracker.databinding.ItemCoinBinding;
import com.example.cryptotracker.model.Coin;
import android.graphics.Color;


import java.util.Locale;

public class CoinViewHolder extends RecyclerView.ViewHolder {

    private final ItemCoinBinding binding; // Binding за item_coin.xml. Дава достъп до views в един RecyclerView ред.

    public CoinViewHolder(@NonNull ItemCoinBinding binding) {
        super(binding.getRoot()); // RecyclerView.ViewHolder очаква root view-а на item layout-а.
        this.binding = binding;
    }

    public void bind(Coin coin) {
        /*
        bind() означава:
        вземи данните от един Coin object
        и ги сложи във views на един ред.
         */

        binding.coinNameTextView.setText(coin.getName()); // Показваме името, например Bitcoin.

        if (coin.getSymbol() != null) { // Symbol идва от API-то и може теоретично да липсва.
            binding.coinSymbolTextView.setText(coin.getSymbol().toUpperCase(Locale.ROOT)); // Показваме symbol-а, например BTC.
        } else {
            binding.coinSymbolTextView.setText("N/A");
        }

        bindPriceData(coin); // Слагаме цената и 24h промяната в реда.

        Glide.with(binding.getRoot().getContext()) // Glide зарежда image URL-а във ImageView.
                .load(coin.getImage())
                .placeholder(R.mipmap.ic_launcher) // Какво да се вижда, докато картинката зарежда.
                .error(R.mipmap.ic_launcher) // Какво да се вижда, ако картинката не се зареди.
                .into(binding.coinImageView);


    }

    private void bindPriceData(Coin coin) {
        /*
        Този метод слага цената и 24h промяната в TextView-овете.
        Държим го отделно, за да е по-подреден bind() методът.
         */

        if (coin.getCurrentPrice() != null) {
            binding.coinPriceTextView.setText(String.format(Locale.US, "$%.2f", coin.getCurrentPrice()));
        } else {
            binding.coinPriceTextView.setText("N/A");
        }

        if (coin.getPriceChangePercentage24h() != null) {
            double change = coin.getPriceChangePercentage24h();

            if (Math.abs(change) < 0.005) {
                binding.coinChangeTextView.setText("0.00%");
                binding.coinChangeTextView.setTextColor(Color.DKGRAY); // Почти 0% го показваме като неутрална промяна.
            } else if (change > 0) {
                binding.coinChangeTextView.setText(String.format(Locale.US, "%.2f%%", change));
                binding.coinChangeTextView.setTextColor(Color.rgb(0, 128, 0)); // Зелено при положителна промяна.
            } else {
                binding.coinChangeTextView.setText(String.format(Locale.US, "%.2f%%", change));
                binding.coinChangeTextView.setTextColor(Color.rgb(180, 0, 0)); // Червено при отрицателна промяна.
            }
        } else {
            binding.coinChangeTextView.setText("N/A");
            binding.coinChangeTextView.setTextColor(Color.DKGRAY);
        }

    }


}
