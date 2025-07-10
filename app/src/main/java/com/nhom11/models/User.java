package com.nhom11.models;

public class User {
    public int user_id;
    public String full_name;
    public String email;
    public String phone_number;
    public String password_hash;
    public String created_at;
    public String updated_at;
    public String last_login_at;
    public int is_active;

    public User(int user_id, String full_name, String email, String phone_number, String password_hash, String created_at, String updated_at, String last_login_at, int is_active) {
        this.user_id = user_id;
        this.full_name = full_name;
        this.email = email;
        this.phone_number = phone_number;
        this.password_hash = password_hash;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.last_login_at = last_login_at;
        this.is_active = is_active;
    }
} 