package minescript.block.entity;

import minescript.item.inventory.ImplementedInventory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TurtleBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory {

    public TurtleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURTLE_BLOCK_ENTITY, pos, state);
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

    public void step(int steps) {
        if (!world.isClient()) {
            BlockState state = world.getBlockState(pos);
            BlockPos newPos = pos;

            switch (state.get(Properties.HORIZONTAL_FACING)) {
                case NORTH -> newPos = newPos.north(steps);
                case SOUTH -> newPos = newPos.south(steps);
                case EAST -> newPos = newPos.east(steps);
                case WEST -> newPos = newPos.west(steps);
            }

            world.setBlockState(newPos, state, Block.NOTIFY_ALL);
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
        }
    }
}
