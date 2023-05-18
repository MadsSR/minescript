package minescript.block.entity;

import minescript.screen.TextEditorScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TurtleBlockEntity extends SyncedBlockEntity implements ExtendedScreenHandlerFactory {

    public BlockPos turtlePos;
    public Thread interpreterThread;
    private Text input;

    public TurtleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURTLE_BLOCK_ENTITY, pos, state);
        turtlePos = pos;
        input = Text.empty();
    }

    public void setTurtleInput(String input) {
        this.input = Text.of(input);
        this.markDirty();
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        String text = input.getString();
        int length = text.length();

        int splits = (int) Math.ceil((double) length / 50000);
        for (int i = 0; i < splits; i++) {
            if (i == splits - 1)
                nbt.putString("input" + i, text.substring(i * 50000));
            else
                nbt.putString("input" + i, text.substring(i * 50000, (i + 1) * 50000));
        }

        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        StringBuilder text = new StringBuilder();
        int split = 0;
        while (nbt.contains("input" + split)) {
            text.append(nbt.getString("input" + split));
            split++;
        }
        this.input = Text.of(text.toString());
        super.readNbt(nbt);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        String text = this.input.getString();
        buf.writeInt(text.length());
        buf.writeString(text, text.length());
    }

    @Override
    public Text getDisplayName() {
        return Text.empty();
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new TextEditorScreenHandler(syncId, playerInventory, this);
    }

    public TurtleBlockEntity getTurtleEntity() {
        if (world.getBlockEntity(turtlePos) instanceof TurtleBlockEntity turtleEntity) {
            turtleEntity.interpreterThread = interpreterThread;
            turtleEntity.input = input;
            turtleEntity.turtlePos = turtlePos;
            return turtleEntity;
        }
        return null;
    }

    public static void tick(World world, BlockPos pos, BlockState state, TurtleBlockEntity entity) {
    }
}
