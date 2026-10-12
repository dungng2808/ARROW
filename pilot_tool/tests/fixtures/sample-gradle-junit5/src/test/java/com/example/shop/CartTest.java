package com.example.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CartTest {

    @Test
    void sumsPrices() {
        Cart cart = new Cart();
        cart.add(2);
        cart.add(3);
        assertEquals(5, cart.total());
    }
}
