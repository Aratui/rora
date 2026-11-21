package br.guardians.aurora;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

/** Lógica única de uso de poderes (item ou tecla), com recarga compartilhada. */
public class AuroraCast {
    static final Map<String, Long> CD = new HashMap<>();

    static boolean ready(ServerPlayerEntity p, String key) {
        long now = p.getServer().getTicks();
        Long end = CD.get(p.getUuidAsString() + key);
        if (end != null && end > now) {
            p.sendMessage(Text.literal("Recarga: " + ((end - now + 19) / 20) + "s"), true);
            return false;
        }
        return true;
    }

    static void start(ServerPlayerEntity p, String key, int ticks, String label) {
        CD.put(p.getUuidAsString() + key, p.getServer().getTicks() + (long) ticks);
        ServerPlayNetworking.send(p, new CdPayload(key, ticks, label));
    }

    /** Usa o poder do deus do jogador. required != null: a essência usada precisa ser desse deus. Retorna a recarga em ticks (0 = falhou). */
    public static int essence(ServerPlayerEntity p, God required) {
        AuroraData.Info i = AuroraData.get(p.getUuid());
        God g = God.byKey(i.god);
        if (g == null) {
            p.sendMessage(Text.literal("Você ainda não tem um deus."), true);
            return 0;
        }
        if (required != null && required != g) {
            p.sendMessage(Text.literal("Esta essência é de " + required.label + ". Peça a um admin: /aurora god set"), true);
            return 0;
        }
        if (!ready(p, "e")) return 0;
        g.cast(p, (ServerWorld) p.getWorld(), i.level);
        int cd = Math.max(60, 300 - i.level * 20);
        start(p, "e", cd, "Essência de " + g.label);
        return cd;
    }

    public static int pearl(ServerPlayerEntity p, Pearl pearl) {
        if (!ready(p, "p" + pearl.key)) return 0;
        int lvl = AuroraData.get(p.getUuid()).level;
        pearl.cast(p, (ServerWorld) p.getWorld(), lvl);
        int cd = Math.max(40, pearl.cooldown - lvl * 15);
        start(p, "p" + pearl.key, cd, pearl.label);
        return cd;
    }

    /** Tecla da pérola: usa a primeira pérola encontrada no inventário (hotbar primeiro). */
    public static void pearlFromInventory(ServerPlayerEntity p) {
        for (int k = 0; k < p.getInventory().size(); k++) {
            ItemStack st = p.getInventory().getStack(k);
            if (st.getItem() instanceof AuroraItems.PearlItem pi) {
                int cd = pearl(p, pi.pearl);
                if (cd > 0) p.getItemCooldownManager().set(pi, cd);
                return;
            }
        }
        p.sendMessage(Text.literal("Você não tem nenhuma pérola."), true);
    }
}
