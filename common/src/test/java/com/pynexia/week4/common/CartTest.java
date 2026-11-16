package com.pynexia.week4.common;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.HashMap;
import java.util.Map;

public class CartTest {
    @Test public void quantitiesAndSubtotalUpdateTogether() {
        Cart cart = new Cart();
        cart.add("apple"); cart.add("apple"); cart.add("milk");
        Map<String, Double> prices = new HashMap<>(); prices.put("apple", 2.49); prices.put("milk", 2.39);
        assertEquals(3, cart.itemCount());
        assertEquals(7.37, cart.subtotal(prices), 0.001);
        cart.removeOne("apple");
        assertEquals(4.88, cart.subtotal(prices), 0.001);
    }

    @Test public void removingLastItemRemovesTheLine() {
        Cart cart = new Cart(); cart.add("coffee"); cart.removeOne("coffee");
        assertEquals(0, cart.quantity("coffee"));
        assertEquals(0, cart.itemCount());
    }

    @Test public void unknownPriceIsExcludedFromSubtotal() {
        Cart cart = new Cart(); cart.add("unknown");
        assertEquals(0, cart.subtotal(new HashMap<>()), 0.001);
    }
}
