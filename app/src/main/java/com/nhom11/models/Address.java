package com.nhom11.models;

public class Address {
    private int addressId;
    private int userId;
    private String recipientName;
    private String phoneNumber;
    private String streetAddress;
    private String ward;
    private String district;
    private String province;
    private int isDefault;
    private String createdAt;
    private String updatedAt;

    public Address(int addressId, int userId, String recipientName, String phoneNumber, String streetAddress, String ward, String district, String province, int isDefault, String createdAt, String updatedAt) {
        this.addressId = addressId;
        this.userId = userId;
        this.recipientName = recipientName;
        this.phoneNumber = phoneNumber;
        this.streetAddress = streetAddress;
        this.ward = ward;
        this.district = district;
        this.province = province;
        this.isDefault = isDefault;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Address() {}

    public int getAddressId() { return addressId; }
    public int getUserId() { return userId; }
    public String getRecipientName() { return recipientName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getStreetAddress() { return streetAddress; }
    public String getWard() { return ward; }
    public String getDistrict() { return district; }
    public String getProvince() { return province; }
    public int isDefault() { return isDefault; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    public void setDefault(int isDefault) { this.isDefault = isDefault; }
    public void setAddressId(int addressId) { this.addressId = addressId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }
    public void setWard(String ward) { this.ward = ward; }
    public void setDistrict(String district) { this.district = district; }
    public void setProvince(String province) { this.province = province; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
} 