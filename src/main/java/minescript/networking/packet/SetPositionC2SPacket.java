package minescript.networking.packet;

import interpreter.types.MSMessageType;
import minescript.block.custom.TurtleBlock;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;

public class SetPositionC2SPacket {
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {

        ServerWorld world = player.getWorld();

        BlockPos oldPos = buf.readBlockPos();
        BlockPos newPos = buf.readBlockPos();
        BlockState state = world.getBlockState(oldPos);

        if (!state.contains(TurtleBlock.FACE) || !state.contains(Properties.HORIZONTAL_FACING)) {
            PrintC2SPacket.print("Property does not exist", player, MSMessageType.ERROR);
            return;
        }

        world.removeBlock(oldPos, false);
        world.setBlockState(newPos, state, Block.NOTIFY_ALL);

    }
}
