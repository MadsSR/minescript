package minescript.block.entity;

import interpreter.Interpreter;
import interpreter.types.MSMessageType;
import minescript.item.inventory.ImplementedInventory;
import minescript.networking.MineScriptPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TurtleBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory {

    private Block placingBlock;
    private BlockPos turtlePos;
    private Thread interpreterThread;
    private int actionDelay;
    public String input;

    public TurtleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURTLE_BLOCK_ENTITY, pos, state);
        actionDelay = 500;
        turtlePos = pos;
        placingBlock = Blocks.AIR;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return null;
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("MineScript Turtle");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return null;
    }

    public static void tick(World world, BlockPos pos, BlockState state, TurtleBlockEntity entity) {
    }

    public TurtleBlockEntity getTurtleEntity() {
        if (world.getBlockEntity(turtlePos) instanceof TurtleBlockEntity turtleEntity) {
            turtleEntity.interpreterThread = interpreterThread;
            turtleEntity.actionDelay = actionDelay;
            turtleEntity.input = input;
            return turtleEntity;
        }
        return null;
    }

    public void step(int steps) {
        for (int i = 0; i < steps; i++) {
            timeout();
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(turtlePos);
            buf.writeInt(1);
            buf.writeInt(Block.getRawIdFromState(placingBlock.getDefaultState()));

            ClientPlayNetworking.send(MineScriptPackets.STEP_ID, buf);

            BlockState state = world.getBlockState(turtlePos);
            BlockPos newPos = turtlePos;

            switch (state.get(Properties.HORIZONTAL_FACING)) {
                case NORTH -> newPos = newPos.north(1);
                case SOUTH -> newPos = newPos.south(1);
                case EAST -> newPos = newPos.east(1);
                case WEST -> newPos = newPos.west(1);
            }

            world.setBlockState(newPos, state, Block.NOTIFY_ALL);
            world.setBlockState(turtlePos, placingBlock.getDefaultState(), Block.NOTIFY_ALL);
            turtlePos = newPos;
        }
    }

    public void setSpeed(int speed) {
        if (speed < 1 || speed > 5) {
            throw new RuntimeException("Speed must be between 1 and 5");
        }
        this.actionDelay = 600 - speed * 100;
    }

    public void useBlock(Block block) {
        placingBlock = block;
    }

    public void turn(int degrees) {
        timeout();
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(turtlePos);
        buf.writeInt(degrees);

        ClientPlayNetworking.send(MineScriptPackets.TURN_ID, buf);
    }

    public void print(String msg, MSMessageType type) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(msg);
        buf.writeInt(type.ordinal());

        ClientPlayNetworking.send(MineScriptPackets.PRINT_ID, buf);

    }

    public void startInterpreter(String program) {
        interpreterThread = new Thread(new Interpreter(program, this));
        interpreterThread.start();
    }

    private void stopInterpreterThread() {
        if (interpreterThread != null) {
            interpreterThread.stop();
        }
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
}
