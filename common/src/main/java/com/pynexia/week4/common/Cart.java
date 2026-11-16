package com.pynexia.week4.common;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Cart {
    private final Map<String, Integer> quantities = new LinkedHashMap<>();

    public void add(String id) { quantities.put(id, quantity(id) + 1); }
    public void removeOne(String id) { int count = quantity(id); if (count <= 1) quantities.remove(id); else quantities.put(id, count - 1); }
    public int quantity(String id) { Integer count = quantities.get(id); return count == null ? 0 : count; }
    public int itemCount() { int total = 0; for (int count : quantities.values()) total += count; return total; }
    public double subtotal(Map<String, Double> unitPrices) {
        double total = 0;
        for (Map.Entry<String, Integer> item : quantities.entrySet()) {
            Double price = unitPrices.get(item.getKey());
            if (price != null) total += price * item.getValue();
        }
        return Math.round(total * 100.0) / 100.0;
    }
    public Map<String, Integer> entries() { return new LinkedHashMap<>(quantities); }
    public void clear() { quantities.clear(); }
}
