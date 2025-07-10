package com.nhom11.models;

public class OrderItem {
    public int orderItemId;
    public int orderId;
    public String variantId;
    public int quantity;
    public double unitPrice;
    public double subTotal;

    public OrderItem(int orderItemId, int orderId, String variantId, int quantity, double unitPrice, double subTotal) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.variantId = variantId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subTotal = subTotal;
    }
} 