package minescript.screen;

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

public class TextEditorScreen extends BaseUIModelScreen<FlowLayout> {
    private TurtleBlockEntity turtleBlockEntity;

    public TextEditorScreen(BlockEntity blockEntity) {
//        super(FlowLayout.class, DataSource.file("../src/main/resources/assets/minescript/owo_ui/editor.xml"));
        super(FlowLayout.class, DataSource.asset(new Identifier("minescript", "editor")));

        if (blockEntity instanceof TurtleBlockEntity turtle) {
            turtleBlockEntity = (TurtleBlockEntity) blockEntity;
        }
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        var editor = rootComponent.childById(EditBoxWidget.class, "edit-box");

        if (editor == null) throw new NullPointerException("editor is null");
        editor.active = true;
        if (turtleBlockEntity.input != null) editor.setText(turtleBlockEntity.input.getString());
        editor.mouseDown().subscribe((a,b,c) -> {
            editor.setFocused(true);

            try {
                Method m = ((Object)editor).getClass().getDeclaredMethod("moveCursor", double.class, double.class);
                m.setAccessible(true);
                m.invoke(editor, a + editor.getX(), b + editor.getY());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }

            return true;
        });

        var runButton = rootComponent.childById(ButtonComponent.class, "run-button");
        if (runButton == null) throw new NullPointerException("runButton is null");
        runButton.onPress(button -> {
            turtleBlockEntity.input = Text.of(editor.getText());
            turtleBlockEntity.markDirty();
            turtleBlockEntity.startInterpreter(editor.getText());
            MinecraftClient.getInstance().setScreen(null);
        });
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

}
