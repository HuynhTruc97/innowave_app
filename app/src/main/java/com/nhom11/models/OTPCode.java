package com.nhom11.models;

public class OTPCode {
    public int otp_id;
    public int user_id;
    public String code;
    public String expires_at;
    public int is_used;
    public String created_at;

    public OTPCode(int otp_id, int user_id, String code, String expires_at, int is_used, String created_at) {
        this.otp_id = otp_id;
        this.user_id = user_id;
        this.code = code;
        this.expires_at = expires_at;
        this.is_used = is_used;
        this.created_at = created_at;
    }
} 