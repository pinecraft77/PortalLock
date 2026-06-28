package com.example.portallock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class PortalLockConfig {

    private static final File DIR = new File("config/portal-lock");
    private static final File FILE = new File(DIR, "config.yml");
    private static final File LEGACY_YAML_FILE = new File(DIR, "portal-lock.yml");
    private static final File LEGACY_JSON_IN_DIR = new File(DIR, "portal-lock.json");
    private static final File LEGACY_JSON_ROOT = new File("config/portal-lock.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String LEGACY_DEFAULT_NETHER = "You need Glowstone to enter the Nether portal";
    private static final String LEGACY_DEFAULT_END = "You need an End Crystal to enter the End portal";

    public static Data DATA = new Data();

    public static class Data {
        public boolean nether_enabled = true;
        public String nether_item = "minecraft:glowstone";
        public int nether_amount = 1;
        public String nether_message = "";
        public boolean nether_overlay = true;
        public String nether_success_sound = "minecraft:entity.enderman.teleport";
        public String nether_fail_sound = "minecraft:block.anvil.place";

        public boolean end_enabled = true;
        public String end_item = "minecraft:end_crystal";
        public int end_amount = 1;
        public String end_message = "";
        public boolean end_overlay = true;
        public String end_success_sound = "minecraft:entity.enderman.teleport";
        public String end_fail_sound = "minecraft:block.anvil.place";

        public float volume = 1.0f;
        public float pitch = 1.2f;

        public String language_mode = "auto";
        public String fixed_language = "en_us";
        public String fallback_language = "en_us";
    }

    public static void load() {
        migrateLegacyIfNeeded();
        backupLegacyJsonFiles();

        try {
            if (!FILE.exists()) {
                DATA = new Data();
                save();
                return;
            }

            DATA = readYaml(FILE);
            if (DATA == null) {
                DATA = new Data();
                save();
                return;
            }

            if (applyDefaultsAndNormalize()) {
                save();
            }
        } catch (Exception e) {
            e.printStackTrace();
            DATA = new Data();
            save();
        }
    }

    private static boolean applyDefaultsAndNormalize() {
        boolean changed = false;

        if (DATA.nether_item == null || DATA.nether_item.isBlank()) {
            DATA.nether_item = "minecraft:glowstone";
            changed = true;
        }
        if (DATA.nether_amount < 0) {
            DATA.nether_amount = 0;
            changed = true;
        }
        if (DATA.end_item == null || DATA.end_item.isBlank()) {
            DATA.end_item = "minecraft:end_crystal";
            changed = true;
        }
        if (DATA.end_amount < 0) {
            DATA.end_amount = 0;
            changed = true;
        }
        if (DATA.nether_success_sound == null || DATA.nether_success_sound.isBlank()) {
            DATA.nether_success_sound = "minecraft:entity.enderman.teleport";
            changed = true;
        }
        if (DATA.end_success_sound == null || DATA.end_success_sound.isBlank()) {
            DATA.end_success_sound = "minecraft:entity.enderman.teleport";
            changed = true;
        }
        if (DATA.nether_fail_sound == null || DATA.nether_fail_sound.isBlank()) {
            DATA.nether_fail_sound = "minecraft:block.anvil.place";
            changed = true;
        }
        if (DATA.end_fail_sound == null || DATA.end_fail_sound.isBlank()) {
            DATA.end_fail_sound = "minecraft:block.anvil.place";
            changed = true;
        }
        if (DATA.language_mode == null || DATA.language_mode.isBlank()) {
            DATA.language_mode = "auto";
            changed = true;
        }
        if (DATA.fixed_language == null || DATA.fixed_language.isBlank()) {
            DATA.fixed_language = "en_us";
            changed = true;
        }
        if (DATA.fallback_language == null || DATA.fallback_language.isBlank()) {
            DATA.fallback_language = "en_us";
            changed = true;
        }
        if (DATA.nether_message == null) {
            DATA.nether_message = "";
            changed = true;
        }
        if (DATA.end_message == null) {
            DATA.end_message = "";
            changed = true;
        }

        if (LEGACY_DEFAULT_NETHER.equals(DATA.nether_message)) {
            DATA.nether_message = "";
            changed = true;
        }
        if (LEGACY_DEFAULT_END.equals(DATA.end_message)) {
            DATA.end_message = "";
            changed = true;
        }

        String normalizedMode = DATA.language_mode.trim().toLowerCase(Locale.ROOT);
        if (!normalizedMode.equals(DATA.language_mode)) {
            DATA.language_mode = normalizedMode;
            changed = true;
        }

        String normalizedFixed = normalizeLocale(DATA.fixed_language);
        if (!normalizedFixed.equals(DATA.fixed_language)) {
            DATA.fixed_language = normalizedFixed;
            changed = true;
        }

        String normalizedFallback = normalizeLocale(DATA.fallback_language);
        if (!normalizedFallback.equals(DATA.fallback_language)) {
            DATA.fallback_language = normalizedFallback;
            changed = true;
        }

        return changed;
    }

    private static void migrateLegacyIfNeeded() {
        if (FILE.exists()) {
            return;
        }

        try {
            DIR.mkdirs();

            if (LEGACY_YAML_FILE.exists()) {
                DATA = readYaml(LEGACY_YAML_FILE);
                if (DATA == null) {
                    DATA = new Data();
                }
                applyDefaultsAndNormalize();
                save();
                System.out.println("[PortalLock] Migrated config from config/portal-lock/portal-lock.yml to config/portal-lock/config.yml");
                return;
            }

            if (LEGACY_JSON_IN_DIR.exists()) {
                DATA = readLegacyJson(LEGACY_JSON_IN_DIR);
                applyDefaultsAndNormalize();
                save();
                System.out.println("[PortalLock] Migrated config from config/portal-lock/portal-lock.json to config/portal-lock/config.yml");
                return;
            }

            if (LEGACY_JSON_ROOT.exists()) {
                DATA = readLegacyJson(LEGACY_JSON_ROOT);
                applyDefaultsAndNormalize();
                save();
                System.out.println("[PortalLock] Migrated config from config/portal-lock.json to config/portal-lock/config.yml");
            }
        } catch (Exception e) {
            System.out.println("[PortalLock] Failed to migrate legacy config; using defaults.");
            DATA = new Data();
            save();
        }
    }

    private static void backupLegacyJsonFiles() {
        backupLegacyJsonFile(LEGACY_JSON_IN_DIR, "config/portal-lock/portal-lock.json");
        backupLegacyJsonFile(LEGACY_JSON_ROOT, "config/portal-lock.json");
    }

    private static void backupLegacyJsonFile(File legacyFile, String label) {
        if (!legacyFile.exists()) {
            return;
        }
        File backup = new File(legacyFile.getParentFile(), legacyFile.getName() + ".bak");
        if (backup.exists()) {
            if (legacyFile.delete()) {
                System.out.println("[PortalLock] Removed legacy config after migration because backup already exists: " + label);
            }
            return;
        }
        try {
            Files.move(legacyFile.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[PortalLock] Backed up legacy JSON config to " + backup.getPath().replace('\\', '/'));
        } catch (IOException e) {
            System.out.println("[PortalLock] Failed to back up legacy config: " + label);
        }
    }

    private static Data readLegacyJson(File file) {
        try (FileReader reader = new FileReader(file)) {
            Data migrated = GSON.fromJson(reader, Data.class);
            return migrated != null ? migrated : new Data();
        } catch (Exception e) {
            return new Data();
        }
    }

    private static Data readYaml(File file) throws IOException {
        Data data = new Data();
        Map<String, String> values = new LinkedHashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                int colon = line.indexOf(':');
                if (colon < 0) {
                    continue;
                }

                String key = line.substring(0, colon).trim();
                if (key.isEmpty()) {
                    continue;
                }

                String rawValue = line.substring(colon + 1).trim();
                values.put(key, parseYamlScalar(rawValue));
            }
        }

        data.nether_enabled = parseBoolean(values.get("nether_enabled"), data.nether_enabled);
        data.nether_item = values.getOrDefault("nether_item", data.nether_item);
        data.nether_amount = parseInt(values.get("nether_amount"), data.nether_amount);
        data.nether_message = values.getOrDefault("nether_message", data.nether_message);
        data.nether_overlay = parseBoolean(values.get("nether_overlay"), data.nether_overlay);
        data.nether_success_sound = values.getOrDefault("nether_success_sound", data.nether_success_sound);
        data.nether_fail_sound = values.getOrDefault("nether_fail_sound", data.nether_fail_sound);

        data.end_enabled = parseBoolean(values.get("end_enabled"), data.end_enabled);
        data.end_item = values.getOrDefault("end_item", data.end_item);
        data.end_amount = parseInt(values.get("end_amount"), data.end_amount);
        data.end_message = values.getOrDefault("end_message", data.end_message);
        data.end_overlay = parseBoolean(values.get("end_overlay"), data.end_overlay);
        data.end_success_sound = values.getOrDefault("end_success_sound", data.end_success_sound);
        data.end_fail_sound = values.getOrDefault("end_fail_sound", data.end_fail_sound);

        data.volume = parseFloat(values.get("volume"), data.volume);
        data.pitch = parseFloat(values.get("pitch"), data.pitch);

        data.language_mode = values.getOrDefault("language_mode", data.language_mode);
        data.fixed_language = values.getOrDefault("fixed_language", data.fixed_language);
        data.fallback_language = values.getOrDefault("fallback_language", data.fallback_language);

        return data;
    }

    private static String parseYamlScalar(String rawValue) {
        if (rawValue == null) {
            return "";
        }
        if ((rawValue.startsWith("\"") && rawValue.endsWith("\"")) || (rawValue.startsWith("'") && rawValue.endsWith("'"))) {
            String inner = rawValue.substring(1, rawValue.length() - 1);
            return unescapeYaml(inner);
        }
        return rawValue;
    }

    private static String unescapeYaml(String value) {
        return value
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private static boolean parseBoolean(String value, boolean fallback) {
        if (value == null) {
            return fallback;
        }
        if (value.equalsIgnoreCase("true")) {
            return true;
        }
        if (value.equalsIgnoreCase("false")) {
            return false;
        }
        return fallback;
    }

    private static int parseInt(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return fallback;
        }
    }

    private static float parseFloat(String value, float fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Float.parseFloat(value);
        } catch (Exception e) {
            return fallback;
        }
    }

    private static String normalizeLocale(String locale) {
        if (locale == null || locale.isBlank()) {
            return "en_us";
        }
        return locale.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }

    public static void save() {
        try {
            DIR.mkdirs();
            try (FileWriter writer = new FileWriter(FILE, StandardCharsets.UTF_8)) {
                writer.write(buildYaml());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String buildYaml() {
        StringBuilder sb = new StringBuilder();
        sb.append("# ==============================\n");
        sb.append("# Portal Lock Configuration\n");
        sb.append("# ==============================\n\n");

        sb.append("# Enable or disable the Nether portal lock.\n");
        sb.append(writeBoolean("nether_enabled", DATA.nether_enabled));
        sb.append("\n");

        sb.append("# Required item ID to enter the Nether.\n");
        sb.append(writeString("nether_item", DATA.nether_item));
        sb.append("\n");

        sb.append("# Required amount of the Nether item.\n");
        sb.append("# Set to 0 to require the item without consuming it.\n");
        sb.append(writeInt("nether_amount", DATA.nether_amount));
        sb.append("\n");

        sb.append("# Optional custom Nether message.\n");
        sb.append("# Leave blank to use language files automatically.\n");
        sb.append("#\n");
        sb.append("# By default, edit messages in config/portal-lock/lang/<language>.json\n");
        sb.append("# If set here, this message overrides all language files.\n");
        sb.append("#\n");
        sb.append("# Supports color codes:\n");
        sb.append("# &0-&9, &a-&f, &r\n");
        sb.append("#\n");
        sb.append("# Supports placeholders:\n");
        sb.append("# %item%\n");
        sb.append("# %item_id%\n");
        sb.append("#\n");
        sb.append("# For unsupported client locales outside the bundled 10 languages,\n");
        sb.append("# you can also override the message here.\n");
        sb.append(writeString("nether_message", DATA.nether_message));
        sb.append("\n");

        sb.append("# Show the denied Nether message in the action bar overlay.\n");
        sb.append("# If false, the message will be shown in the chat.\n");
        sb.append(writeBoolean("nether_overlay", DATA.nether_overlay));
        sb.append("\n");

        sb.append("# Sound played when Nether entry succeeds.\n");
        sb.append(writeString("nether_success_sound", DATA.nether_success_sound));
        sb.append("\n");

        sb.append("# Sound played when Nether entry is denied.\n");
        sb.append(writeString("nether_fail_sound", DATA.nether_fail_sound));
        sb.append("\n");

        sb.append("# Enable or disable the End portal lock.\n");
        sb.append(writeBoolean("end_enabled", DATA.end_enabled));
        sb.append("\n");

        sb.append("# Required item ID to enter the End.\n");
        sb.append(writeString("end_item", DATA.end_item));
        sb.append("\n");

        sb.append("# Required amount of the End item.\n");
        sb.append("# Set to 0 to require the item without consuming it.\n");
        sb.append(writeInt("end_amount", DATA.end_amount));
        sb.append("\n");

        sb.append("# Optional custom End message.\n");
        sb.append("# Leave blank to use language files automatically.\n");
        sb.append("#\n");
        sb.append("# By default, edit messages in config/portal-lock/lang/<language>.json\n");
        sb.append("# If set here, this message overrides all language files.\n");
        sb.append("#\n");
        sb.append("# Supports color codes:\n");
        sb.append("# &0-&9, &a-&f, &r\n");
        sb.append("#\n");
        sb.append("# Supports placeholders:\n");
        sb.append("# %item%\n");
        sb.append("# %item_id%\n");
        sb.append("#\n");
        sb.append("# For unsupported client locales outside the bundled 10 languages,\n");
        sb.append("# you can also override the message here.\n");
        sb.append(writeString("end_message", DATA.end_message));
        sb.append("\n");

        sb.append("# Show the denied End message in the action bar overlay.\n");
        sb.append("# If false, the message will be shown in the chat.\n");
        sb.append(writeBoolean("end_overlay", DATA.end_overlay));
        sb.append("\n");

        sb.append("# Sound played when End entry succeeds.\n");
        sb.append(writeString("end_success_sound", DATA.end_success_sound));
        sb.append("\n");

        sb.append("# Sound played when End entry is denied.\n");
        sb.append(writeString("end_fail_sound", DATA.end_fail_sound));
        sb.append("\n");

        sb.append("# Volume for success and fail sounds.\n");
        sb.append(writeFloat("volume", DATA.volume));
        sb.append("\n");

        sb.append("# Pitch for success and fail sounds.\n");
        sb.append(writeFloat("pitch", DATA.pitch));
        sb.append("\n");

        sb.append("# Language mode: auto or fixed.\n");
        sb.append(writeString("language_mode", DATA.language_mode));
        sb.append("\n");

        sb.append("# Used only when language_mode is fixed. Example: en_us, ja_jp\n");
        sb.append(writeString("fixed_language", DATA.fixed_language));
        sb.append("\n");

        sb.append("# Fallback language when the client locale is unsupported.\n");
        sb.append(writeString("fallback_language", DATA.fallback_language));

        return sb.toString();
    }

    private static String writeString(String key, String value) {
        return key + ": \"" + escapeYaml(value == null ? "" : value) + "\"\n";
    }

    private static String writeBoolean(String key, boolean value) {
        return key + ": " + value + "\n";
    }

    private static String writeInt(String key, int value) {
        return key + ": " + value + "\n";
    }

    private static String writeFloat(String key, float value) {
        if (value == (long) value) {
            return key + ": " + (long) value + ".0\n";
        }
        return key + ": " + value + "\n";
    }

    private static String escapeYaml(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
