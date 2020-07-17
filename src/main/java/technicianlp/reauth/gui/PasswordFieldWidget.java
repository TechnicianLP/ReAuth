package technicianlp.reauth.gui;

import net.minecraft.SharedConstants;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import technicianlp.reauth.mixin.TextFieldWidgetMixin;

import java.util.Arrays;

final class PasswordFieldWidget extends TextFieldWidget {

    PasswordFieldWidget(TextRenderer renderer, int posx, int posy, int x, int y, Text name) {
        super(renderer, posx, posy, x, y, name);
        this.setMaxLength(512);
    }

    private char[] password = new char[0];

    /**
     * Prevent Cut/Copy; actual logic handled by super
     */
    @Override
    public final boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.isActive() || Screen.isCopy(keyCode) || Screen.isCut(keyCode))
            return false;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * Vanilla filters out "§" therefore a custom filter is use (see {@link #isValidChar(char)}) to allow those
     */
    @Override
    public boolean charTyped(char chr, int keyCode) {
        if (!this.isActive()) {
            return false;
        } else if (isValidChar(chr)) {
            this.write(Character.toString(chr));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Modified version of {@link TextFieldWidget#write(String)} to allow for displayed text to differ and make the password be array based
     */
    @Override
    public final void write(String rawInput) {
        int selectionEnd = getMixin().reauthGetSelectionEnd();
        int selStart = Math.min(this.getCursor(), selectionEnd);
        int selEnd = Math.max(this.getCursor(), selectionEnd);
        int selLength = selEnd - selStart;

        char[] input = stripInvalidChars(rawInput);
        char[] newPW = new char[password.length - selLength + input.length];

        if (password.length != 0 && selStart > 0)
            System.arraycopy(password, 0, newPW, 0, Math.min(selStart, password.length));

        System.arraycopy(input, 0, newPW, selStart, input.length);

        if (password.length != 0 && selEnd < password.length)
            System.arraycopy(password, selEnd, newPW, selStart + input.length, password.length - selEnd);

        setPassword(newPW);
    }

    /**
     * Modified version of {@link TextFieldWidget#eraseCharacters(int)} to allow for displayed text to differ and make the password be array based
     */
    @Override
    public final void eraseCharacters(int characterOffset) {
        if (password.length == 0)
            return;
        if (this.getMixin().reauthGetSelectionEnd() != this.getCursor()) {
            this.write("");
        } else {
            int cursor = Util.moveCursor(this.getText(), this.getCursor(), characterOffset);
            int start = Math.min(cursor, this.getCursor());
            int end = Math.max(cursor, this.getCursor());

            if(start != end) {
                char[] newPW = new char[start + password.length - end];

                if (start >= 0)
                    System.arraycopy(password, 0, newPW, 0, start);

                if (end < password.length)
                    System.arraycopy(password, end, newPW, start, password.length - end);

                setPassword(newPW);
                this.setCursor(start);
            }
        }
    }

    final char[] getPassword() {
        char[] pw = new char[password.length];
        System.arraycopy(password, 0, pw, 0, password.length);
        return pw;
    }

    /**
     * clear old password and update displayed Text
     */
    final void setPassword(char[] password) {
        Arrays.fill(this.password, 'f');
        this.password = password;
        updateText();
    }

    /**
     * Redirect Setter to {@link #setPassword(char[])}
     */
    @Override
    public final void setText(String textIn) {
        setPassword(textIn.toCharArray());
        updateText();
    }

    /**
     * Sets the actually displayed Text to all dots
     */
    private void updateText() {
        char[] chars = new char[password.length];
        Arrays.fill(chars, '\u25CF');
        super.setText(new String(chars));
    }

    /**
     * Modified version of {@link SharedConstants#stripInvalidChars(String)} to allow SectionSign to be input into the field
     */
    private char[] stripInvalidChars(String input) {
        char[] out = new char[input.length()];
        int outInd = 0;
        for (int i = 0; i < out.length; i++) {
            char in = input.charAt(i);
            if (isValidChar(in)) {
                out[outInd++] = in;
            }
        }
        char[] ret = new char[outInd];
        System.arraycopy(out, 0, ret, 0, outInd);
        Arrays.fill(out, 'f');
        return ret;
    }

    /**
     * Modified version of {@link SharedConstants#isValidChar(char)} to allow SectionSign to be input into the field
     */
    private boolean isValidChar(char in) {
        return in == 0xa7 || SharedConstants.isValidChar(in);
    }

    @SuppressWarnings("ConstantConditions")
    private TextFieldWidgetMixin getMixin() {
        return (TextFieldWidgetMixin)(Object) this;
    }
}
