package com.example.portallock;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PortalLockEndState {
    private static final Map<UUID, Integer> PENDING_END_CONSUME = new ConcurrentHashMap<>();
    private static final java.util.Set<UUID> FAIL_NOTICE = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private PortalLockEndState() {}

    public static void markPendingEnd(UUID playerId, int age) {
        PENDING_END_CONSUME.put(playerId, age);
    }

    public static boolean shouldConsumeEnd(UUID playerId, int currentAge) {
        Integer markedAt = PENDING_END_CONSUME.get(playerId);
        if (markedAt == null) {
            return false;
        }
        if (currentAge - markedAt > 200) {
            PENDING_END_CONSUME.remove(playerId);
            return false;
        }
        return true;
    }

    public static void clearPendingEnd(UUID playerId) {
        PENDING_END_CONSUME.remove(playerId);
    }

    public static boolean markFailNotice(UUID playerId) {
        return FAIL_NOTICE.add(playerId);
    }

    public static void clearFailNotice(UUID playerId) {
        FAIL_NOTICE.remove(playerId);
    }
}

