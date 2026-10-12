package com.example.shop;

import java.util.ArrayList;
import java.util.List;

/** Class under test of the fixture. */
public class Cart {

    private final List<Integer> prices = new ArrayList<>();

    public void add(int price) {
        if (price < 0) {
            throw new IllegalArgumentException("price must not be negative");
        }
        prices.add(price);
    }

    public int total() {
        int sum = 0;
        for (int price : prices) {
            sum += price;
        }
        return sum;
    }

    public int size() {
        return prices.size();
    }
}
