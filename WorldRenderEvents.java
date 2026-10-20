package net.fabricmc.fabric.api.client.rendering.v1;
import net.fabricmc.fabric.api.event.Event;
public final class WorldRenderEvents{ public static final Event<AfterTranslucent> AFTER_TRANSLUCENT=null; public interface AfterTranslucent{ void afterTranslucent(WorldRenderContext c);}}
