package com.saxion.proj.tfms.commons.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    // Tokens expire after 2 years
    private static final long EXPIRY_MILLIS = 2L * 365 * 24 * 60 * 60 * 1000;

    // Map to store blacklisted tokens with their blacklist time
    private Map<String, Long> blacklist = new ConcurrentHashMap<>();

    public void blackListToken(String token) {
        blacklist.put(token, System.currentTimeMillis());
    }

    public boolean isTokenBlacklisted(String token) {
        Long time = blacklist.get(token);
        if (time == null) return false;
        if (System.currentTimeMillis() - time > EXPIRY_MILLIS) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }

    @Scheduled(cron = "0 0 * * * *") // every hour
    public void cleanupExpiredTokens() {
        long now = System.currentTimeMillis();
        blacklist.entrySet().removeIf(entry -> now - entry.getValue() > EXPIRY_MILLIS);
    }
}
