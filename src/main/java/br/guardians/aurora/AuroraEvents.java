package br.guardians.aurora;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class AuroraEvents {
    /** Morrer = perde 1 nível (mínimo 1). Matar jogador de outro time/facção = +1 ponto. */
    public static void onDeath(LivingEntity entity, DamageSource source) {
        if (!(entity instanceof ServerPlayerEntity victim)) return;
        AuroraData.Info vi = AuroraData.get(victim.getUuid());
        if (!vi.god.isEmpty() && vi.level > 1) {
            vi.level--;
            victim.sendMessage(Text.literal("Você morreu e perdeu um nível: " + vi.level), false);
        }
        if (source.getAttacker() instanceof ServerPlayerEntity killer && killer != victim) {
            AuroraData.Info ki = AuroraData.get(killer.getUuid());
            if (!ki.team.isEmpty() && !ki.team.equals(vi.team)) AuroraData.addScore("time:" + ki.team, 1);
            if (!ki.faction.isEmpty() && !ki.faction.equals(vi.faction)) AuroraData.addScore("faccao:" + ki.faction, 1);
        }
    }

    /** HUD simples na action bar, atualizada a cada segundo. */
    public static void tick(MinecraftServer server) {
        if (server.getTicks() % 20 != 0) return;
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            AuroraData.Info i = AuroraData.get(p.getUuid());
            if (i.god.isEmpty() && i.team.isEmpty() && i.faction.isEmpty()) continue;
            God g = God.byKey(i.god);
            String s = (g != null ? g.label + " Nv." + i.level + " | " : "")
                    + (i.team.isEmpty() ? "" : "Time: " + i.team.toUpperCase() + " | ")
                    + (i.faction.isEmpty() ? "" : "Facção: " + i.faction);
            p.sendMessage(Text.literal(s), true);
        }
    }
}
