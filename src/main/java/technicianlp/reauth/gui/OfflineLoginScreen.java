package technicianlp.reauth.gui;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import technicianlp.reauth.session.SessionHelper;

import java.util.Objects;

public final class OfflineLoginScreen extends AbstractScreen {

    private TextFieldWidget username;
    private ButtonWidget confirm;

    public OfflineLoginScreen() {
        super("reauth.gui.title.offline");
    }

    @Override
    public void init() {
        super.init();

        this.username = new TextFieldWidget(this.textRenderer, this.centerX - BUTTON_WIDTH / 2, this.centerY - 5,
                BUTTON_WIDTH, 20, Text.translatable("reauth.gui.auth.username"));
        this.username.setMaxLength(16);
        this.username.setTextFieldFocused(true);
        this.username.setText(Objects.requireNonNull(this.client).getSession().getUsername());
        this.addSelectableChild(this.username);
        this.setInitialFocus(this.username);

        this.confirm = new ButtonWidget(this.centerX - BUTTON_WIDTH / 2, this.baseY + this.screenHeight - 42,
                BUTTON_WIDTH, 20, Text.translatable("reauth.gui.button.username"), (b) -> this.performUsernameChange());
        this.addDrawableChild(this.confirm);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.username.render(matrixStack, mouseX, mouseY, partialTicks);
        this.textRenderer.drawWithShadow(matrixStack, I18n.translate("reauth.gui.auth.username"),
                this.centerX - (BUTTON_WIDTH / 2f), this.centerY - 15, 0xFFFFFFFF);
    }

    @Override
    public void tick() {
        super.tick();
        this.confirm.active = SessionHelper.isValidOfflineUsername(this.username.getText());
    }

    @Override
    public boolean keyPressed(int keyCode, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            Element focus = this.getFocused();
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
