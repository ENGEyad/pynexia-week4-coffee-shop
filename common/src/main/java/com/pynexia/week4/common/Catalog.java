package com.pynexia.week4.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class Catalog {
    private Catalog() { }

    public static List<Product> grocery() {
        return new ArrayList<>(Arrays.asList(
            new Product("apple", "Royal Gala Apples", "Fruits", "Crisp · 4 pieces", 2.49),
            new Product("banana", "Ripe Bananas", "Fruits", "Sweet · 6 pieces", 1.79),
            new Product("avocado", "Hass Avocado", "Fruits", "Ready to eat · 2 pieces", 3.25),
            new Product("tomato", "Vine Tomatoes", "Vegetables", "Fresh · 500 g", 2.19),
            new Product("spinach", "Baby Spinach", "Vegetables", "Washed · 120 g", 2.59),
            new Product("milk", "Whole Milk", "Dairy", "Organic · 1 L", 2.39),
            new Product("yogurt", "Greek Yogurt", "Dairy", "Plain · 500 g", 3.49),
            new Product("bread", "Sourdough Bread", "Bakery", "Fresh baked · loaf", 4.20),
            new Product("oats", "Rolled Oats", "Pantry", "Whole grain · 1 kg", 3.10),
            new Product("coffee", "Ground Coffee", "Pantry", "Medium roast · 250 g", 6.49)
        ));
    }

    public static List<Product> retail() {
        return new ArrayList<>(Arrays.asList(
            new Product("headphones", "Studio Headphones", "Electronics", "Wireless · 30 hour battery", 79.00),
            new Product("speaker", "Pocket Bluetooth Speaker", "Electronics", "Water resistant · compact", 42.50),
            new Product("lamp", "Arc Desk Lamp", "Home", "Warm dimmable LED", 36.00),
            new Product("bottle", "Insulated Bottle", "Home", "Keeps drinks cold for 24 hours", 24.00),
            new Product("shirt", "Everyday Cotton Tee", "Fashion", "Soft organic cotton", 22.00),
            new Product("bag", "Canvas Weekender", "Fashion", "Roomy · recycled cotton", 49.00),
            new Product("serum", "Hydration Serum", "Beauty", "Lightweight daily care", 18.50),
            new Product("notebook", "Linen Journal", "Stationery", "160 pages · lay-flat binding", 15.00)
        ));
    }

    public static List<Product> coffee() {
        return new ArrayList<>(Arrays.asList(
            new Product("flatwhite", "Flat White", "Espresso", "Double espresso · silky milk", 4.50),
            new Product("latte", "Vanilla Oat Latte", "Espresso", "Espresso · oat milk · vanilla", 5.25),
            new Product("americano", "Americano", "Espresso", "Double espresso · hot water", 3.75),
            new Product("filter", "Pour-over", "Brewed", "Single origin · hand poured", 4.75),
            new Product("coldbrew", "Slow Cold Brew", "Cold", "Steeped 18 hours · smooth finish", 5.00),
            new Product("matcha", "Iced Matcha", "Tea", "Ceremonial matcha · oat milk", 5.50),
            new Product("croissant", "Butter Croissant", "Bakery", "Flaky · baked this morning", 3.25),
            new Product("banana_bread", "Banana Bread", "Bakery", "Walnuts · toasted", 3.75)
        ));
    }

    public static List<Product> filter(List<Product> source, String query, String category) {
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        String selected = category == null ? "All" : category;
        List<Product> result = new ArrayList<>();
        for (Product product : source) {
            boolean matchesText = needle.isEmpty() || product.name.toLowerCase(Locale.ROOT).contains(needle)
                || product.detail.toLowerCase(Locale.ROOT).contains(needle)
                || product.category.toLowerCase(Locale.ROOT).contains(needle);
            boolean matchesCategory = selected.equals("All") || product.category.equalsIgnoreCase(selected);
            if (matchesText && matchesCategory) result.add(product);
        }
        return result;
    }
}
