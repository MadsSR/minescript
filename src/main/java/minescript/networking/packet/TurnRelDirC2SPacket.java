package minescript.networking.packet;

import interpreter.types.MSRelDir;
import minescript.block.custom.TurtleBlock;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class TurnRelDirC2SPacket {
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {
        ServerWorld world = player.getWorld();

        BlockPos pos = buf.readBlockPos();
        MSRelDir.Direction direction = MSRelDir.Direction.values()[buf.readInt()];
        BlockState state = world.getBlockState(pos);
        Direction facing = state.get(Properties.HORIZONTAL_FACING);
        WallMountLocation face = state.get(TurtleBlock.FACE);

        if (face == WallMountLocation.WALL) {
            switch (direction) {
                case LEFT -> facing = facing.rotateYCounterclockwise();
                case RIGHT -> facing = facing.rotateYClockwise();
                case UP -> face = WallMountLocation.CEILING;
                case DOWN -> face = WallMountLocation.FLOOR;
            }
        }
        else if (face == WallMountLocation.CEILING) {
            switch (direction) {
                case LEFT -> facing = facing.rotateYCounterclockwise();
                case RIGHT -> facing = facing.rotateYClockwise();
                case UP -> {
                    facing = facing.getOpposite();
                    face = WallMountLocation.WALL;
                }
                case DOWN -> face = WallMountLocation.WALL;
            }
        }
        else if (face == WallMountLocation.FLOOR) {
            switch (direction) {
                case LEFT -> facing = facing.rotateYCounterclockwise();
                case RIGHT -> facing = facing.rotateYClockwise();
                case UP -> face = WallMountLocation.WALL;
                case DOWN -> {
                    facing = facing.getOpposite();
                    face = WallMountLocation.WALL;
                }
            }
        }

        world.setBlockState(pos, state.with(Properties.HORIZONTAL_FACING, facing).with(TurtleBlock.FACE, face), Block.NOTIFY_ALL);
    }
}
