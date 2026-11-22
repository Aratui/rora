package br.guardians.aurora;

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
    }

    /** HUD simples na action bar, atualizada a cada segundo. */
    public static void tick(MinecraftServer server) {
        if (server.getTicks() % 20 != 0) return;
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            AuroraData.Info i = AuroraData.get(p.getUuid());
            if (i.god.isEmpty() && i.team.isEmpty() && i.faction.isEmpty() && i.level <= 1) continue;
            God g = God.byKey(i.god);
            String s = (g != null ? g.label + " " : "") + "Nv." + i.level
                    + (i.team.isEmpty() ? "" : " | Time: " + i.team.toUpperCase())
                    + (i.faction.isEmpty() ? "" : " | Facção: " + i.faction);
            p.sendMessage(Text.literal(s), true);
        }
    }
}
