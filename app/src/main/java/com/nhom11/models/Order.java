package com.nhom11.models;

public class Order {
    public int orderId;
    public int userId;
    public int addressId;
    public int voucherId;
    public double totalAmount;
    public double shippingFee;
    public String status;
    public String createdAt;
    public String updatedAt;

    public Order(int orderId, int userId, int addressId, int voucherId, double totalAmount, double shippingFee, String status, String createdAt, String updatedAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.addressId = addressId;
        this.voucherId = voucherId;
        this.totalAmount = totalAmount;
        this.shippingFee = shippingFee;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
} 