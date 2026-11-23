package br.guardians.aurora;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class AuroraCommands {
    static final List<String> FACTIONS = List.of("arcana", "aurora", "hidra");
    static final List<String> TEAMS = List.of("spy", "olympus", "gdc");
    static final List<String> GODS = Arrays.stream(God.values()).map(g -> g.key).toList();
    static final List<String> PEARLS = Arrays.stream(Pearl.values()).map(p -> p.key).toList();

    static RequiredArgumentBuilder<ServerCommandSource, String> word(String name, List<String> opts) {
        return argument(name, StringArgumentType.word())
                .suggests((c, b) -> CommandSource.suggestMatching(opts, b));
    }

    static boolean admin(ServerCommandSource s) {
        return s.hasPermissionLevel(2) || s.getServer().isSingleplayer();
    }

    static void msg(com.mojang.brigadier.context.CommandContext<ServerCommandSource> c, String s) {
        c.getSource().sendFeedback(() -> Text.literal(s), false);
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((d, reg, env) -> d.register(
            literal("aurora").executes(c -> {
                msg(c, "Aurora: /aurora info | skills | score | faction join <nome> | god set | team set | level set/add | xp add | pearl give | essence give <deus>");
                return 1;
            })
                // ---- facções (S1) ----
                .then(literal("faction")
                    .then(literal("join").then(word("nome", FACTIONS).executes(c -> {
                        String n = StringArgumentType.getString(c, "nome").toLowerCase();
                        if (!FACTIONS.contains(n)) { msg(c, "Facção inválida."); return 0; }
                        ServerPlayerEntity p = c.getSource().getPlayerOrThrow();
                        AuroraData.get(p.getUuid()).faction = n;
                        AuroraData.save(c.getSource().getServer());
                        msg(c, "Você entrou na facção " + n);
                        return 1;
                    })))
                    .then(literal("leave").executes(c -> {
                        ServerPlayerEntity p = c.getSource().getPlayerOrThrow();
                        AuroraData.get(p.getUuid()).faction = "";
                        AuroraData.save(c.getSource().getServer());
                        msg(c, "Você saiu da facção.");
                        return 1;
                    })))
                // ---- times (S2) ----
                .then(literal("team").requires(AuroraCommands::admin)
                    .then(literal("set").then(argument("players", EntityArgumentType.players())
                        .then(word("time", TEAMS).executes(c -> {
                            String t = StringArgumentType.getString(c, "time").toLowerCase();
                            if (!TEAMS.contains(t)) { msg(c, "Time inválido."); return 0; }
                            for (ServerPlayerEntity p : EntityArgumentType.getPlayers(c, "players"))
                                AuroraData.get(p.getUuid()).team = t;
                            AuroraData.save(c.getSource().getServer());
                            msg(c, "Time definido: " + t);
                            return 1;
                        })))))
                // ---- deuses ----
                .then(literal("god").requires(AuroraCommands::admin)
                    .then(literal("set").then(argument("players", EntityArgumentType.players())
                        .then(word("deus", GODS).executes(c -> {
                            God g = God.byKey(StringArgumentType.getString(c, "deus").toLowerCase());
                            if (g == null) { msg(c, "Deus inválido."); return 0; }
                            for (ServerPlayerEntity p : EntityArgumentType.getPlayers(c, "players")) {
                                AuroraData.Info i = AuroraData.get(p.getUuid());
                                i.god = g.key; i.level = 1;
                            }
                            AuroraData.save(c.getSource().getServer());
                            msg(c, "Deus definido: " + g.label);
                            return 1;
                        })))))
                // ---- níveis ----
                .then(literal("level").requires(AuroraCommands::admin)
                    .then(literal("set").then(argument("players", EntityArgumentType.players())
                        .then(argument("n", IntegerArgumentType.integer(1, God.MAX_LEVEL)).executes(c -> {
                            int n = IntegerArgumentType.getInteger(c, "n");
                            for (ServerPlayerEntity p : EntityArgumentType.getPlayers(c, "players"))
                                AuroraData.get(p.getUuid()).level = n;
                            AuroraData.save(c.getSource().getServer());
                            msg(c, "Nível definido: " + n);
                            return 1;
                        }))))
                    .then(literal("add").then(argument("players", EntityArgumentType.players())
                        .then(argument("n", IntegerArgumentType.integer(-9, 9)).executes(c -> {
                            int n = IntegerArgumentType.getInteger(c, "n");
                            for (ServerPlayerEntity p : EntityArgumentType.getPlayers(c, "players")) {
                                AuroraData.Info i = AuroraData.get(p.getUuid());
                                i.level = Math.max(1, Math.min(God.MAX_LEVEL, i.level + n));
                            }
                            AuroraData.save(c.getSource().getServer());
                            msg(c, "Nível ajustado.");
                            return 1;
                        })))))
                // ---- itens ----
                .then(literal("xp").requires(AuroraCommands::admin)
                    .then(literal("add").then(argument("players", EntityArgumentType.players())
                        .then(argument("n", IntegerArgumentType.integer(1, 5000)).executes(c -> {
                            int n = IntegerArgumentType.getInteger(c, "n");
                            for (ServerPlayerEntity p : EntityArgumentType.getPlayers(c, "players"))
                                AuroraEvents.addXp(p, n);
                            AuroraData.save(c.getSource().getServer());
                            msg(c, "XP adicionado: " + n);
                            return 1;
                        })))))
                .then(literal("pearl").requires(AuroraCommands::admin)
                    .then(literal("give").then(argument("players", EntityArgumentType.players())
                        .then(word("perola", PEARLS).executes(c -> {
                            Pearl pr = Pearl.byKey(StringArgumentType.getString(c, "perola").toLowerCase());
                            if (pr == null) { msg(c, "Pérola inválida."); return 0; }
                            for (ServerPlayerEntity p : EntityArgumentType.getPlayers(c, "players"))
                                p.giveItemStack(new ItemStack(AuroraItems.PEARLS.get(pr)));
                            msg(c, "Pérola entregue.");
                            return 1;
                        })))))
                .then(literal("essence").requires(AuroraCommands::admin)
                    .then(literal("give").then(argument("players", EntityArgumentType.players())
                        .then(word("deus", GODS).executes(c -> {
                            God g = God.byKey(StringArgumentType.getString(c, "deus").toLowerCase());
                            if (g == null) { msg(c, "Deus inválido."); return 0; }
                            for (ServerPlayerEntity p : EntityArgumentType.getPlayers(c, "players"))
                                p.giveItemStack(new ItemStack(AuroraItems.ESSENCES.get(g)));
                            msg(c, "Essência entregue.");
                            return 1;
                        })))))
                // ---- info / placar ----
                .then(literal("skills").executes(c -> {
                    AuroraData.Info i = AuroraData.get(c.getSource().getPlayerOrThrow().getUuid());
                    God g = God.byKey(i.god);
                    if (g == null) { msg(c, "Você não tem um deus."); return 0; }
                    msg(c, g.label + " - Nível " + i.level);
                    for (Skill s : g.skills)
                        msg(c, (i.level >= s.level() ? "[OK] " : "[Nv." + s.level() + "] ") + s.name() + ": " + s.desc());
                    return 1;
                }))
                .then(literal("info").executes(c -> {
                    AuroraData.Info i = AuroraData.get(c.getSource().getPlayerOrThrow().getUuid());
                    msg(c, "Deus: " + (i.god.isEmpty() ? "-" : i.god) + " | Nível: " + i.level
                            + " | Time: " + (i.team.isEmpty() ? "-" : i.team)
                            + " | Facção: " + (i.faction.isEmpty() ? "-" : i.faction));
                    return 1;
                }))
                .then(literal("score").executes(c -> {
                    if (AuroraData.scores().isEmpty()) { msg(c, "Placar vazio."); return 1; }
                    AuroraData.scores().forEach((k, v) -> msg(c, k + ": " + v));
                    return 1;
                }))
        ));
    }
}
