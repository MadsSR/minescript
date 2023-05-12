package minescript.block.entity;

import interpreter.Interpreter;
import interpreter.types.MSAbsDir;
import interpreter.types.MSMessageType;
import interpreter.types.MSRelDir;
import minescript.block.custom.TurtleBlock;
import minescript.networking.MineScriptPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TurtleBlockEntity extends BlockEntity {

    private Block placingBlock;
    private BlockPos turtlePos;
    private Thread interpreterThread;
    private int actionDelay;
    public boolean shouldBreak;
    public Text input;

    public TurtleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURTLE_BLOCK_ENTITY, pos, state);
        actionDelay = 500;
        turtlePos = pos;
        shouldBreak = true;
        placingBlock = Blocks.AIR;
        input = Text.empty();
    }

    public TurtleBlockEntity getTurtleEntity() {
        if (world.getBlockEntity(turtlePos) instanceof TurtleBlockEntity turtleEntity) {
            turtleEntity.interpreterThread = interpreterThread;
            turtleEntity.actionDelay = actionDelay;
            turtleEntity.input = input;
            turtleEntity.turtlePos = turtlePos;
            turtleEntity.placingBlock = placingBlock;
            turtleEntity.shouldBreak = shouldBreak;
            return turtleEntity;
        }
        return null;
    }

    public static void tick(World world, BlockPos pos, BlockState state, TurtleBlockEntity entity) {
    }

    /**
     * @param steps The amount of steps to move forward
     */
    public void step(int steps) {
        for (int i = 0; i < steps; i++) {
            timeout();

            if (!shouldBreak && peek() != Blocks.AIR) {
                print("Cannot move forward, block in the way", MSMessageType.WARNING);
                return;
            }

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(turtlePos);
            buf.writeInt(Block.getRawIdFromState(placingBlock.getDefaultState()));

            ClientPlayNetworking.send(MineScriptPackets.STEP_ID, buf);

            BlockState state = world.getBlockState(turtlePos);
            BlockPos oldPos = turtlePos;


            if (state.get(TurtleBlock.FACE) == WallMountLocation.WALL) {
                switch (state.get(Properties.HORIZONTAL_FACING)) {
                    case NORTH -> turtlePos = turtlePos.north(1);
                    case SOUTH -> turtlePos = turtlePos.south(1);
                    case EAST -> turtlePos = turtlePos.east(1);
                    case WEST -> turtlePos = turtlePos.west(1);
                }
            } else {
                switch (state.get(TurtleBlock.FACE)) {
                    case FLOOR -> turtlePos = turtlePos.down(1);
                    case CEILING -> turtlePos = turtlePos.up(1);
                }
            }

            world.setBlockState(turtlePos, state, Block.NOTIFY_ALL);
            world.setBlockState(oldPos, placingBlock.getDefaultState(), Block.NOTIFY_ALL);
        }
    }

    /**
     * @param speed The speed to set the turtle to
     *              <p>
     *              1 = 500ms
     *              <p>
     *              2 = 400ms
     *              <p>
     *              3 = 300ms
     *              <p>
     *              4 = 200ms
     *              <p>
     *              5 = 100ms
     */
    public void setSpeed(int speed) {
        if (speed < 1 || speed > 5) {
            throw new RuntimeException("Speed must be between 1 and 5");
        }
        this.actionDelay = 600 - speed * 100;
    }

    /**
     * @param block The block to placed by the turtle
     */
    public void useBlock(Block block) {
        placingBlock = block;
    }

    /**
     * @param direction The relative direction to turn the turtle
     */
    public void turn(MSRelDir.Direction direction) {
        timeout();
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(turtlePos);
        buf.writeInt(direction.ordinal());

        ClientPlayNetworking.send(MineScriptPackets.TURN_RELDIR_ID, buf);
    }

    /**
     * @param direction The absolute direction to turn the turtle
     */
    public void turn(MSAbsDir.Direction direction) {
        timeout();
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(turtlePos);
        buf.writeInt(direction.ordinal());

        ClientPlayNetworking.send(MineScriptPackets.TURN_ABSDIR_ID, buf);
    }

    /**
     * @param msg The message to print
     * @param type The type of message to print (ERROR, WARNING, INFO)
     */
    public void print(String msg, MSMessageType type) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(msg);
        buf.writeInt(type.ordinal());

        ClientPlayNetworking.send(MineScriptPackets.PRINT_ID, buf);

    }

    /**
     * @param program The program to run (from turtle terminal)
     */
    public void startInterpreter(String program) {
        interpreterThread = new Thread(new Interpreter(program, this));
        interpreterThread.start();
    }

    private void timeout() {
        if (Thread.currentThread() == interpreterThread) {
            try {
                Thread.sleep(actionDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public int getXPosition() {
        return turtlePos.getX();
    }

    public int getYPosition() {
        return turtlePos.getY();
    }

    public int getZPosition() {
        return turtlePos.getZ();
    }

    /**
     * @return The block in front of the turtle
     */
    public Block peek() {
        BlockState state = world.getBlockState(turtlePos);

        if (!state.contains(TurtleBlock.FACE) || !state.contains(Properties.HORIZONTAL_FACING))
            throw new RuntimeException("Property does not exist");

        BlockPos peekPos = null;
        if (state.get(TurtleBlock.FACE) == WallMountLocation.WALL) {
            switch (state.get(Properties.HORIZONTAL_FACING)) {
                case NORTH -> peekPos = turtlePos.north(1);
                case SOUTH -> peekPos = turtlePos.south(1);
                case EAST -> peekPos = turtlePos.east(1);
                case WEST -> peekPos = turtlePos.west(1);
            }
        } else {
            switch (state.get(TurtleBlock.FACE)) {
                case FLOOR -> peekPos = turtlePos.down(1);
                case CEILING -> peekPos = turtlePos.up(1);
            }
        }

        return world.getBlockState(peekPos).getBlock();
    }


    /**
     * @return The horizontal direction the turtle is facing
     */
    public String getHorizontalDirection() {
        BlockState state = world.getBlockState(turtlePos);
        if (state.contains(Properties.HORIZONTAL_FACING)) {
            return state.get(Properties.HORIZONTAL_FACING).toString();
        }
        return "north";
    }

    /**
     * @return The vertical direction the turtle is facing
     *        <p>
     *            If the turtle is facing a wall, it will return the direction the turtle is facing horizontally
     */
    public String getVerticalDirection() {
        BlockState state = world.getBlockState(turtlePos);
        if (state.contains(TurtleBlock.FACE)) {
            switch (state.get(TurtleBlock.FACE)) {
                case FLOOR:
                    return "bottom";
                case CEILING:
                    return "top";
            }
        }
        if (state.contains(Properties.HORIZONTAL_FACING)) {
            return state.get(Properties.HORIZONTAL_FACING).toString();
        }
        return "north";
    }

    /**
     * @param pos The position to move the turtle to
     */
    public void setPosition(BlockPos pos) {
        timeout();

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(turtlePos);
        buf.writeBlockPos(pos);

        ClientPlayNetworking.send(MineScriptPackets.SET_ID, buf);

        BlockState state = world.getBlockState(turtlePos);

        world.removeBlock(turtlePos, false);
        world.setBlockState(pos, state, Block.NOTIFY_ALL);

        turtlePos = pos;
    }
}
