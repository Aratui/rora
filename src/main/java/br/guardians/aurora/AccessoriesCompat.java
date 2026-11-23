package br.guardians.aurora;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.lang.reflect.Method;
import java.util.function.Predicate;

/**
 * Integração OPCIONAL com o mod Accessories (o "Curios" do Fabric 1.21.1, que também lê slots de Curios).
 * Feita por reflexão: sem o Accessories instalado, nada acontece; com ele, as pérolas guardadas
 * no slot "charm" continuam funcionando pelas teclas.
 */
public class AccessoriesCompat {
    static boolean tried, ok;
    static Method get, getEquipped, getAll;

    static void init() {
        if (tried) return;
        tried = true;
        try {
            Class<?> cap = Class.forName("io.wispforest.accessories.api.AccessoriesCapability");
            get = cap.getMethod("get", LivingEntity.class);
            try { getEquipped = cap.getMethod("getEquipped", Predicate.class); } catch (Throwable ignored) {}
            try { getAll = cap.getMethod("getAllEquipped"); } catch (Throwable ignored) {}
            ok = getEquipped != null || getAll != null;
        } catch (Throwable t) {
            ok = false;
        }
    }

    static ItemStack stackOf(Object ref) {
        try {
            Object s;
            try { s = ref.getClass().getMethod("stack").invoke(ref); }
            catch (NoSuchMethodException e) { s = ref.getClass().getMethod("getStack").invoke(ref); }
            if (s instanceof ItemStack st) return st;
        } catch (Throwable ignored) {}
        return null;
    }

    /** Primeira pérola guardada nos slots do Accessories/Curios, ou null. */
    public static ItemStack firstPearl(ServerPlayerEntity p) {
        init();
        if (!ok) return null;
        try {
            Object cap = get.invoke(null, p);
            if (cap == null) return null;
            Iterable<?> list;
            if (getEquipped != null) {
                Predicate<ItemStack> pred = st -> st.getItem() instanceof AuroraItems.PearlItem;
                list = (Iterable<?>) getEquipped.invoke(cap, pred);
            } else {
                list = (Iterable<?>) getAll.invoke(cap);
            }
            for (Object ref : list) {
                ItemStack st = stackOf(ref);
                if (st != null && st.getItem() instanceof AuroraItems.PearlItem) return st;
            }
        } catch (Throwable ignored) {}
        return null;
    }
}
