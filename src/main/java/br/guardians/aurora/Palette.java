package br.guardians.aurora;

import net.minecraft.util.Identifier;

import java.util.Map;

/** Gerado automaticamente: cor de destaque de cada item e as fontes do mod. */
public class Palette {
    public static final Identifier TITLE = AuroraMod.id("title");
    public static final Identifier SKILL = AuroraMod.id("skill");
    static final Map<String, Integer> COL = Map.ofEntries(
        Map.entry("pearl_ignis", 0xFF8C3F),
        Map.entry("pearl_zefiro", 0xA5F2FF),
        Map.entry("pearl_mare", 0x59D0FF),
        Map.entry("pearl_terra", 0x8CFF72),
        Map.entry("pearl_trovao", 0xFFF259),
        Map.entry("pearl_sombra", 0xD072FF),
        Map.entry("pearl_tempo", 0x72FFE1),
        Map.entry("pearl_gelo", 0xB6F2FF),
        Map.entry("pearl_luz", 0xFFFAB6),
        Map.entry("pearl_alma", 0x6AFFE1),
        Map.entry("pearl_fada", 0xFFA5FF),
        Map.entry("pearl_fenix", 0xFFB648),
        Map.entry("pearl_medusa", 0xA5FF59),
        Map.entry("pearl_dragao", 0xFF613F),
        Map.entry("pearl_kraken", 0x839DFF),
        Map.entry("pearl_vampiro", 0xFF4872),
        Map.entry("essence_zeus", 0x9DE1FF),
        Map.entry("essence_poseidon", 0x59FFE9),
        Map.entry("essence_hades", 0x8CFFA5),
        Map.entry("essence_athena", 0xFFE58C),
        Map.entry("essence_apollo", 0xFFE159),
        Map.entry("essence_ares", 0xFF7248),
        Map.entry("essence_hermes", 0xFFE98C),
        Map.entry("essence_artemis", 0xC7FFE9),
        Map.entry("essence_hera", 0x72E9E9),
        Map.entry("essence_hefesto", 0xFFA548),
        Map.entry("essence_dionisio", 0xFF72D0),
        Map.entry("essence_demeter", 0xFFE57B)
    );

    public static int color(String id) { return COL.getOrDefault(id, 0xFFFFFF); }
}
