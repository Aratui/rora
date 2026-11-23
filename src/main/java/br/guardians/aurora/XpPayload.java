package br.guardians.aurora;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Servidor -> cliente: XP atual e XP necessário para o próximo nível (need = 0 no nível máximo). */
public record XpPayload(int xp, int need) implements CustomPayload {
    public static final CustomPayload.Id<XpPayload> ID = new CustomPayload.Id<>(AuroraMod.id("xp"));
    public static final PacketCodec<RegistryByteBuf, XpPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, XpPayload::xp,
            PacketCodecs.INTEGER, XpPayload::need,
            XpPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
}
