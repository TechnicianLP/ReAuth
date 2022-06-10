package technicianlp.reauth.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;
import technicianlp.reauth.session.SessionHelper;

import java.io.IOException;

public final class OfflineLoginScreen extends AbstractScreen {

    private GuiTextField username;
    private GuiButton confirm;

    public OfflineLoginScreen(GuiScreen background) {
        super("reauth.gui.title.offline", background);
    }

    @Override
    public final void initGui() {
        super.initGui();

        this.username = new GuiTextField(2, this.fontRenderer, this.centerX - BUTTON_WIDTH / 2, this.centerY - 5, BUTTON_WIDTH, 20);
        this.username.setMaxStringLength(16);
        this.username.setFocused(true);


        this.confirm = new GuiButton(3, this.centerX - BUTTON_WIDTH / 2, this.baseY + this.screenHeight - 42, BUTTON_WIDTH, 20, I18n.format("reauth.gui.button.username"));
        this.addButton(this.confirm);
    }

    @Override
    public final void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        this.fontRenderer.drawStringWithShadow(I18n.format("reauth.gui.auth.username"), this.centerX - (BUTTON_WIDTH / 2f), this.centerY - 15, 0xFFFFFFFF);

        this.username.drawTextBox();
    }

    @Override
    public final void updateScreen() {
        super.updateScreen();
        this.confirm.enabled = SessionHelper.isValidOfflineUsername(this.username.getText());
    }

    @Override
    public final void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        this.username.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            this.performUsernameChange();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button.id == 3) {
            this.performUsernameChange();
        }
    }

    /**
     * Calls the to do the Login and handles Errors
     * Closes the Screen if successful
     */
    private void performUsernameChange() {
        if (SessionHelper.isValidOfflineUsername(this.username.getText())) {
            SessionHelper.setOfflineUsername(this.username.getText());
            this.requestClose(true);
        }
    }
}
