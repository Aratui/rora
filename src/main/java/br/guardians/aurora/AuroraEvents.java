package br.guardians.aurora;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class AuroraEvents {
    /** Morrer = -1 nível (mín. 1). Matar jogador = +1 nível e 1 ponto para time/facção. */
    public static void onDeath(LivingEntity entity, DamageSource source) {
        if (!(entity instanceof ServerPlayerEntity victim)) return;
        Morph.end(victim);
        Forms.end(victim);
        Stealth.end(victim);
        AuroraData.Info vi = AuroraData.get(victim.getUuid());
        if (vi.level > 1) {
            vi.level--;
            victim.sendMessage(Text.literal("Você morreu e perdeu um nível: " + vi.level), false);
        }
        if (source.getAttacker() instanceof ServerPlayerEntity killer && killer != victim) {
            AuroraData.Info ki = AuroraData.get(killer.getUuid());
            if (!ki.team.isEmpty() && !ki.team.equals(vi.team)) AuroraData.addScore("time:" + ki.team, 1);
            if (!ki.faction.isEmpty() && !ki.faction.equals(vi.faction)) AuroraData.addScore("faccao:" + ki.faction, 1);
            if (ki.level < God.MAX_LEVEL) {
                ki.level++;
                levelUp(killer, ki);
            }
        }
    }

    static void levelUp(ServerPlayerEntity p, AuroraData.Info i) {
        List<String> novas = new ArrayList<>();
        God g = God.byKey(i.god);
        if (g != null)
            for (Skill s : g.skills) if (s.level() == i.level) novas.add(s.name() + " (" + g.label + ")");
        for (int k = 0; k < p.getInventory().size(); k++) {
            ItemStack st = p.getInventory().getStack(k);
            if (st.getItem() instanceof AuroraItems.PearlItem pi)
                for (Skill s : pi.pearl.skills) if (s.level() == i.level) novas.add(s.name() + " (" + pi.pearl.label + ")");
        }
        p.sendMessage(Text.literal("Nível " + i.level + "!"
                + (novas.isEmpty() ? "" : " Nova habilidade: " + String.join(", ", novas))), false);
        AuroraCast.syncSel(p);
    }

    /** A cada segundo, envia ao cliente os dados do painel de status (HUD). */
    public static void tick(MinecraftServer server) {
        Morph.tick(server);
        Forms.tick(server);
        Stealth.tick(server);
        if (server.getTicks() % 20 != 0) return;
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            AuroraData.Info i = AuroraData.get(p.getUuid());
            int tp = i.team.isEmpty() ? 0 : AuroraData.scores().getOrDefault("time:" + i.team, 0);
            int fp = i.faction.isEmpty() ? 0 : AuroraData.scores().getOrDefault("faccao:" + i.faction, 0);
            ServerPlayNetworking.send(p, new StatusPayload(i.god, i.level, i.team, i.faction, tp, fp));
        }
    }
}
