package com.nhom11.innowave;
import java.io.Serializable;

public class CartItemLite implements Serializable {
    public int cartItemId;
    public String productId;
    public String variantId;
    public int quantity;
    public String productName;
    public String variantName;
    public double price;
    public CartItemLite(int cartItemId, String productId, String variantId, int quantity, String productName, String variantName, double price) {
        this.cartItemId = cartItemId;
        this.productId = productId;
        this.variantId = variantId;
        this.quantity = quantity;
        this.productName = productName;
        this.variantName = variantName;
        this.price = price;
    }
} 