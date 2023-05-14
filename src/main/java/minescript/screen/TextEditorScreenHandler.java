package minescript.screen;

import minescript.MineScript;
import minescript.block.entity.TurtleBlockEntity;
import minescript.network.MineScriptPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import org.jetbrains.annotations.Nullable;

public class TextEditorScreenHandler extends ScreenHandler {
    private String inputText;
    private final @Nullable TurtleBlockEntity entity;

    public TextEditorScreenHandler(int syncId, PlayerInventory inventory, PacketByteBuf buf) {
        this(syncId, inventory, (TurtleBlockEntity) null);
        inputText = buf.readString();
    }

    public TextEditorScreenHandler(int syncId, PlayerInventory inventory, @Nullable TurtleBlockEntity entity) {
        super(MineScript.TEXT_EDITOR_SCREEN_HANDLER, syncId);

        inputText = "placeholder";

        this.entity = entity;
        this.addServerboundMessage(StartInterpreterMessage.class, this::handleSetInputText);
    }

    public String getInputText() {
        return inputText;
    }

    private void handleSetInputText(StartInterpreterMessage message) {
        this.inputText = message.message;
        assert entity != null;
        entity.setTurtleInput(message.message);

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(entity.getPos());
        buf.writeString(message.message);

        ClientPlayNetworking.send(MineScriptPackets.START_INTERPRETER_ID, buf);
    }

    public record StartInterpreterMessage(String message) {}

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
