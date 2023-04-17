package minescript.networking.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;

public class StepC2SPacket {
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {
        // This code runs on the server, so you can use server-side only methods here.
        ServerWorld world = player.getWorld();

        BlockPos pos = buf.readBlockPos();
        int steps = buf.readInt();
        int id = buf.readInt();
        BlockState placingBlockState = Block.getStateFromRawId(id);

        BlockPos newPos = pos;
        BlockState state = world.getBlockState(pos);

        switch (state.get(Properties.HORIZONTAL_FACING)) {
            case NORTH -> newPos = newPos.north(steps);
            case SOUTH -> newPos = newPos.south(steps);
            case EAST -> newPos = newPos.east(steps);
            case WEST -> newPos = newPos.west(steps);
        }

        world.setBlockState(newPos, state, Block.NOTIFY_ALL);
        world.setBlockState(pos, placingBlockState, Block.NOTIFY_ALL);

    }
}
