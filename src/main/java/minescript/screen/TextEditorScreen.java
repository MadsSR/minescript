package minescript.screen;

import io.netty.buffer.ByteBuf;
import io.wispforest.owo.client.screens.ScreenInternals;
import io.wispforest.owo.ui.base.BaseUIModelHandledScreen;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TextEditorScreen extends BaseUIModelHandledScreen<FlowLayout, TextEditorScreenHandler> {

    public TextEditorScreen(TextEditorScreenHandler handler, PlayerInventory inventory, Text title) {
//        super(handler, inventory, title, FlowLayout.class, BaseUIModelScreen.DataSource.file("../src/main/resources/assets/minescript/owo_ui/editor.xml")); // For development
        super(handler, inventory, title, FlowLayout.class, BaseUIModelScreen.DataSource.asset(new Identifier("minescript", "editor"))); // For release
        this.playerInventoryTitleY = 70000;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        var editor = rootComponent.childById(EditBoxWidget.class, "edit-box");

        if (editor == null) throw new NullPointerException("editor is null");
        editor.active = true;
        editor.setText(handler.getInputText());

        editor.mouseDown().subscribe((mouseX, mouseY, button) -> {
            editor.setFocused(true);

            // Moves the cursor to the mouse position
            try {
                // Method m = ((Object) editor).getClass().getDeclaredMethod("moveCursor", double.class, double.class); // For development
                Method m = ((Object) editor).getClass().getSuperclass().getDeclaredMethod("method_44404", double.class, double.class); // For release
                m.setAccessible(true);
                m.invoke(editor, mouseX + editor.getX(), mouseY + editor.getY());
            } catch (Exception e) {
                // Loops through all methods until one works (this is used because the method name is obfuscated)
                var methods = ((Object) editor).getClass().getSuperclass().getDeclaredMethods();
                for (Method method : methods) {
                    try {
                        method.setAccessible(true);
                        method.invoke(editor, mouseX + editor.getX(), mouseY + editor.getY());
                        break;
                    } catch (Exception ignored) {
                    }
                }
            }
            return true;
        });

        var runButton = rootComponent.childById(ButtonComponent.class, "run-button");
        if (runButton == null) throw new NullPointerException("runButton is null");
        runButton.onPress(button -> {
            String text = editor.getText();
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(text, text.length());

            handler.sendMessage(new TextEditorScreenHandler.StartInterpreterMessage(text.length(), buf));
            this.close();
        });
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
