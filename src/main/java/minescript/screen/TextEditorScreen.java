package minescript.screen;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import minescript.block.entity.TurtleBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.widget.EditBoxWidget;

public class TextEditorScreen extends BaseUIModelScreen<FlowLayout> {

    private BlockEntity blockEntity;
    public TextEditorScreen(BlockEntity blockEntity) {
        super(FlowLayout.class, DataSource.file("../src/main/resources/assets/minescript/owo_ui/editor.xml"));
        //super(FlowLayout.class, DataSource.asset(new Identifier("minescript", "editor")));
        this.blockEntity = blockEntity;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        var editor = rootComponent.childById(EditBoxWidget.class, "edit-box");

        if (editor == null) throw new NullPointerException("editor is null");
        editor.active = true;
        editor.mouseDown().subscribe((a,b,c) -> {
            editor.setFocused(true);

            return true;
        });

        var runButton = rootComponent.childById(ButtonComponent.class, "run-button");
        if (runButton == null) throw new NullPointerException("runButton is null");
        runButton.onPress(button -> {
            if (blockEntity instanceof TurtleBlockEntity turtleBlockEntity) {
                turtleBlockEntity.step(Integer.parseInt(editor.getText()));
            }
        });
    }
}
