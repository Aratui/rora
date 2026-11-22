package br.guardians.aurora;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class AuroraItems {
    public static final Map<Pearl, Item> PEARLS = new EnumMap<>(Pearl.class);
    public static final Map<God, Item> ESSENCES = new EnumMap<>(God.class);

    static void tip(List<Text> t, String desc, String use, Skill[] sk) {
        t.add(Text.literal(desc).formatted(Formatting.GRAY));
        t.add(Text.literal(use).formatted(Formatting.YELLOW));
        t.add(Text.literal("Habilidades (desbloqueiam ao subir de nível):").formatted(Formatting.GOLD));
        for (Skill s : sk)
            t.add(Text.literal(" Nv." + s.level() + " " + s.name() + ": ").formatted(Formatting.GREEN)
                    .append(Text.literal(s.desc()).formatted(Formatting.WHITE)));
        t.add(Text.literal("Matar um jogador: +1 nível. Morrer: -1 nível.").formatted(Formatting.DARK_GRAY));
    }

    public static class PearlItem extends Item {
        final Pearl pearl;
        PearlItem(Pearl pearl) { super(new Item.Settings().maxCount(1)); this.pearl = pearl; }

        @Override public Text getName(ItemStack stack) { return Text.literal(pearl.label); }
        @Override public boolean hasGlint(ItemStack stack) { return true; }

        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext ctx, List<Text> t, TooltipType type) {
            tip(t, pearl.desc, "Clique direito ou tecla G para usar. Recarga base: " + (pearl.cooldown / 20)
                    + "s (diminui com o nível).", pearl.skills);
        }

        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            ItemStack st = user.getStackInHand(hand);
            if (!world.isClient && user instanceof ServerPlayerEntity sp) {
                int cd = AuroraCast.pearl(sp, pearl);
                if (cd > 0) user.getItemCooldownManager().set(this, cd);
            }
            return TypedActionResult.success(st, world.isClient());
        }
    }

    public static class EssenceItem extends Item {
        final God god;
        EssenceItem(God god) { super(new Item.Settings().maxCount(1)); this.god = god; }

        @Override public Text getName(ItemStack stack) { return Text.literal("Essência de " + god.label); }
        @Override public boolean hasGlint(ItemStack stack) { return true; }

        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext ctx, List<Text> t, TooltipType type) {
            tip(t, god.desc, "Clique direito ou tecla R (sem precisar do item) para usar. Só o deus " + god.label
                    + " pode usar a essência dele. Recarga cai com o nível.", god.skills);
        }

        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            ItemStack st = user.getStackInHand(hand);
            if (!world.isClient && user instanceof ServerPlayerEntity sp) {
                int cd = AuroraCast.essence(sp, god);
                if (cd == 0) return TypedActionResult.fail(st);
                user.getItemCooldownManager().set(this, cd);
            }
            return TypedActionResult.success(st, world.isClient());
        }
    }

    public static void register() {
        List<Item> all = new ArrayList<>();
        for (Pearl p : Pearl.values()) {
            Item it = Registry.register(Registries.ITEM, AuroraMod.id("pearl_" + p.key), new PearlItem(p));
            PEARLS.put(p, it);
            all.add(it);
        }
        for (God g : God.values()) {
            Item it = Registry.register(Registries.ITEM, AuroraMod.id("essence_" + g.key), new EssenceItem(g));
            ESSENCES.put(g, it);
            all.add(it);
        }
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(e -> all.forEach(e::add));
    }
}
