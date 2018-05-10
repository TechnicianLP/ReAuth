package technicianlp.reauth;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;

@SideOnly(Side.CLIENT)
final class GuiPasswordField extends GuiTextField {

    GuiPasswordField(FontRenderer renderer, int posx, int posy, int x, int y) {
        super(1, renderer, posx, posy, x, y);
        this.setMaxStringLength(512);
    }

    private char[] password = new char[0];

    final char[] getPW() {
        char[] pw = new char[password.length];
        System.arraycopy(password,0,pw,0,password.length);
        return pw;
    }

    public final boolean textboxKeyTyped(char typedChar, int keyCode) {
        if (!this.isFocused() || GuiScreen.isKeyComboCtrlC(keyCode) || GuiScreen.isKeyComboCtrlX(keyCode))
            return false; // Prevent Cut/Copy
        if (GuiScreen.isKeyComboCtrlA(keyCode) || GuiScreen.isKeyComboCtrlV(keyCode))
            return super.textboxKeyTyped(typedChar, keyCode); // combos handled by super

        switch (keyCode) {
            case Keyboard.KEY_BACK: // backspace
            case Keyboard.KEY_DELETE:
            case Keyboard.KEY_HOME: // jump keys?
            case Keyboard.KEY_END:
            case Keyboard.KEY_LEFT: // arrowkey
            case Keyboard.KEY_RIGHT:
                return super.textboxKeyTyped(typedChar, keyCode); // special keys handled by super
            default:
                if (isAllowedCharacter(typedChar)) {
                    this.writeText(Character.toString(typedChar));
                    return true;
                }
                return false;
        }
    }

    public final void writeText(String rawInput) {
        int selStart = this.getCursorPosition() < this.getSelectionEnd() ? this.getCursorPosition() : this.getSelectionEnd();
        int selEnd = this.getCursorPosition() < this.getSelectionEnd() ? this.getSelectionEnd() : this.getCursorPosition();

        char[] input = filterAllowedCharacters(rawInput).toCharArray();
        char[] newPW = new char[selStart + password.length - selEnd + input.length];

        if (password.length != 0 && selStart > 0)
            System.arraycopy(password, 0, newPW, 0, Math.min(selStart, password.length));

        System.arraycopy(input, 0, newPW, selStart, input.length);
        int l = input.length;


        if (password.length != 0 && selEnd < password.length)
            System.arraycopy(password, selEnd, newPW, selStart + input.length, password.length - selEnd);

        setPassword(newPW);
        Arrays.fill(newPW, 'f');
        this.moveCursorBy(selStart - this.getSelectionEnd() + l);
    }

    @Override
    public final void deleteFromCursor(int num) {
        if (password.length == 0)
            return;
        if (this.getSelectionEnd() != this.getCursorPosition()) {
            this.writeText("");
        } else {
            boolean direction = num < 0;
            int start = direction ? Math.max(this.getCursorPosition() + num, 0) : this.getCursorPosition();
            int end = direction ? this.getCursorPosition() : Math.min(this.getCursorPosition() + num, password.length);

            char[] newPW = new char[start + password.length - end];


            if (start >= 0)
                System.arraycopy(password, 0, newPW, 0, start);

            if (end < password.length)
                System.arraycopy(password, end, newPW, start, password.length - end);

            setPassword(newPW);
            Arrays.fill(newPW,'f');
            if (direction)
                this.moveCursorBy(num);
        }
    }

    final void setPassword(char[] password) {
        Arrays.fill(this.password, 'f');
        this.password = new char[password.length];
        System.arraycopy(password, 0, this.password, 0, password.length);
        updateText();
    }

    @Override
    public final void setText(String textIn) {
        setPassword(textIn.toCharArray());
        updateText();
    }

    private void updateText() {
        char[] chars = new char[password.length];
        Arrays.fill(chars, '\u25CF');
        super.setText(new String(chars));
    }

    /**
     * Allow SectionSign to be input into the field
     */
    private boolean isAllowedCharacter(int character) {
        return character == 0xa7 || ChatAllowedCharacters.isAllowedCharacter((char) character);
    }

    /**
     * Modified version of {@link ChatAllowedCharacters#filterAllowedCharacters(String)}
     */
    private String filterAllowedCharacters(String input) {
        StringBuilder stringbuilder = new StringBuilder();
        input.chars().filter(this::isAllowedCharacter).forEach(i -> stringbuilder.append((char) i));
        return stringbuilder.toString();
    }
}
