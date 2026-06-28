package com.example.portallock;

public final class PortalLockText {
    private PortalLockText() {}
    public static void sendNetherDenied(Object player) { ReflectPortalLogic.sendNetherDenied(player); }
    public static void sendEndDenied(Object player) { ReflectPortalLogic.sendEndDenied(player); }
    public static int countItem(Object player, Object item) throws Exception { return ReflectPortalLogic.countItem(player, item); }
    public static int requiredHoldingAmount(int configuredAmount) { return ReflectPortalLogic.requiredHoldingAmount(configuredAmount); }
    public static void consumeItem(Object player, Object item, int amount) throws Exception { ReflectPortalLogic.consumeItem(player, item, amount); }
    public static Object getItemOrAir(String id) { return ReflectPortalLogic.getItemOrAir(id); }
    public static void playConfiguredSound(Object player, String soundId) { ReflectPortalLogic.playConfiguredSound(player, soundId); }
}
