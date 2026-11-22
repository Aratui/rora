package br.guardians.aurora;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
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
        PayloadTypeRegistry.playC2S().register(CastPayload.ID, CastPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CdPayload.ID, CdPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SelPayload.ID, SelPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(StatusPayload.ID, StatusPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(CastPayload.ID, (payload, ctx) -> {
            ServerPlayerEntity p = ctx.player();
            switch (payload.kind()) {
                case 0 -> AuroraCast.essence(p, null);
                case 1 -> AuroraCast.pearlFromInventory(p);
                case 2 -> AuroraCast.cycleEssence(p);
                default -> AuroraCast.cyclePearl(p);
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> Morph.end(handler.player));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            Forms.cleanup(handler.player);
            Stealth.end(handler.player);
            AuroraCast.syncSel(handler.player);
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldP, newP, alive) -> Forms.cleanup(newP));
        ServerLifecycleEvents.SERVER_STARTED.register(AuroraData::load);
        ServerLifecycleEvents.SERVER_STOPPING.register(AuroraData::save);
        ServerLivingEntityEvents.AFTER_DEATH.register(AuroraEvents::onDeath);
        ServerTickEvents.END_SERVER_TICK.register(AuroraEvents::tick);
    }
}
