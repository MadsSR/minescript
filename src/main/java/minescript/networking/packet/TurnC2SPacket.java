package minescript.networking.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class TurnC2SPacket {
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {
        ServerWorld world = player.getWorld();

        BlockPos pos = buf.readBlockPos();
        int degrees = buf.readInt();
        BlockState state = world.getBlockState(pos);
        Direction dir = state.get(Properties.HORIZONTAL_FACING);

        switch (degrees) {
            case 90 -> dir = dir.rotateYClockwise();
            case -90 -> dir = dir.rotateYCounterclockwise();
        }

        world.setBlockState(pos, state.with(Properties.HORIZONTAL_FACING, dir), Block.NOTIFY_ALL);
    }
}
