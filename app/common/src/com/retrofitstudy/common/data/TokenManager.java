package com.retrofitstudy.common.data;

public class TokenManager {
    private static String sToken;

    public static synchronized void updateToken(String token) {
        sToken = token;
    }

    public static String getToken() {
        return sToken;
    }
}
