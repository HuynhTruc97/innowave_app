package com.nhom11.models;

public class Review {
    public int review_id;
    public int user_id;
    public String product_id;
    public int rating;
    public String comment;
    public String created_at;
    public String updated_at;
    public int is_active;

    public Review(int review_id, int user_id, String product_id, int rating, String comment, String created_at, String updated_at, int is_active) {
        this.review_id = review_id;
        this.user_id = user_id;
        this.product_id = product_id;
        this.rating = rating;
        this.comment = comment;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.is_active = is_active;
    }
} 