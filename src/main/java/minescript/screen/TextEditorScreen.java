package minescript.screen;

import interpreter.types.MSMessageType;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import minescript.block.entity.TurtleBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class TextEditorScreen extends BaseUIModelScreen<FlowLayout> {
    private TurtleBlockEntity turtleBlockEntity;

    public TextEditorScreen(BlockEntity blockEntity) {
        // super(FlowLayout.class, DataSource.file("../src/main/resources/assets/minescript/owo_ui/editor.xml")); // For development
        super(FlowLayout.class, DataSource.asset(new Identifier("minescript", "editor"))); // For release

        if (blockEntity instanceof TurtleBlockEntity turtle) {
            turtleBlockEntity = turtle;
        }
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        var editor = rootComponent.childById(EditBoxWidget.class, "edit-box");

        if (editor == null) throw new NullPointerException("editor is null");
        editor.active = true;

        if (turtleBlockEntity.input != null) editor.setText(turtleBlockEntity.input.getString());

        editor.mouseDown().subscribe((mouseX, mouseY, button) -> {
            editor.setFocused(true);

            // Moves the cursor to the mouse position
            try {
                // Method m = ((Object) editor).getClass().getDeclaredMethod("moveCursor", double.class, double.class); // For development
                Method m = ((Object) editor).getClass().getDeclaredMethod("method_44404", double.class, double.class); // For release
                m.setAccessible(true);
                m.invoke(editor, mouseX + editor.getX(), mouseY + editor.getY());
            } catch (Exception e) {
                // Loops through all methods until one works (this is used because the method name is obfuscated)
                var methods = ((Object) editor).getClass().getDeclaredMethods();
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
            turtleBlockEntity.input = Text.of(editor.getText());
            turtleBlockEntity.startInterpreter(editor.getText());
            MinecraftClient.getInstance().setScreen(null);
        });
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

}
