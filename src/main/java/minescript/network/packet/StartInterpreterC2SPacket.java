package minescript.network.packet;

import interpreter.Interpreter;
import minescript.block.entity.TurtleBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class StartInterpreterC2SPacket {
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        BlockPos pos = buf.readBlockPos();
        String program = buf.readString();
        
        server.executeSync(() -> {
            ServerWorld world = player.getWorld();
            TurtleBlockEntity entity = (TurtleBlockEntity) world.getBlockEntity(pos);

            assert entity != null;
            entity.interpreterThread = new Thread(new Interpreter(program, server, world, pos));
            entity.interpreterThread.start();
        });
    }
}
