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
    private boolean isClientTextUpdated;
    private final @Nullable TurtleBlockEntity entity;

    public TextEditorScreenHandler(int syncId, PlayerInventory inventory, PacketByteBuf buf) {
        this(syncId, inventory, (TurtleBlockEntity) null);
        isClientTextUpdated = false;
    }

    public TextEditorScreenHandler(int syncId, PlayerInventory inventory, @Nullable TurtleBlockEntity entity) {
        super(MineScript.TEXT_EDITOR_SCREEN_HANDLER, syncId);

        inputText = "placeholder";

        this.entity = entity;
        this.addClientboundMessage(SendInputToClientMessage.class, this::handleSendInputToClient);
        this.addServerboundMessage(StartInterpreterMessage.class, this::handleSetInputText);
        this.addServerboundMessage(TriggerSendInputMessage.class, this::handleTriggerSendInput);
    }

    private void handleTriggerSendInput(TriggerSendInputMessage message) {
        assert entity != null;
        String text = entity.getTurtleInput();
        int length = text.length();

        int splits = (int) Math.ceil((double) length / 50000);
        PacketByteBuf buf = PacketByteBufs.create();

        int splitLength;

        for (int i = 0; i < splits; i++) {
            if (i == splits - 1) {
                splitLength = text.substring(i * 50000).length();
                buf.writeString(text.substring(i * 50000), splitLength);
                this.sendMessage(new SendInputToClientMessage(i, splits, splitLength, buf));
            }
            else {
                String substring = text.substring(i * 50000, (i + 1) * 50000);
                buf.writeString(substring, substring.length());
                splitLength = substring.length();
                this.sendMessage(new SendInputToClientMessage(i, splits, splitLength, buf));
            }
        }
    }

    private void handleSendInputToClient(SendInputToClientMessage message) {
        int splits = message.totalSplits;
        int currentSplit = message.current;
        int splitLength = message.splitLength;
        PacketByteBuf buf = message.buf;

        if (currentSplit == 0)
            this.inputText = buf.readString(splitLength);
        else
            this.inputText += buf.readString(splitLength);

        // if last split then set clientTextUpdated to true
        if (currentSplit == splits - 1)
            isClientTextUpdated = true;
    }

    private void handleSetInputText(StartInterpreterMessage message) {
        int length = message.length;
        String text = message.buf.readString(length);
        this.inputText = text;
        assert entity != null;
        entity.setTurtleInput(text);

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(entity.getPos());
        buf.writeInt(length);
        buf.writeString(text, text.length());

        ClientPlayNetworking.send(MineScriptPackets.START_INTERPRETER_ID, buf);
    }

    public String getInputText() {
        return inputText;
    }
    public boolean isClientTextUpdated() { return isClientTextUpdated; }

    public record TriggerSendInputMessage() {}
    public record SendInputToClientMessage(int current, int totalSplits, int splitLength, PacketByteBuf buf) {}
    public record StartInterpreterMessage(int length, PacketByteBuf buf) {}

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
