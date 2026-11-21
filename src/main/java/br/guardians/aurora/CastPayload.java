package br.guardians.aurora;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Pacote cliente -> servidor. kind: 0 = essência, 1 = pérola. */
public record CastPayload(int kind) implements CustomPayload {
    public static final CustomPayload.Id<CastPayload> ID = new CustomPayload.Id<>(AuroraMod.id("cast"));
    public static final PacketCodec<RegistryByteBuf, CastPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.INTEGER, CastPayload::kind, CastPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
}
