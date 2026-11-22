package br.guardians.aurora;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Uso de poderes (item ou tecla): habilidade selecionada, recarga por habilidade. */
public class AuroraCast {
    static final Map<String, Long> CD = new HashMap<>();
    static final Map<String, Integer> SEL = new HashMap<>();

    // ---------- recarga ----------
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

    // ---------- seleção ----------
    static List<Skill> unlocked(Skill[] all, int lvl) {
        List<Skill> l = new ArrayList<>();
        for (Skill s : all) if (lvl >= s.level()) l.add(s);
        return l;
    }

    static int idx(String key, int n) {
        return Math.max(0, Math.min(SEL.getOrDefault(key, 0), n - 1));
    }

    static Pearl findPearl(ServerPlayerEntity p) {
        for (int k = 0; k < p.getInventory().size(); k++) {
            ItemStack st = p.getInventory().getStack(k);
            if (st.getItem() instanceof AuroraItems.PearlItem pi) return pi.pearl;
        }
        return null;
    }

    /** Envia ao cliente qual habilidade está selecionada (para o HUD). */
    public static void syncSel(ServerPlayerEntity p) {
        AuroraData.Info i = AuroraData.get(p.getUuid());
        String e = "";
        God g = God.byKey(i.god);
        if (g != null) {
            List<Skill> un = unlocked(g.skills, i.level);
            e = g.label + " · " + un.get(idx(p.getUuidAsString() + "e", un.size())).name();
        }
        String pr = "";
        Pearl pe = findPearl(p);
        if (pe != null) {
            List<Skill> un = unlocked(pe.skills, i.level);
            pr = pe.label + " · " + un.get(idx(p.getUuidAsString() + "p" + pe.key, un.size())).name();
        }
        ServerPlayNetworking.send(p, new SelPayload("e", e));
        ServerPlayNetworking.send(p, new SelPayload("p", pr));
    }

    public static void cycleEssence(ServerPlayerEntity p) {
        AuroraData.Info i = AuroraData.get(p.getUuid());
        God g = God.byKey(i.god);
        if (g == null) { p.sendMessage(Text.literal("Você ainda não tem um deus."), true); return; }
        List<Skill> un = unlocked(g.skills, i.level);
        String k = p.getUuidAsString() + "e";
        int n = (idx(k, un.size()) + 1) % un.size();
        SEL.put(k, n);
        p.sendMessage(Text.literal(un.get(n).name() + ": " + un.get(n).desc()), true);
        syncSel(p);
    }

    public static void cyclePearl(ServerPlayerEntity p) {
        Pearl pe = findPearl(p);
        if (pe == null) { p.sendMessage(Text.literal("Você não tem nenhuma pérola."), true); return; }
        int lvl = AuroraData.get(p.getUuid()).level;
        List<Skill> un = unlocked(pe.skills, lvl);
        String k = p.getUuidAsString() + "p" + pe.key;
        int n = (idx(k, un.size()) + 1) % un.size();
        SEL.put(k, n);
        p.sendMessage(Text.literal(un.get(n).name() + ": " + un.get(n).desc()), true);
        syncSel(p);
    }

    // ---------- uso ----------
    /** Usa a habilidade selecionada da essência. required != null: precisa ser desse deus. Retorna a recarga (0 = falhou). */
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
        List<Skill> un = unlocked(g.skills, i.level);
        Skill s = un.get(idx(p.getUuidAsString() + "e", un.size()));
        String cdKey = "e" + s.name();
        if (!ready(p, cdKey)) return 0;
        Skill.castOne(s, p, (ServerWorld) p.getWorld(), i.level, g.fx);
        int cd = Math.max(60, 80 + s.level() * 30 - (i.level - s.level()) * 5);
        start(p, cdKey, cd, s.name());
        syncSel(p);
        return cd;
    }

    public static int pearl(ServerPlayerEntity p, Pearl pr) {
        int lvl = AuroraData.get(p.getUuid()).level;
        List<Skill> un = unlocked(pr.skills, lvl);
        Skill s = un.get(idx(p.getUuidAsString() + "p" + pr.key, un.size()));
        String cdKey = "p" + pr.key + s.name();
        if (!ready(p, cdKey)) return 0;
        Skill.castOne(s, p, (ServerWorld) p.getWorld(), lvl, pr.fx);
        int cd = Math.max(60, (int) (pr.cooldown * (0.5 + 0.1 * s.level())) - (lvl - s.level()) * 10);
        start(p, cdKey, cd, s.name());
        syncSel(p);
        return cd;
    }

    /** Tecla da pérola: usa a primeira pérola do inventário (hotbar primeiro). */
    public static void pearlFromInventory(ServerPlayerEntity p) {
        Pearl pe = findPearl(p);
        if (pe == null) { p.sendMessage(Text.literal("Você não tem nenhuma pérola."), true); return; }
        pearl(p, pe);
    }
}
