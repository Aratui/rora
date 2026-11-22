package br.guardians.aurora;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Servidor -> cliente: dados do painel (deus, nível, time, facção e pontos). */
public record StatusPayload(String god, int level, String team, String faction, int teamPts, int facPts)
        implements CustomPayload {
    public static final CustomPayload.Id<StatusPayload> ID = new CustomPayload.Id<>(AuroraMod.id("status"));
    public static final PacketCodec<RegistryByteBuf, StatusPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, StatusPayload::god,
            PacketCodecs.INTEGER, StatusPayload::level,
            PacketCodecs.STRING, StatusPayload::team,
            PacketCodecs.STRING, StatusPayload::faction,
            PacketCodecs.INTEGER, StatusPayload::teamPts,
            PacketCodecs.INTEGER, StatusPayload::facPts,
            StatusPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
}
