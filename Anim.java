package dev.pulse.pvp;

import net.minecraft.class_1268;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_742;
import org.joml.Quaternionf;

/** First-person item transforms: View Model + Swing Animation. Called from mixins. */
public final class Anim {
    private static boolean pushed;
    private static boolean broken;

    private Anim() {}

    private static float side(class_742 player, class_1268 hand) {
        boolean main = hand.ordinal() == 0;
        try {
            boolean mainIsRight = player.method_6068().ordinal() == 1;
            return (main == mainIsRight) ? 1f : -1f;
        } catch (Throwable t) {
            return main ? 1f : -1f;
        }
    }

    private static void rot(class_4587 m, float dx, float dy, float dz) {
        if (dx == 0 && dy == 0 && dz == 0) return;
        m.method_22907(new Quaternionf().rotationXYZ((float) Math.toRadians(dx), (float) Math.toRadians(dy), (float) Math.toRadians(dz)));
    }

    public static void begin(class_742 player, class_1268 hand, float swing, class_4587 m) {
        pushed = false;
        if (broken) return;
        try {
            boolean vm = Config.on(Mods.VIEW);
            boolean sw = Config.on(Mods.SWING) && swing > 0f;
            if (!vm && !sw) return;
            m.method_22903();
            pushed = true;
            float side = side(player, hand);
            // vanilla hand position: everything below rotates/scales around the item itself
            float px = side * 0.56f, py = -0.52f, pz = -0.72f;

            if (vm) {
                m.method_46416(side * Mods.v(Mods.VIEW, "x"), Mods.v(Mods.VIEW, "y"), Mods.v(Mods.VIEW, "z"));
            }
            m.method_46416(px, py, pz);
            if (vm) {
                float s = Mods.v(Mods.VIEW, "scale");
                if (s != 1f) m.method_22905(s, s, s);
                rot(m, Mods.v(Mods.VIEW, "rx"), side * Mods.v(Mods.VIEW, "ry"), side * Mods.v(Mods.VIEW, "rz"));
            }
            if (sw) {
                int style = Math.round(Mods.v(Mods.SWING, "style"));
                float str = Mods.v(Mods.SWING, "strength");
                float k = (float) Math.sin(swing * Math.PI) * str;
                switch (style) {
                    case 1: // Slash
                        m.method_46416(-0.08f * side * k, 0f, 0f);
                        rot(m, 25f * k, 0f, -55f * side * k);
                        break;
                    case 2: // Stab
                        m.method_46416(0f, 0.04f * k, -0.45f * k);
                        rot(m, -10f * k, 0f, 0f);
                        break;
                    case 3: // Block (1.8 style)
                        m.method_46416(0f, -0.12f * k, 0f);
                        rot(m, -30f * k, -30f * side * k, 0f);
                        break;
                    case 4: // Whirl
                        rot(m, 0f, 0f, side * 360f * swing * str);
                        break;
                    default:
                        break;
                }
            }
            m.method_46416(-px, -py, -pz);
        } catch (Throwable t) {
            broken = true;
            end(m);
        }
    }

    public static void end(class_4587 m) {
        if (!pushed) return;
        pushed = false;
        try {
            m.method_22909();
        } catch (Throwable ignored) {
        }
    }

    /** Value for the equip-progress argument (0 = fully raised). */
    public static float equip(float original) {
        try {
            if (Config.on(Mods.VIEW) && Mods.v(Mods.VIEW, "noequip") > 0.5f) return 0f;
        } catch (Throwable ignored) {
        }
        return original;
    }

    /** New swing duration in ticks, only for the local player. */
    public static int swingDuration(Object entity, int original) {
        try {
            class_310 mc = class_310.method_1551();
            if (mc == null || entity != mc.field_1724) return original;
            if (!Config.on(Mods.SWING)) return original;
            float speed = Mods.v(Mods.SWING, "speed");
            if (speed <= 0f) return original;
            return Math.max(1, Math.round(original / speed));
        } catch (Throwable t) {
            return original;
        }
    }
}
