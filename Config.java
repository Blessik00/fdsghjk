package dev.pulse.pvp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class Config {
    public static final String TARGET = "Target HUD";
    public static final String NUMBERS = "Damage Numbers";
    public static final String PARTICLES = "Hit Particles";
    public static final String KEYS = "Keystrokes + CPS";
    public static final String ARMOR = "Armor HUD";
    public static final String POTIONS = "Potion HUD";
    public static final String ITEMS = "PvP Item Counter";
    public static final String COMBO = "Combo + Reach";
    public static final String INFO = "Info HUD";
    public static final String FLASH = "Damage Flash";

    /** Kept for compatibility with older code. The menu uses {@link Mods}. */
    public static final String[][] DEFS = {
            {TARGET, "Здоровье, броня и дистанция до цели"},
            {NUMBERS, "Летящие цифры урона над целью"},
            {PARTICLES, "Частицы при ударе и криты"},
            {KEYS, "WASD, мышь и CPS на экране"},
            {ARMOR, "Прочность вашей брони"},
            {POTIONS, "Активные эффекты и таймеры"},
            {ITEMS, "Totem, перлы, золотые яблоки, зелья"},
            {COMBO, "Комбо-счётчик и дистанция удара"},
            {INFO, "FPS, пинг, координаты, направление"},
            {FLASH, "Красная вспышка экрана при получении урона"}
    };

    public static final int[] ACCENTS = {-8626945, -16722689, -45715, -12723068, -20448};

    public static final Map<String, Boolean> MODULES = new LinkedHashMap<>();
    public static final Map<String, Float> VALUES = new LinkedHashMap<>();
    public static final Set<String> FAVORITES = new LinkedHashSet<>();

    public static int accent = ACCENTS[0];
    public static float scale = 1.0f;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("pulsepvp.json");

    static {
        for (Mods.Mod m : Mods.ALL) MODULES.put(m.name, m.def);
    }

    private Config() {}

    public static boolean on(String name) {
        return MODULES.getOrDefault(name, false);
    }

    public static void load() {
        try {
            if (!Files.exists(FILE)) return;
            JsonObject o = GSON.fromJson(Files.readString(FILE), JsonObject.class);
            if (o == null) return;
            if (o.has("modules")) {
                JsonObject m = o.getAsJsonObject("modules");
                for (String k : new java.util.ArrayList<>(MODULES.keySet()))
                    if (m.has(k)) MODULES.put(k, m.get(k).getAsBoolean());
            }
            if (o.has("values")) {
                JsonObject v = o.getAsJsonObject("values");
                for (Map.Entry<String, JsonElement> e : v.entrySet()) VALUES.put(e.getKey(), e.getValue().getAsFloat());
            }
            if (o.has("favorites")) {
                for (JsonElement e : o.getAsJsonArray("favorites")) FAVORITES.add(e.getAsString());
            }
            if (o.has("accent")) accent = o.get("accent").getAsInt();
            if (o.has("scale")) scale = Math.max(0.7f, Math.min(1.5f, o.get("scale").getAsFloat()));
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        try {
            JsonObject o = new JsonObject();
            JsonObject m = new JsonObject();
            for (Map.Entry<String, Boolean> e : MODULES.entrySet()) m.addProperty(e.getKey(), e.getValue());
            o.add("modules", m);
            JsonObject v = new JsonObject();
            for (Map.Entry<String, Float> e : VALUES.entrySet()) v.addProperty(e.getKey(), e.getValue());
            o.add("values", v);
            JsonArray f = new JsonArray();
            for (String s : FAVORITES) f.add(s);
            o.add("favorites", f);
            o.addProperty("accent", accent);
            o.addProperty("scale", scale);
            Files.writeString(FILE, GSON.toJson(o));
        } catch (Exception ignored) {
        }
    }
}
