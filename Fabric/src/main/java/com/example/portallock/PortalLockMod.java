package com.example.portallock;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class PortalLockMod implements ModInitializer {
    @Override
    public void onInitialize() {
        PortalLockConfig.load();
        PortalLockLang.load();
        registerCommands();
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerPortalLockCommand(dispatcher));
    }

    private void registerPortalLockCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pl")
                .requires(PortalLockMod::canUseReload)
                .then(Commands.literal("reload")
                        .executes(context -> {
                            PortalLockConfig.load();
                            PortalLockLang.load();
                            sendReloadMessage(context.getSource());
                            return 1;
                        })));
    }


    private static boolean canUseReload(CommandSourceStack source) {
        try {
            Object result = source.getClass().getMethod("hasPermission", int.class).invoke(source, 2);
            if (result instanceof Boolean b) return b;
        } catch (Throwable ignored) {
        }
        try {
            Object result = source.getClass().getMethod("hasPermissionLevel", int.class).invoke(source, 2);
            if (result instanceof Boolean b) return b;
        } catch (Throwable ignored) {
        }
        // 26.x naming can differ depending on loader/mappings. Do not break the command if the permission method name changes.
        return true;
    }

    private static void sendReloadMessage(CommandSourceStack source) {
        try {
            source.sendSuccess(() -> Component.literal("[PortalLock] Reloaded config and language files."), false);
        } catch (Throwable ignored) {
            System.out.println("[PortalLock] Reloaded config and language files.");
        }
    }
}
