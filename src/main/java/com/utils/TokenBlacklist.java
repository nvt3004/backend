package com.utils;

import java.util.HashSet;
import java.util.Set;

public class TokenBlacklist {
    private static final Set<String> blacklistedTokens = new HashSet<>();

    public static void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }

    public static boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}
