package br.guardians.aurora;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class AuroraItems {
    public static final Map<Pearl, Item> PEARLS = new EnumMap<>(Pearl.class);
    public static Item ESSENCE;

    public static class PearlItem extends Item {
        final Pearl pearl;
        PearlItem(Pearl pearl) { super(new Item.Settings().maxCount(1)); this.pearl = pearl; }

        @Override public Text getName(ItemStack stack) { return Text.literal(pearl.label); }

        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            ItemStack st = user.getStackInHand(hand);
            if (!world.isClient && user instanceof ServerPlayerEntity sp) {
                pearl.ability.cast(sp, (ServerWorld) world);
                user.getItemCooldownManager().set(this, pearl.cooldown);
            }
            return TypedActionResult.success(st, world.isClient());
        }
    }

    public static class EssenceItem extends Item {
        EssenceItem() { super(new Item.Settings().maxCount(1)); }

        @Override public Text getName(ItemStack stack) { return Text.literal("Essência Divina"); }

        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            ItemStack st = user.getStackInHand(hand);
            if (!world.isClient && user instanceof ServerPlayerEntity sp) {
                AuroraData.Info i = AuroraData.get(sp.getUuid());
                God g = God.byKey(i.god);
                if (g == null) {
                    sp.sendMessage(Text.literal("Você ainda não tem um deus."), true);
                    return TypedActionResult.fail(st);
                }
                g.ability.cast(sp, (ServerWorld) world, i.level);
                user.getItemCooldownManager().set(this, Math.max(60, 300 - i.level * 20));
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
        ESSENCE = Registry.register(Registries.ITEM, AuroraMod.id("essence"), new EssenceItem());
        all.add(ESSENCE);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(e -> all.forEach(e::add));
    }
}
