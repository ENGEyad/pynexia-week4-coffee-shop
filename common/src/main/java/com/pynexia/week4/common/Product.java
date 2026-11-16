package com.pynexia.week4.common;

public final class Product {
    public final String id;
    public final String name;
    public final String category;
    public final String detail;
    public final double price;

    public Product(String id, String name, String category, String detail, double price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.detail = detail;
        this.price = price;
    }
}
