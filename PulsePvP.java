package dev.pulse.pvp;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;

public class PulsePvP implements ClientModInitializer {
    public static class_304 openKey;

    @Override
    public void onInitializeClient() {
        Config.load();
        openKey = KeyBindingHelper.registerKeyBinding(
                new class_304("key.pulsepvp.gui", class_3675.class_307.field_1668, 344, "category.pulsepvp"));
        Tracker.init();
        HudRenderCallback.EVENT.register((ctx, tick) -> {
            Hud.render(ctx, tick);
            Extras.render(ctx);
        });
        WorldRenderEvents.AFTER_TRANSLUCENT.register(DamageNumbers::render);
        ClientTickEvents.END_CLIENT_TICK.register(PulsePvP::tick);
    }

    private static void tick(class_310 mc) {
        Extras.tick(mc);
        while (openKey.method_1436()) {
            mc.method_1507(new ClickGuiScreen());
        }
    }
}
