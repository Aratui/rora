package br.guardians.aurora;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AuroraEvents {
    /** Morrer = -1 nível (mín. 1). Matar jogador = +1 nível e 1 ponto para time/facção. */
    public static void onDeath(LivingEntity entity, DamageSource source) {
        // XP por matar monstros, chefes e outros seres
        if (!(entity instanceof ServerPlayerEntity) && source.getAttacker() instanceof ServerPlayerEntity k) {
            addXp(k, entity instanceof EnderDragonEntity ? 100 : entity instanceof Monster ? 6 : 1);
            return;
        }
        if (!(entity instanceof ServerPlayerEntity victim)) return;
        Morph.end(victim);
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
                ki.xp = 0;
                levelUp(killer, ki);
            }
        }
    }

    // ---------- XP ----------
    /** XP necessário para sair do nível dado. */
    public static int need(int level) { return 30 + level * 20; }

    /** Ganha XP; sobe de nível sozinho quando enche a barra. */
    public static void addXp(ServerPlayerEntity p, int amount) {
        AuroraData.Info i = AuroraData.get(p.getUuid());
        if (i.level >= God.MAX_LEVEL) return;
        i.xp += amount;
        while (i.level < God.MAX_LEVEL && i.xp >= need(i.level)) {
            i.xp -= need(i.level);
            i.level++;
            levelUp(p, i);
        }
        if (i.level >= God.MAX_LEVEL) i.xp = 0;
    }

    // ---------- sem dano do próprio poder ----------
    static final Map<UUID, Long> SELF = new HashMap<>();

    /** Marca que o jogador acabou de lançar um poder: por alguns ticks, raio/fogo/explosão/magia sem dono não o ferem. */
    public static void markCast(ServerPlayerEntity p, int ticks) {
        SELF.put(p.getUuid(), p.getServer().getTicks() + (long) ticks);
    }

    public static boolean allowDamage(LivingEntity e, DamageSource src, float amount) {
        if (!(e instanceof ServerPlayerEntity p)) return true;
        Long end = SELF.get(p.getUuid());
        if (end == null || p.getServer().getTicks() > end) return true;
        if (src.getAttacker() != null && src.getAttacker() != p) return true;   // dano de outro ser continua valendo
        boolean own = src.isOf(DamageTypes.LIGHTNING_BOLT) || src.isIn(DamageTypeTags.IS_FIRE)
                || src.isIn(DamageTypeTags.IS_EXPLOSION) || src.isOf(DamageTypes.MAGIC)
                || src.isOf(DamageTypes.INDIRECT_MAGIC) || src.isOf(DamageTypes.WITHER);
        if (!own) return true;
        if (p.isOnFire()) p.extinguish();
        return false;
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
        Stealth.tick(server);
        if (server.getTicks() % 20 != 0) return;
        boolean minute = server.getTicks() % 1200 == 0;
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            if (minute) addXp(p, 5);   // meditação: XP passivo a cada minuto
            AuroraData.Info i = AuroraData.get(p.getUuid());
            int tp = i.team.isEmpty() ? 0 : AuroraData.scores().getOrDefault("time:" + i.team, 0);
            int fp = i.faction.isEmpty() ? 0 : AuroraData.scores().getOrDefault("faccao:" + i.faction, 0);
            ServerPlayNetworking.send(p, new StatusPayload(i.god, i.level, i.team, i.faction, tp, fp));
            ServerPlayNetworking.send(p, new XpPayload(i.xp, i.level >= God.MAX_LEVEL ? 0 : need(i.level)));
        }
    }
}
