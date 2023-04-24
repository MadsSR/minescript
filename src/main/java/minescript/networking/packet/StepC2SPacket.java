package minescript.networking.packet;

import interpreter.types.MSMessageType;
import minescript.block.custom.TurtleBlock;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.WallMountLocation;
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
        int id = buf.readInt();
        BlockState placingBlockState = Block.getStateFromRawId(id);

        BlockPos newPos = pos;
        BlockState state = world.getBlockState(pos);

        if (!state.contains(TurtleBlock.FACE) || !state.contains(Properties.HORIZONTAL_FACING)) {
            PrintC2SPacket.print("Property does not exist", player, MSMessageType.ERROR);
            PrintC2SPacket.print("POS: " + pos, player, MSMessageType.INFO);
            return;
        }

        if (state.get(TurtleBlock.FACE) == WallMountLocation.WALL) {
            switch (state.get(Properties.HORIZONTAL_FACING)) {
                case NORTH -> newPos = newPos.north(1);
                case SOUTH -> newPos = newPos.south(1);
                case EAST -> newPos = newPos.east(1);
                case WEST -> newPos = newPos.west(1);
            }
        }
        else {
            switch (state.get(TurtleBlock.FACE)) {
                case FLOOR -> newPos = newPos.down(1);
                case CEILING -> newPos = newPos.up(1);
            }
        }

        world.setBlockState(newPos, state, Block.NOTIFY_ALL);
        world.setBlockState(pos, placingBlockState, Block.NOTIFY_ALL);

    }
}
