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
    public Text input;

    public TurtleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURTLE_BLOCK_ENTITY, pos, state);
        actionDelay = 500;
        turtlePos = pos;
        placingBlock = Blocks.AIR;
        input = Text.empty();
    }

//    @Override
//    public void writeNbt(NbtCompound tag) {
//        super.writeNbt(tag);
//        tag.putString("input", Text.Serializer.toJson(input));
//    }
//
//    @Override
//    public void readNbt(NbtCompound tag) {
//        super.readNbt(tag);
//        Text temp = Text.Serializer.fromJson(tag.getString("input"));
//        if (!temp.equals(Text.empty())) {
//            input = temp;
//        }
//    }

//    @Override
//    public Packet<ClientPlayPacketListener> toUpdatePacket() {
//        return BlockEntityUpdateS2CPacket.create(this);
//    }
//
//    @Override
//    public NbtCompound toInitialChunkDataNbt() {
//        return createNbt();
//    }

    public static void tick(World world, BlockPos pos, BlockState state, TurtleBlockEntity entity) {
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

            if (state.get(TurtleBlock.FACE) == WallMountLocation.WALL) {
                switch (state.get(Properties.HORIZONTAL_FACING)) {
                    case NORTH -> turtlePos = turtlePos.north(1);
                    case SOUTH -> turtlePos = turtlePos.south(1);
                    case EAST -> turtlePos = turtlePos.east(1);
                    case WEST -> turtlePos = turtlePos.west(1);
                }
            }
            else {
                switch (state.get(TurtleBlock.FACE)) {
                    case FLOOR -> turtlePos = turtlePos.down(1);
                    case CEILING -> turtlePos = turtlePos.up(1);
                }
            }
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

    public void turn(MSRelDir.Direction direction) {
        timeout();
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(turtlePos);
        buf.writeInt(direction.ordinal());

        ClientPlayNetworking.send(MineScriptPackets.TURN_RELDIR_ID, buf);
    }

    public void turn(MSAbsDir.Direction direction) {
        timeout();
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(turtlePos);
        buf.writeInt(direction.ordinal());

        ClientPlayNetworking.send(MineScriptPackets.TURN_ABSDIR_ID, buf);
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
