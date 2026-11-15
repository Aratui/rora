package br.guardians.aurora;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.util.Identifier;

public class AuroraMod implements ModInitializer {
    public static final String ID = "aurora";

    public static Identifier id(String path) {
        return Identifier.of(ID, path);
    }

    @Override
    public void onInitialize() {
        AuroraItems.register();
        AuroraCommands.register();
        ServerLifecycleEvents.SERVER_STARTED.register(AuroraData::load);
        ServerLifecycleEvents.SERVER_STOPPING.register(AuroraData::save);
        ServerLivingEntityEvents.AFTER_DEATH.register(AuroraEvents::onDeath);
        ServerTickEvents.END_SERVER_TICK.register(AuroraEvents::tick);
    }
}
