package technicianlp.reauth.gui;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import technicianlp.reauth.ReAuth;
import technicianlp.reauth.session.SessionHelper;

public final class OfflineLoginScreen extends AbstractScreen {

    private EditBox username;
    private Button confirm;

    public OfflineLoginScreen() {
        super("reauth.gui.title.offline");
    }

    @Override
    public void init() {
        super.init();

        this.username = new EditBox(this.font, this.centerX - BUTTON_WIDTH / 2, this.centerY - 5, BUTTON_WIDTH, 20, Component.translatable("reauth.gui.auth.username"));
        this.username.setMaxLength(16);
        this.addRenderableWidget(this.username);
        this.username.setFocus(true);
        this.setFocused(this.username);
        this.addRenderableWidget(this.username);

        this.confirm = ReAuth.button(this.centerX - BUTTON_WIDTH / 2, this.baseY + this.screenHeight - 42, BUTTON_WIDTH, 20, Component.translatable("reauth.gui.button.username"), (b) -> this.performUsernameChange());
        this.addRenderableWidget(this.confirm);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);

        this.font.drawShadow(poseStack, I18n.get("reauth.gui.auth.username"), this.centerX - (BUTTON_WIDTH / 2f), this.centerY - 15, 0xFFFFFFFF);
    }

    @Override
    public void tick() {
        super.tick();
        this.confirm.active = SessionHelper.isValidOfflineUsername(this.username.getValue());
    }

    @Override
    public boolean keyPressed(int keyCode, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            GuiEventListener focus = this.getFocused();
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
        if (SessionHelper.isValidOfflineUsername(this.username.getValue())) {
            SessionHelper.setOfflineUsername(this.username.getValue());
            this.requestClose(true);
        }
    }
}
