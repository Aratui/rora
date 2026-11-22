package br.guardians.aurora;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Invisibilidade total: além do corpo, esconde armadura e itens nas mãos para quem está olhando.
 * Não mexe no inventário: só envia aos outros jogadores pacotes de equipamento "vazio" enquanto durar.
 */
public class Stealth {
    static final Map<UUID, Long> ACTIVE = new HashMap<>();

    public static void start(ServerPlayerEntity p, int ticks) {
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, ticks, 0, false, false, true));
        ACTIVE.put(p.getUuid(), p.getServer().getTicks() + (long) ticks);
        send(p, true);
    }

    static void send(ServerPlayerEntity p, boolean hide) {
        List<Pair<EquipmentSlot, ItemStack>> list = new ArrayList<>();
        for (EquipmentSlot s : EquipmentSlot.values())
            list.add(Pair.of(s, hide ? ItemStack.EMPTY : p.getEquippedStack(s).copy()));
        EntityEquipmentUpdateS2CPacket pk = new EntityEquipmentUpdateS2CPacket(p.getId(), list);
        for (ServerPlayerEntity t : PlayerLookup.tracking(p)) t.networkHandler.sendPacket(pk);
    }

    /** Termina o efeito e mostra o equipamento de novo. */
    public static void end(ServerPlayerEntity p) {
        if (ACTIVE.remove(p.getUuid()) != null) send(p, false);
    }

    public static void tick(MinecraftServer s) {
        if (ACTIVE.isEmpty()) return;
        long now = s.getTicks();
        for (ServerPlayerEntity p : s.getPlayerManager().getPlayerList()) {
            Long e = ACTIVE.get(p.getUuid());
            if (e == null) continue;
            if (now >= e || !p.isAlive() || !p.hasStatusEffect(StatusEffects.INVISIBILITY)) end(p);
            else send(p, true);
        }
    }
}
