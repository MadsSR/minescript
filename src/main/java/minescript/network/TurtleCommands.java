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

    /**
     * @param server       The server instance
     * @param world        The world instance
     * @param placingBlock The block to place
     * @param pos          The position of the turtle
     * @return A future that completes when the block has been placed
     */
    public static CompletableFuture<BlockPos> step(MinecraftServer server, ServerWorld world, Block placingBlock, BlockPos pos) {
        CompletableFuture<BlockPos> future = new CompletableFuture<>();

        // Execute on main thread
        server.executeSync(() -> {
            TurtleBlockEntity entity = (TurtleBlockEntity) world.getBlockEntity(pos);
            assert entity != null;

            BlockPos oldPos = entity.turtlePos;
            BlockState state = world.getBlockState(entity.turtlePos);

            entity.turtlePos = getBlockPosInFront(state, entity.turtlePos);

            // world.breakBlock(entity.turtlePos, true);

            world.removeBlock(oldPos, true);
            world.setBlockState(oldPos, placingBlock.getDefaultState(), Block.NOTIFY_ALL);
            world.setBlockState(entity.turtlePos, state, Block.NOTIFY_ALL);

            entity = entity.getTurtleEntity();
            future.complete(entity.turtlePos);
        });

        return future;
    }

    /**
     * @param server The server instance
     * @param world  The world instance
     * @param pos    The position of the turtle
     * @param newPos The new position of the turtle
     * @return A future that completes when the turtle has been moved
     */
    public static CompletableFuture<BlockPos> setPosition(MinecraftServer server, ServerWorld world, BlockPos pos, BlockPos newPos) {
        CompletableFuture<BlockPos> future = new CompletableFuture<>();

        server.executeSync(() -> {
            TurtleBlockEntity entity = (TurtleBlockEntity) world.getBlockEntity(pos);
            assert entity != null;

            BlockPos oldPos = entity.turtlePos;
            BlockState state = world.getBlockState(entity.turtlePos);

            entity.turtlePos = newPos;

            world.removeBlock(oldPos, true);
            world.setBlockState(entity.turtlePos, state, Block.NOTIFY_ALL);

            entity = entity.getTurtleEntity();
            future.complete(entity.turtlePos);
        });

        return future;
    }

    /**
     * @param server    The server instance
     * @param world     The world instance
     * @param pos       The position of the turtle
     * @param direction The relative direction to turn
     */
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
            } else if (face == WallMountLocation.CEILING) {
                switch (direction) {
                    case LEFT -> facing = facing.rotateYCounterclockwise();
                    case RIGHT -> facing = facing.rotateYClockwise();
                    case UP -> {
                        facing = facing.getOpposite();
                        face = WallMountLocation.WALL;
                    }
                    case DOWN -> face = WallMountLocation.WALL;
                }
            } else if (face == WallMountLocation.FLOOR) {
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

    /**
     * @param server    The server instance
     * @param world     The world instance
     * @param pos       The position of the turtle
     * @param direction The absolute direction to turn
     */
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

    /**
     * @param world The world instance
     * @param pos   The position of the turtle
     * @return The block in front of the turtle
     */
    public static Block peek(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        if (!state.contains(TurtleBlock.FACE) || !state.contains(Properties.HORIZONTAL_FACING))
            throw new RuntimeException("Property does not exist");

        BlockPos peekPos = getBlockPosInFront(state, pos);

        return world.getBlockState(peekPos).getBlock();
    }

    /**
     * @param state The state of the turtle
     * @param pos   The position of the turtle
     * @return The position in front of the turtle
     */
    private static BlockPos getBlockPosInFront(BlockState state, BlockPos pos) {
        if (state.get(TurtleBlock.FACE) == WallMountLocation.WALL) {
            switch (state.get(Properties.HORIZONTAL_FACING)) {
                case NORTH -> pos = pos.north(1);
                case SOUTH -> pos = pos.south(1);
                case EAST -> pos = pos.east(1);
                case WEST -> pos = pos.west(1);
            }
        } else {
            switch (state.get(TurtleBlock.FACE)) {
                case FLOOR -> pos = pos.down(1);
                case CEILING -> pos = pos.up(1);
            }
        }
        return pos;
    }

    /**
     * @param world The world instance
     * @param pos   The position of the turtle
     * @return The absolute direction of the turtle on the horizontal axis
     */
    public static MSAbsDir getHorizontalDirection(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        if (state.contains(Properties.HORIZONTAL_FACING)) {
            return new MSAbsDir(state.get(Properties.HORIZONTAL_FACING).toString());
        }

        return new MSAbsDir(MSAbsDir.Direction.NORTH);
    }

    /**
     * @param world The world instance
     * @param pos   The position of the turtle
     * @return The absolute direction of the turtle on the vertical axis
     */
    public static MSAbsDir getVerticalDirection(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        if (state.contains(TurtleBlock.FACE)) {
            switch (state.get(TurtleBlock.FACE)) {
                case FLOOR -> {
                    return new MSAbsDir(MSAbsDir.Direction.BOTTOM);
                }
                case CEILING -> {
                    return new MSAbsDir(MSAbsDir.Direction.TOP);
                }
            }
        }

        return getHorizontalDirection(world, pos);
    }

    /**
     * @param server  The server instance
     * @param message The message to print
     * @param type    The type of message
     */
    public static void print(MinecraftServer server, String message, MSMessageType type) {

        // Sets the message color and text based on the type
        switch (type) {
            case ERROR -> message = "§cERROR:§r " + message;
            case WARNING -> message = "§eWARNING:§r " + message;
            case INFO -> message = "§aINFO:§r " + message;
        }

        Text msgText = Text.of(message);

        // Execute on the main thread
        server.executeSync(() -> {
            // Send the message to all players
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                player.sendMessage(msgText, false);
            }
        });
    }

}
