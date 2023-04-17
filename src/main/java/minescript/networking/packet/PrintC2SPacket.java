package minescript.networking.packet;

import interpreter.types.MSMessageType;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class PrintC2SPacket {
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {
        // This code runs on the server, so you can use server-side only methods here.
        ServerWorld world = player.getWorld();
        String message = buf.readString();
        int type = buf.readInt();

        switch (MSMessageType.values()[type]) {
            case ERROR -> message = "§cERROR:§r " + message;
            case WARNING -> message = "§eWARNING:§r " + message;
            case INFO -> message = "§aINFO:§r " + message;
        }

        player.sendMessage(Text.of(message));
    }
}
