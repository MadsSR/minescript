package minescript.screen;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.util.Identifier;

public class TextEditorScreen extends BaseUIModelScreen<FlowLayout> {
    public TextEditorScreen() {
        super(FlowLayout.class, DataSource.file("../src/main/resources/assets/minescript/owo_ui/editor.xml"));
        //super(FlowLayout.class, DataSource.asset(new Identifier("minescript", "editor")));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        var editor = rootComponent.childById(EditBoxWidget.class, "edit-box");
        editor.active = true;

        editor.mouseDown().subscribe((a,b,c) -> {
            editor.setFocused(true);
            return true;
        });
    }
}
