package dev.atcrock.totemfx;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import io.netty.buffer.Unpooled;

public class TotemFX implements ModInitializer {
    public static final String MODID = "totemfx";
    public static final Identifier START_FX = new Identifier(MODID, "start_fx");

    @Override
    public void onInitialize() {
    }

    public static void sendStartFX(ServerPlayerEntity player) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeUuid(player.getUuid());
        ServerPlayNetworking.send(player, START_FX, buf);
    }
}
