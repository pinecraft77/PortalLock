package com.example.portallock;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PortalLockNetherState {
    private static final Map<UUID, Integer> PENDING_NETHER_CONSUME = new ConcurrentHashMap<>();
    private static final Set<UUID> FAIL_NOTICE = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> ARRIVAL_PROTECTION = ConcurrentHashMap.newKeySet();

    private PortalLockNetherState() {}

    public static void markPendingNether(UUID playerId, int age) {
        PENDING_NETHER_CONSUME.put(playerId, age);
    }

    public static boolean shouldConsumeNether(UUID playerId, int currentAge) {
        Integer markedAt = PENDING_NETHER_CONSUME.get(playerId);
        if (markedAt == null) return false;
        if (currentAge - markedAt > 200) {
            PENDING_NETHER_CONSUME.remove(playerId);
            return false;
        }
        return true;
    }

    public static void clearPendingNether(UUID playerId) {
        PENDING_NETHER_CONSUME.remove(playerId);
    }

    public static boolean markFailNotice(UUID playerId) {
        return FAIL_NOTICE.add(playerId);
    }

    public static void clearFailNotice(UUID playerId) {
        FAIL_NOTICE.remove(playerId);
    }

    public static void markArrivalProtection(UUID playerId) {
        ARRIVAL_PROTECTION.add(playerId);
    }

    public static boolean isArrivalProtected(UUID playerId) {
        return ARRIVAL_PROTECTION.contains(playerId);
    }

    public static void clearArrivalProtection(UUID playerId) {
        ARRIVAL_PROTECTION.remove(playerId);
    }
}
