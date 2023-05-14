package minescript.screen;

import io.wispforest.owo.ui.inject.GreedyInputComponent;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.text.Text;

public class TextEditor extends EditBoxWidget implements GreedyInputComponent {
    public TextEditor(TextRenderer textRenderer, int x, int y, int width, int height, Text placeholder, Text message) {
        super(textRenderer, x, y, width, height, placeholder, message);
    }
}
