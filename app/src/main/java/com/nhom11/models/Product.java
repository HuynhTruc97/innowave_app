package com.nhom11.models;

public class Product {
    public String product_id;
    public String name;
    public int category_id;
    public String default_variant_id;
    public byte[] thumbnail_url;
    public String description;
    public double original_price;
    public double discounted_price;
    public int is_active;
    public String created_at;
    public String updated_at;

    public Product(String product_id, String name, int category_id, String default_variant_id, byte[] thumbnail_url, String description, double original_price, double discounted_price, int is_active, String created_at, String updated_at) {
        this.product_id = product_id;
        this.name = name;
        this.category_id = category_id;
        this.default_variant_id = default_variant_id;
        this.thumbnail_url = thumbnail_url;
        this.description = description;
        this.original_price = original_price;
        this.discounted_price = discounted_price;
        this.is_active = is_active;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }
} 