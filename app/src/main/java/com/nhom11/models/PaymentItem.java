package com.nhom11.models;

public class PaymentItem {
    public String productName;
    public String variantName;
    public int quantity;
    public double price;
    public double originalPrice;
    public byte[] thumbnailUrl; // Ảnh của variant
    public String variantId;

    public PaymentItem(String productName, String variantName, int quantity, double price, double originalPrice, byte[] thumbnailUrl, String variantId) {
        this.productName = productName;
        this.variantName = variantName;
        this.quantity = quantity;
        this.price = price;
        this.originalPrice = originalPrice;
        this.thumbnailUrl = thumbnailUrl;
        this.variantId = variantId;
    }
} 