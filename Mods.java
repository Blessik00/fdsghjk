package dev.pulse.pvp;

import java.util.ArrayList;
import java.util.List;

/** Registry of modules, categories and their settings. */
public final class Mods {
    public static final String VISUALS = "Visuals";
    public static final String HUD = "HUD";
    public static final String ANIM = "Animations";

    public static final String SWING = "Swing Animation";
    public static final String VIEW = "View Model";
    public static final String HITMARK = "Hit Marker";
    public static final String LOWHP = "Low Health Warning";
    public static final String JUMP = "Jump Circle";
    public static final String KILL = "Kill Effect";
    public static final String SPEED = "Speed HUD";
    public static final String COOLDOWN = "Cooldown Bar";

    public static final String[] CATEGORIES = {VISUALS, HUD, ANIM};

    public static final int SLIDER = 0, CHOICE = 1, TOGGLE = 2;

    public static final class Setting {
        public final String key, label;
        public final int type;
        public final float min, max, def, step;
        public final String[] options;

        Setting(String key, String label, int type, float min, float max, float def, float step, String[] options) {
            this.key = key; this.label = label; this.type = type;
            this.min = min; this.max = max; this.def = def; this.step = step; this.options = options;
        }
    }

    public static final class Mod {
        public final String name, category, desc;
        public final boolean def;
        public final List<Setting> settings = new ArrayList<>();

        Mod(String name, String category, String desc, boolean def) {
            this.name = name; this.category = category; this.desc = desc; this.def = def;
        }
        Mod slider(String key, String label, float min, float max, float def, float step) {
            settings.add(new Setting(key, label, SLIDER, min, max, def, step, null)); return this;
        }
        Mod choice(String key, String label, int def, String... options) {
            settings.add(new Setting(key, label, CHOICE, 0, options.length - 1, def, 1, options)); return this;
        }
        Mod toggle(String key, String label, boolean def) {
            settings.add(new Setting(key, label, TOGGLE, 0, 1, def ? 1 : 0, 1, null)); return this;
        }
    }

    public static final List<Mod> ALL = new ArrayList<>();

    private static Mod add(String name, String cat, String desc, boolean def) {
        Mod m = new Mod(name, cat, desc, def);
        ALL.add(m);
        return m;
    }

    static {
        // ---- Visuals
        add(Config.NUMBERS, VISUALS, "Летящие цифры урона над целью", true);
        add(Config.PARTICLES, VISUALS, "Частицы при ударе и криты", true);
        add(Config.FLASH, VISUALS, "Красная вспышка экрана при получении урона", true);
        add(HITMARK, VISUALS, "Крестик в центре экрана при попадании", true)
                .slider("size", "Размер", 3, 12, 6, 1);
        add(LOWHP, VISUALS, "Пульсирующая красная рамка при низком здоровье", true)
                .slider("hearts", "Порог (сердца)", 2, 10, 6, 1);
        add(JUMP, VISUALS, "Кольцо частиц под ногами при прыжке", true)
                .slider("size", "Размер", 0.5f, 3f, 1.2f, 0.1f);
        add(KILL, VISUALS, "Взрыв частиц, когда цель умирает", true)
                .slider("amount", "Количество", 10, 80, 30, 5);

        // ---- HUD
        add(Config.TARGET, HUD, "Здоровье, броня и дистанция до цели", true);
        add(Config.KEYS, HUD, "WASD, мышь и CPS на экране", true);
        add(Config.ARMOR, HUD, "Прочность вашей брони", true);
        add(Config.POTIONS, HUD, "Активные эффекты и таймеры", true);
        add(Config.ITEMS, HUD, "Totem, перлы, золотые яблоки, зелья", true);
        add(Config.COMBO, HUD, "Комбо-счётчик и дистанция удара", true);
        add(Config.INFO, HUD, "FPS, пинг, координаты, направление", true);
        add(SPEED, HUD, "Скорость движения в блоках в секунду", false);
        add(COOLDOWN, HUD, "Полоска перезарядки удара под прицелом", true);

        // ---- Animations
        add(SWING, ANIM, "Свой стиль и скорость взмаха оружием", true)
                .choice("style", "Стиль", 1, "Vanilla", "Slash", "Stab", "Block", "Whirl")
                .slider("speed", "Скорость", 0.4f, 2.5f, 1.0f, 0.05f)
                .slider("strength", "Сила", 0f, 2f, 1.0f, 0.05f);
        add(VIEW, ANIM, "Положение, размер и поворот предмета в руке", true)
                .slider("x", "Смещение X", -1f, 1f, 0f, 0.01f)
                .slider("y", "Смещение Y", -1f, 1f, 0f, 0.01f)
                .slider("z", "Смещение Z", -1f, 1f, 0f, 0.01f)
                .slider("scale", "Размер", 0.4f, 1.6f, 1.0f, 0.01f)
                .slider("rx", "Поворот X", -90f, 90f, 0f, 1f)
                .slider("ry", "Поворот Y", -90f, 90f, 0f, 1f)
                .slider("rz", "Поворот Z", -90f, 90f, 0f, 1f)
                .toggle("noequip", "Без анимации смены", false);
    }

    public static Mod get(String name) {
        for (Mod m : ALL) if (m.name.equals(name)) return m;
        return null;
    }

    public static Setting setting(String mod, String key) {
        Mod m = get(mod);
        if (m == null) return null;
        for (Setting s : m.settings) if (s.key.equals(key)) return s;
        return null;
    }

    /** Current value of a module setting. */
    public static float v(String mod, String key) {
        String id = mod + "." + key;
        Float f = Config.VALUES.get(id);
        if (f != null) return f;
        Setting s = setting(mod, key);
        return s == null ? 0f : s.def;
    }
}
