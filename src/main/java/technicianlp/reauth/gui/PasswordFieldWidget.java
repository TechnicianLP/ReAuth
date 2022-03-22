package technicianlp.reauth.gui;//package technicianlp.reauth.gui;
//
//import net.minecraft.client.gui.FontRenderer;
//import net.minecraft.client.gui.screen.Screen;
//import net.minecraft.client.gui.widget.TextFieldWidget;
//import net.minecraft.util.SharedConstants;
//import net.minecraft.util.Util;
//import net.minecraft.util.text.ITextComponent;
//import technicianlp.reauth.util.ReflectionHelper;
//
//import java.lang.reflect.Field;
//import java.util.Arrays;
//
//final class PasswordFieldWidget extends TextFieldWidget {
//
//    private static final Field selectionEnd = ReflectionHelper.findMcpField(TextFieldWidget.class, "field_146223_s");
//
//    PasswordFieldWidget(FontRenderer renderer, int posx, int posy, int x, int y, ITextComponent name) {
//        super(renderer, posx, posy, x, y, name);
//        this.setMaxStringLength(512);
//    }
//
//    private char[] password = new char[0];
//
//    /**
//     * Prevent Cut/Copy; actual logic handled by super
//     */
//    @Override
//    public final boolean keyPressed(int keyCode, int scanCode, int modifiers) {
//        if (!this.isFocused() || Screen.isCopy(keyCode) || Screen.isCut(keyCode))
//            return false;
//        return super.keyPressed(keyCode, scanCode, modifiers);
//    }
//
//    /**
//     * Vanilla filters out "§" therefore a custom filter is used (see {@link #isAllowedCharacter(char)}) to allow those
//     */
//    @Override
//    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
//        if (!this.canWrite()) {
//            return false;
//        } else if (this.isAllowedCharacter(p_charTyped_1_)) {
//            this.writeText(Character.toString(p_charTyped_1_));
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    /**
//     * Modified version of {@link TextFieldWidget#writeText(String)} to allow for displayed text to differ and make the password be array based
//     */
//    @Override
//    public final void writeText(String rawInput) {
//        int selectionEnd = this.getSelectionEnd();
//        int selStart = Math.min(this.getCursorPosition(), selectionEnd);
//        int selEnd = Math.max(this.getCursorPosition(), selectionEnd);
//        int selLength = selEnd - selStart;
//
//        char[] input = this.filterAllowedCharacters(rawInput);
//        char[] newPW = new char[this.password.length - selLength + input.length];
//
//        if (this.password.length != 0 && selStart > 0)
//            System.arraycopy(this.password, 0, newPW, 0, Math.min(selStart, this.password.length));
//
//        System.arraycopy(input, 0, newPW, selStart, input.length);
//
//        if (this.password.length != 0 && selEnd < this.password.length)
//            System.arraycopy(this.password, selEnd, newPW, selStart + input.length, this.password.length - selEnd);
//
//        this.setPassword(newPW);
//    }
//
//    /**
//     * Modified version of {@link TextFieldWidget#deleteFromCursor(int)} to allow for displayed text to differ and make the password be array based
//     */
//    @Override
//    public final void deleteFromCursor(int characterOffset) {
//        if (this.password.length == 0)
//            return;
//        if (this.getSelectionEnd() != this.getCursorPosition()) {
//            this.writeText("");
//        } else {
//            int cursor = Util.func_240980_a_(this.getText(), this.getCursorPosition(), characterOffset);
//            int start = Math.min(cursor, this.getCursorPosition());
//            int end = Math.max(cursor, this.getCursorPosition());
//
//            if (start != end) {
//                char[] newPW = new char[start + this.password.length - end];
//
//                if (start >= 0)
//                    System.arraycopy(this.password, 0, newPW, 0, start);
//
//                if (end < this.password.length)
//                    System.arraycopy(this.password, end, newPW, start, this.password.length - end);
//
//                this.setPassword(newPW);
//                this.setCursorPosition(start);
//            }
//        }
//    }
//
//    /**
//     * clear old password and update displayed Text
//     */
//    final void setPassword(char[] password) {
//        Arrays.fill(this.password, 'f');
//        this.password = password;
//        this.updateText();
//    }
//
//    final char[] getPassword() {
//        char[] pw = new char[this.password.length];
//        System.arraycopy(this.password, 0, pw, 0, this.password.length);
//        return pw;
//    }
//
//    /**
//     * Redirect Setter to {@link #setPassword(char[])}
//     */
//    @Override
//    public final void setText(String textIn) {
//        this.setPassword(textIn.toCharArray());
//        this.updateText();
//    }
//
//    /**
//     * Sets the actually displayed Text to all dots
//     */
//    private void updateText() {
//        char[] chars = new char[this.password.length];
//        Arrays.fill(chars, '\u25CF');
//        super.setText(new String(chars));
//    }
//
//    /**
//     * Modified version of {@link SharedConstants#filterAllowedCharacters(String)} to allow SectionSign to be input into the field
//     */
//    private char[] filterAllowedCharacters(String input) {
//        char[] out = new char[input.length()];
//        int outInd = 0;
//        for (int i = 0; i < out.length; i++) {
//            char in = input.charAt(i);
//            if (this.isAllowedCharacter(in)) {
//                out[outInd++] = in;
//            }
//        }
//        char[] ret = new char[outInd];
//        System.arraycopy(out, 0, ret, 0, outInd);
//        Arrays.fill(out, 'f');
//        return ret;
//    }
//
//    /**
//     * Modified version of {@link SharedConstants#isAllowedCharacter(char)} to allow SectionSign to be input into the field
//     */
//    private boolean isAllowedCharacter(char in) {
//        return in == 0xa7 || SharedConstants.isAllowedCharacter(in);
//    }
//
//    /**
//     * Getter due to the field being private in super
//     */
//    public int getSelectionEnd() {
//        return ReflectionHelper.getField(selectionEnd, this);
//    }
//}
