package minescript.screen;

import interpreter.Interpreter;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import minescript.block.entity.TurtleBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TextEditorScreen extends BaseUIModelScreen<FlowLayout> {
    private TurtleBlockEntity turtleBlockEntity;

    public TextEditorScreen(BlockEntity blockEntity) {
        super(FlowLayout.class, DataSource.file("../src/main/resources/assets/minescript/owo_ui/editor.xml"));
        //super(FlowLayout.class, DataSource.asset(new Identifier("minescript", "editor")));

        if (blockEntity instanceof TurtleBlockEntity turtle) {
            turtleBlockEntity = (TurtleBlockEntity) blockEntity;
        }
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        var editor = rootComponent.childById(EditBoxWidget.class, "edit-box");

        if (editor == null) throw new NullPointerException("editor is null");
        editor.active = true;
        if (turtleBlockEntity.input != null) editor.setText(turtleBlockEntity.input);
        editor.mouseDown().subscribe((a,b,c) -> {
            editor.setFocused(true);

            return true;
        });

        var runButton = rootComponent.childById(ButtonComponent.class, "run-button");
        if (runButton == null) throw new NullPointerException("runButton is null");
        runButton.onPress(button -> {
            turtleBlockEntity.input = editor.getText();
            turtleBlockEntity.startInterpreter(editor.getText());
            MinecraftClient.getInstance().setScreen(null);
        });
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

}
