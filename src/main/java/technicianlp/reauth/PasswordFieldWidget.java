package technicianlp.reauth;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.SharedConstants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.Arrays;

@OnlyIn(Dist.CLIENT)
final class PasswordFieldWidget extends TextFieldWidget {

    private static final Field selectionEnd = ObfuscationReflectionHelper.findField(TextFieldWidget.class, "field_146223_s");

    PasswordFieldWidget(FontRenderer renderer, int posx, int posy, int x, int y, String name) {
        super(renderer, posx, posy, x, y, name);
        this.setMaxStringLength(512);
    }

    private char[] password = new char[0];

    final char[] getPW() {
        char[] pw = new char[password.length];
        System.arraycopy(password, 0, pw, 0, password.length);
        return pw;
    }

//    @Override
//    public String getMessage() {
//        System.out.println(new String(password));
//        return super.getMessage();
//    }

    public final boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.isFocused() || Screen.isCopy(keyCode) || Screen.isCut(keyCode))
            return false; // Prevent Cut/Copy
        return super.keyPressed(keyCode, scanCode, modifiers); // combos handled by super
    }

    @Override
    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
        if (!this.func_212955_f()) {
            return false;
        } else if (isAllowedCharacter(p_charTyped_1_)) {
            this.writeText(Character.toString(p_charTyped_1_));
            return true;
        } else {
            return false;
        }
    }

    public final void writeText(String rawInput) {
        int selectionEnd = getSelectionEnd();
        int selStart = Math.min(this.getCursorPosition(), selectionEnd);
        int selEnd = Math.max(this.getCursorPosition(), selectionEnd);
        int selLength = selEnd - selStart;

        char[] input = filterAllowedCharacters(rawInput);
        char[] newPW = new char[password.length - selLength + input.length];

        if (password.length != 0 && selStart > 0)
            System.arraycopy(password, 0, newPW, 0, Math.min(selStart, password.length));

        System.arraycopy(input, 0, newPW, selStart, input.length);

        if (password.length != 0 && selEnd < password.length)
            System.arraycopy(password, selEnd, newPW, selStart + input.length, password.length - selEnd);

        setPassword(newPW);
        this.func_212422_f(selStart + input.length);
        this.setSelectionPos(getCursorPosition());
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

            if (direction)
                this.moveCursorBy(num);
            setPassword(newPW);
        }
    }

    final void setPassword(char[] password) {
        Arrays.fill(this.password, 'f');
        this.password = password;
        updateText();
    }

    public int getSelectionEnd() {
        try {
            return (int) selectionEnd.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed Reflective Access", e);
        }
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
     * Modified version of {@link SharedConstants#filterAllowedCharacters(String)} to allow SectionSign to be input into the field
     */
    private char[] filterAllowedCharacters(String input) {
        char[] out = new char[input.length()];
        int outInd = 0;
        for (int i = 0; i < out.length; i++) {
            char in = input.charAt(i);
            if (isAllowedCharacter(in)) {
                out[outInd++] = in;
            }
        }
        char[] ret = new char[outInd];
        System.arraycopy(out, 0, ret, 0, outInd);
        Arrays.fill(out, 'f');
        return ret;
    }

    /**
     * Modified version of {@link SharedConstants#isAllowedCharacter(char)} to allow SectionSign to be input into the field
     */
    private boolean isAllowedCharacter(char in) {
        return in == 0xa7 || SharedConstants.isAllowedCharacter(in);
    }
}
