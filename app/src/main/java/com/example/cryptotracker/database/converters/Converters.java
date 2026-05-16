package com.example.cryptotracker.database.converters;

import androidx.room.TypeConverter;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Converters {

    @TypeConverter // Казваме на Room как да превърне Date в Long, защото Room може да пази Long в базата.
    public static Long fromDate(Date date) {
        if (date == null) {
            return null;
        }

        return date.getTime(); // Date -> milliseconds timestamp
    }

    @TypeConverter // Казваме на Room как да върне Long обратно към Date.
    public static Date toDate(Long timestamp) {
        if (timestamp == null) {
            return null;
        }

        return new Date(timestamp); // milliseconds timestamp -> Date
    }

    @TypeConverter // Превръща List<String> в един String, защото Room не може директно да пази List.
    public static String fromStringList(List<String> list) {
        if (list == null) {
            return null;
        }

        return String.join(",", list); // Например ["usd", "eur"] -> "usd,eur"
    }

    @TypeConverter // Превръща String обратно към List<String>.
    public static List<String> toStringList(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        return Arrays.asList(value.split(",")); // Например "usd,eur" -> ["usd", "eur"]
    }
}
