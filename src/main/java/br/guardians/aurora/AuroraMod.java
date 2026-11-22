package br.guardians.aurora;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
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
        ServerPlayNetworking.registerGlobalReceiver(CastPayload.ID, (payload, ctx) -> {
            ServerPlayerEntity p = ctx.player();
            if (payload.kind() == 0) {
                int cd = AuroraCast.essence(p, null);
                if (cd > 0) for (Item it : AuroraItems.ESSENCES.values()) p.getItemCooldownManager().set(it, cd);
            } else {
                AuroraCast.pearlFromInventory(p);
            }
        });
        ServerLifecycleEvents.SERVER_STARTED.register(AuroraData::load);
        ServerLifecycleEvents.SERVER_STOPPING.register(AuroraData::save);
        ServerLivingEntityEvents.AFTER_DEATH.register(AuroraEvents::onDeath);
        ServerTickEvents.END_SERVER_TICK.register(AuroraEvents::tick);
    }
}
