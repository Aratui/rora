package br.guardians.aurora;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Servidor -> cliente: habilidade selecionada ("e" = essência, "p" = pérola). Texto vazio = nada. */
public record SelPayload(String key, String text) implements CustomPayload {
    public static final CustomPayload.Id<SelPayload> ID = new CustomPayload.Id<>(AuroraMod.id("sel"));
    public static final PacketCodec<RegistryByteBuf, SelPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, SelPayload::key,
            PacketCodecs.STRING, SelPayload::text,
            SelPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
}
