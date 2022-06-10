package technicianlp.reauth.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;
import technicianlp.reauth.session.SessionHelper;

public final class OfflineLoginScreen extends AbstractScreen {

    private TextFieldWidget username;
    private Button confirm;

    public OfflineLoginScreen() {
        super("reauth.gui.title.offline");
    }

    @Override
    public final void init() {
        super.init();
        this.getMinecraft().keyboardListener.enableRepeatEvents(true);

        this.username = new TextFieldWidget(this.font, this.centerX - BUTTON_WIDTH / 2, this.centerY - 5, BUTTON_WIDTH, 20, new TranslationTextComponent("reauth.gui.auth.username"));
        this.username.setMaxStringLength(16);
        this.addButton(this.username);
        this.username.setFocused2(true);
        this.setListener(this.username);

        this.confirm = new Button(this.centerX - BUTTON_WIDTH / 2, this.baseY + this.screenHeight - 42, BUTTON_WIDTH, 20, new TranslationTextComponent("reauth.gui.button.username"), (b) -> this.performUsernameChange());
        this.addButton(this.confirm);
    }

    @Override
    public final void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        super.render(matrices, mouseX, mouseY, partialTicks);

        this.font.drawStringWithShadow(matrices, I18n.format("reauth.gui.auth.username"), this.centerX - (BUTTON_WIDTH / 2f), this.centerY - 15, 0xFFFFFFFF);
    }

    @Override
    public final void tick() {
        super.tick();
        this.confirm.active = SessionHelper.isValidOfflineUsername(this.username.getText());
    }

    @Override
    public final boolean keyPressed(int keyCode, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            IGuiEventListener focus = this.getListener();
            if (focus == this.username) {
                this.performUsernameChange();
                return true;
            }
        }
        return super.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
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
