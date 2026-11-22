package br.guardians.aurora;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Pacote servidor -> cliente: avisa o início de uma recarga para mostrar na tela. */
public record CdPayload(String key, int ticks, String label) implements CustomPayload {
    public static final CustomPayload.Id<CdPayload> ID = new CustomPayload.Id<>(AuroraMod.id("cd"));
    public static final PacketCodec<RegistryByteBuf, CdPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, CdPayload::key,
            PacketCodecs.INTEGER, CdPayload::ticks,
            PacketCodecs.STRING, CdPayload::label,
            CdPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
}
