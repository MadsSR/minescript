package minescript.screen;

import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.inject.GreedyInputComponent;
import minescript.mixin.ui.access.EditBoxWidgetAccessor;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.EditBox;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.input.CursorMovement;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class TextEditor extends EditBoxWidget implements GreedyInputComponent {
    protected final EditBox editBox;
    protected final int tabSize = 4;

    public TextEditor(TextRenderer textRenderer, int x, int y, int width, int height, Text placeholder, Text message) {
        super(textRenderer, x, y, width, height, placeholder, message);
        this.editBox = ((EditBoxWidgetAccessor) this).getEditBox();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean result = super.keyPressed(keyCode, scanCode, modifiers);

        if (keyCode == GLFW.GLFW_KEY_TAB && modifiers == 0) { // Indent on TAB
            StringBuilder indentText = new StringBuilder();
            String input = this.editBox.getSelectedText();
            String[] lines = input.split("\n");

            for (int i = 0; i < lines.length; i++) {
                indentText.append(" ".repeat(tabSize)).append(lines[i]);
                if (i != lines.length - 1) {
                    indentText.append("\n");
                }
            }

            if (input.endsWith("\n")) {
                indentText.append("\n");
            }

            this.editBox.setSelecting(true);
            this.editBox.replaceSelection(indentText.toString());
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_TAB && modifiers == GLFW.GLFW_MOD_SHIFT) { // Remove indent on SHIFT+TAB
            int cursorPos = this.editBox.getCursor();
            int change = 0;
            int charsBefore = 0;
            int relCursorPos;

            StringBuilder resText = new StringBuilder();
            String text = this.editBox.getText();
            String[] textLines = text.split("\n");
            String currentLine = textLines[this.editBox.getCurrentLineIndex()];

            for (int i = 0; i < this.editBox.getCurrentLineIndex(); i++) {
                charsBefore += textLines[i].length() + 1;
            }

            relCursorPos = cursorPos - charsBefore;

            for (int i = 0; i < tabSize && relCursorPos > 0; i++) {
                if (currentLine.charAt(relCursorPos-1) == ' ') {
                    currentLine = currentLine.substring(0, relCursorPos-1) + currentLine.substring(relCursorPos);
                    relCursorPos--;
                    change++;
                }
                else {
                    break;
                }
            }

            textLines[this.editBox.getCurrentLineIndex()] = currentLine;

            for (int i = 0; i < textLines.length; i++) {
                if (i != textLines.length - 1) {
                    resText.append(textLines[i]).append("\n");
                }
                else {
                    resText.append(textLines[i]);

                }
            }

            if (text.endsWith("\n")) {
                resText.append("\n");
            }

            this.editBox.setText(resText.toString());
            this.editBox.setSelecting(false);
            this.editBox.moveCursor(CursorMovement.ABSOLUTE, cursorPos-change);
            return true;
        }
        else{
            return result;
        }
    }

    @Override
    public void update(float delta, int mouseX, int mouseY) {
        super.update(delta, mouseX, mouseY);
        this.cursorStyle(this.overflows() && mouseX >= this.getX() + this.width - 9 ? CursorStyle.NONE : CursorStyle.TEXT);
    }

}
