package br.guardians.aurora;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** Transformações temporárias: pequeno, gigante e morcego. Uma por jogador. */
public class Morph {
    static class St {
        long end;
        String type;
        BatEntity bat;
    }

    static final Map<UUID, St> ACTIVE = new HashMap<>();
    static final Identifier SCALE_ID = AuroraMod.id("morph_scale");

    static void setScale(ServerPlayerEntity p, double scale) {
        EntityAttributeInstance a = p.getAttributeInstance(EntityAttributes.GENERIC_SCALE);
        if (a == null) return;
        a.removeModifier(SCALE_ID);
        if (scale != 1.0)
            a.addTemporaryModifier(new EntityAttributeModifier(SCALE_ID, scale - 1.0,
                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    static St begin(ServerPlayerEntity p, String type, int ticks) {
        St old = ACTIVE.get(p.getUuid());
        if (old != null) clear(p, old);
        St st = new St();
        st.type = type;
        st.end = p.getServer().getTicks() + (long) ticks;
        ACTIVE.put(p.getUuid(), st);
        return st;
    }

    public static void small(ServerPlayerEntity p, int ticks) {
        begin(p, "small", ticks);
        setScale(p, 0.35);
    }

    public static void big(ServerPlayerEntity p, int ticks) {
        begin(p, "big", ticks);
        setScale(p, 2.0);
    }

    /** Vira morcego: fica invisível, pequeno, voa, e um morcego de verdade segue você. */
    public static void bat(ServerPlayerEntity p, int ticks) {
        begin(p, "bat", ticks);
        setScale(p, 0.3);
        Stealth.start(p, ticks);
        p.getAbilities().allowFlying = true;
        p.getAbilities().flying = true;
        p.getAbilities().setFlySpeed(0.08f);
        p.sendAbilitiesUpdate();
    }

    static void clear(ServerPlayerEntity p, St st) {
        setScale(p, 1.0);
        if (st.bat != null) st.bat.discard();
        if (st.type.equals("bat")) {
            p.removeStatusEffect(StatusEffects.INVISIBILITY);
            Stealth.end(p);
            GameMode gm = p.interactionManager.getGameMode();
            if (!gm.isCreative() && gm != GameMode.SPECTATOR) {
                p.getAbilities().allowFlying = false;
                p.getAbilities().flying = false;
            }
            p.getAbilities().setFlySpeed(0.05f);
            p.sendAbilitiesUpdate();
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 100, 0));
        }
    }

    /** Encerra a transformação (morte, saída do servidor). */
    public static void end(ServerPlayerEntity p) {
        St st = ACTIVE.remove(p.getUuid());
        if (st != null) clear(p, st);
    }

    static void followBat(ServerPlayerEntity p, St st) {
        ServerWorld w = (ServerWorld) p.getWorld();
        BatEntity b = st.bat;
        if (b == null || b.isRemoved()) {
            b = EntityType.BAT.create(w);
            if (b == null) return;
            b.setAiDisabled(true);
            b.setInvulnerable(true);
            b.setNoGravity(true);
            b.setSilent(true);
            b.setRoosting(false);
            b.setPersistent();
            b.refreshPositionAndAngles(p.getX(), p.getY() + 0.1, p.getZ(), p.getYaw(), 0);
            w.spawnEntity(b);
            st.bat = b;
        } else {
            b.refreshPositionAndAngles(p.getX(), p.getY() + 0.1, p.getZ(), p.getYaw(), 0);
        }
        b.setHeadYaw(p.getHeadYaw());
    }

    public static void tick(MinecraftServer s) {
        if (ACTIVE.isEmpty()) return;
        long now = s.getTicks();
        Iterator<Map.Entry<UUID, St>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, St> en = it.next();
            St st = en.getValue();
            ServerPlayerEntity p = s.getPlayerManager().getPlayer(en.getKey());
            if (p == null || !p.isAlive() || now >= st.end) {
                if (p != null) clear(p, st);
                else if (st.bat != null) st.bat.discard();
                it.remove();
                continue;
            }
            if (st.type.equals("bat")) followBat(p, st);
        }
    }
}
