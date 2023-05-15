package minescript.network;

import interpreter.types.MSAbsDir;
import interpreter.types.MSMessageType;
import interpreter.types.MSRelDir;
import minescript.block.custom.TurtleBlock;
import minescript.block.entity.TurtleBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.concurrent.CompletableFuture;

public class TurtleCommands {
    public static CompletableFuture<BlockPos> step(MinecraftServer server, ServerWorld world, Block placingBlock, BlockPos pos) {
        CompletableFuture<BlockPos> future = new CompletableFuture<>();

        server.executeSync(() -> {
            TurtleBlockEntity entity = (TurtleBlockEntity) world.getBlockEntity(pos);
            assert entity != null;

            BlockPos oldPos = entity.turtlePos;
            BlockState state = world.getBlockState(entity.turtlePos);

            if (state.get(TurtleBlock.FACE) == WallMountLocation.WALL) {
                switch (state.get(Properties.HORIZONTAL_FACING)) {
                    case NORTH -> entity.turtlePos = entity.turtlePos.north(1);
                    case SOUTH -> entity.turtlePos = entity.turtlePos.south(1);
                    case EAST -> entity.turtlePos = entity.turtlePos.east(1);
                    case WEST -> entity.turtlePos = entity.turtlePos.west(1);
                }
            } else {
                switch (state.get(TurtleBlock.FACE)) {
                    case FLOOR -> entity.turtlePos = entity.turtlePos.down(1);
                    case CEILING -> entity.turtlePos = entity.turtlePos.up(1);
                }
            }

            // world.breakBlock(entity.turtlePos, true);
            world.setBlockState(entity.turtlePos, state, Block.NOTIFY_ALL);
            world.setBlockState(oldPos, placingBlock.getDefaultState(), Block.NOTIFY_ALL);

            entity = entity.getTurtleEntity();
            future.complete(entity.turtlePos);
        });

        return future;
    }

    public static CompletableFuture<BlockPos> setPosition(MinecraftServer server, ServerWorld world, BlockPos pos, BlockPos newPos) {
        CompletableFuture<BlockPos> future = new CompletableFuture<>();

        server.executeSync(() -> {
            TurtleBlockEntity entity = (TurtleBlockEntity) world.getBlockEntity(pos);
            assert entity != null;

            BlockPos oldPos = entity.turtlePos;
            BlockState state = world.getBlockState(entity.turtlePos);

            entity.turtlePos = newPos;

            world.removeBlock(oldPos, false);
            world.setBlockState(entity.turtlePos, state, Block.NOTIFY_ALL);

            entity = entity.getTurtleEntity();
            future.complete(entity.turtlePos);
        });

        return future;
    }

    public static void turn(MinecraftServer server, ServerWorld world, BlockPos pos, MSRelDir.Direction direction) {

        server.executeSync(() -> {
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
        });
    }

    public static void turn(MinecraftServer server, ServerWorld world, BlockPos pos, MSAbsDir.Direction direction) {
        server.executeSync(() -> {
            BlockState state = world.getBlockState(pos);

            if (!state.contains(TurtleBlock.FACE) || !state.contains(Properties.HORIZONTAL_FACING)) {
                print(server, "Property does not exist", MSMessageType.ERROR);
                return;
            }

            Direction facing = state.get(Properties.HORIZONTAL_FACING);
            WallMountLocation face = state.get(TurtleBlock.FACE);

            switch (direction) {
                case NORTH -> facing = Direction.NORTH;
                case EAST -> facing = Direction.EAST;
                case SOUTH -> facing = Direction.SOUTH;
                case WEST -> facing = Direction.WEST;
                case TOP -> face = WallMountLocation.CEILING;
                case BOTTOM -> face = WallMountLocation.FLOOR;
            }

            world.setBlockState(pos, state.with(Properties.HORIZONTAL_FACING, facing).with(TurtleBlock.FACE, face), Block.NOTIFY_ALL);
        });
    }

    public static Block peek(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        BlockPos peekPos = pos;

        if (!state.contains(TurtleBlock.FACE) || !state.contains(Properties.HORIZONTAL_FACING))
            throw new RuntimeException("Property does not exist");

        if (state.get(TurtleBlock.FACE) == WallMountLocation.WALL) {
            switch (state.get(Properties.HORIZONTAL_FACING)) {
                case NORTH -> peekPos = peekPos.north(1);
                case SOUTH -> peekPos = peekPos.south(1);
                case EAST -> peekPos = peekPos.east(1);
                case WEST -> peekPos = peekPos.west(1);
            }
        } else {
            switch (state.get(TurtleBlock.FACE)) {
                case FLOOR -> peekPos = peekPos.down(1);
                case CEILING -> peekPos = peekPos.up(1);
            }
        }

        return world.getBlockState(peekPos).getBlock();
    }

    public static String getHorizontalDirection(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        if (state.contains(Properties.HORIZONTAL_FACING)) {
            return state.get(Properties.HORIZONTAL_FACING).toString();
        }

        return "north";
    }

    public static String getVerticalDirection(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        if (state.contains(TurtleBlock.FACE)) {
            switch (state.get(TurtleBlock.FACE)) {
                case FLOOR -> {
                    return "bottom";
                }
                case CEILING -> {
                    return "top";
                }
            }
        }

        if (state.contains(Properties.HORIZONTAL_FACING)) {
            return state.get(Properties.HORIZONTAL_FACING).toString();
        }

        return "north";
    }

    public static void print(MinecraftServer server, String message, MSMessageType type) {
        switch (type) {
            case ERROR -> message = "§cERROR:§r " + message;
            case WARNING -> message = "§eWARNING:§r " + message;
            case INFO -> message = "§aINFO:§r " + message;
        }

        Text msgText = Text.of(message);

        server.executeSync(() -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                player.sendMessage(msgText, false);
            }
        });
    }

}
