package technicianlp.reauth.gui;

import com.mojang.authlib.exceptions.AuthenticationException;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.lwjgl.glfw.GLFW;
import technicianlp.reauth.AuthHelper;
import technicianlp.reauth.ReAuth;
import technicianlp.reauth.VersionChecker;
import technicianlp.reauth.integration.ClothConfigIntegration;

import java.awt.Color;

public final class AuthScreen extends Screen {

    private TextFieldWidget username;
    private PasswordFieldWidget pw;
    private ButtonWidget confirm;
    private ButtonWidget cancel;
    private CheckboxWidget save;
    private ButtonWidget config;

    private Screen prev;

    private int baseY;

    private String message = "";

    public AuthScreen(Screen prev) {
        super(new TranslatableText("reauth.gui.auth.title"));
        this.prev = prev;
    }

    public AuthScreen(Screen prev, String message) {
        this(prev);
        this.message = message;
    }

    @Override
    public void init() {
        super.init();
        this.client.keyboard.setRepeatEvents(true);

        this.baseY = this.height / 2 - 110 / 2;

        this.username = new TextFieldWidget(this.textRenderer, this.width / 2 - 155, this.baseY + 15, 2 * 155, 20, new TranslatableText("reauth.gui.auth.username"));
        this.username.setMaxLength(512);
        this.username.setText(ReAuth.config.getUsername());
        addButton(username);

        this.pw = new PasswordFieldWidget(this.textRenderer, this.width / 2 - 155, this.baseY + 60, 2 * 155, 20, new TranslatableText("reauth.gui.auth.password"));
        this.pw.setMaxLength(Short.MAX_VALUE);
        this.pw.setText(ReAuth.config.getPassword());
        addButton(this.pw);

        setInitialFocus(username.getText().isEmpty() ? username : pw);

        this.save = new CheckboxWidget(this.width / 2 - 155, this.baseY + 85, 2 * 155, 20, new TranslatableText("reauth.gui.auth.checkbox"), !pw.getText().isEmpty());
        if (ReAuth.config.hasCrypto()) {
            addButton(this.save);
        }

        this.confirm = new ButtonWidget(this.width / 2 - 155, this.baseY + 110, 153, 20, LiteralText.EMPTY, b -> doLogin());
        addButton(confirm);
        this.cancel = new ButtonWidget(this.width / 2 + 2, this.baseY + 110, 155, 20, new TranslatableText("gui.cancel"), b -> this.client.openScreen(prev));
        addButton(cancel);

        this.config = new ButtonWidget(this.width - 80, this.height - 25, 75, 20, new TranslatableText("reauth.gui.auth.config"), b -> {
            this.client.openScreen(ClothConfigIntegration.getConfigScreen(this));
        });
        if (ClothConfigIntegration.isAvailable()) {
            addButton(config);
        }

        if (message.isEmpty() && ReAuth.versionCheck.getStatus() == VersionChecker.Status.OUTDATED) {
            String msg = ReAuth.versionCheck.getChanges();
            if (msg != null) {
                message = I18n.translate("reauth.gui.auth.update", msg);
            }
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrices);

        drawCenteredString2(matrices, this.textRenderer, I18n.translate("reauth.gui.auth.text1"), this.width / 2, this.baseY, Color.WHITE.getRGB());
        drawCenteredString2(matrices, this.textRenderer, I18n.translate("reauth.gui.auth.text2"), this.width / 2, this.baseY + 45, Color.WHITE.getRGB());
        if (!this.message.isEmpty()) {
            drawCenteredString2(matrices, this.textRenderer, this.message, this.width / 2, this.baseY - 15, 0xFFFFFF);
        }

        if (!ReAuth.config.hasCrypto()) {
            this.textRenderer.draw(matrices, I18n.translate("reauth.gui.auth.noCrypto"), this.width / 2 - 155, this.baseY + 90, Color.WHITE.getRGB());
        }

        LoginType status = getLoginType();
        this.confirm.setMessage(new TranslatableText(status.getTranslation()));
        this.confirm.active = status.isActive();

        super.render(matrices, mouseX, mouseY, partialTicks);
    }

    /**
     * Sets the focus to the given TextFieldWidget
     */
    private void focus(TextFieldWidget widget) {
        Element old = getFocused();
        if (old instanceof TextFieldWidget)
            ((TextFieldWidget) old).setSelected(false);
        if (widget != null)
            widget.setSelected(true);
        focusOn(widget);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            Element focus = getFocused();
            if (focus == username) {
                focus(pw);
                return true;
            } else if (focus == pw) {
                doLogin();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * Determines which {@link LoginType} is applicable<br>
     * - {@link LoginType#ONLINE} requires a Password<br>
     * - {@link LoginType#OFFLINE} needs the name to match the Regex for valid Minecraft Names
     */
    private LoginType getLoginType() {
        String user = this.username.getText();
        if (user.isEmpty()) {
            return LoginType.NONE;
        } else if (this.pw.getText().isEmpty()) {
            if (ReAuth.auth.isValidName(user)) {
                return LoginType.OFFLINE;
            } else {
                return LoginType.NONE;
            }
        } else {
            return LoginType.ONLINE;
        }
    }

    /**
     * Calls the {@link AuthHelper} to do the Login and handles Errors
     * Closes the Screen if successful
     */
    private void doLogin() {
        boolean success = false;
        try {
            LoginType type = getLoginType();
            switch (type) {
                case ONLINE:
                    ReAuth.auth.login(this.username.getText(), this.pw.getPassword(), this.save.isChecked());
                    break;
                case OFFLINE:
                    ReAuth.auth.offline(this.username.getText());
                    break;
                default:
                    return;
            }
            this.message = I18n.translate("reauth.login.success");
            success = true;
        } catch (AuthenticationException e) {
            this.message = I18n.translate("reauth.login.fail", e.getMessage());
            ReAuth.log.error("Login failed:", e);
        } catch (Exception e) {
            this.message = I18n.translate("reauth.login.error", e.getMessage());
            ReAuth.log.error("Error:", e);
        }
        if (success)
            onClose();
    }

    /**
     * Method called to request this Screen to close itself (unfortunate MCP name)
     */
    @Override
    public void onClose() {
        this.client.openScreen(prev);
    }

    /**
     * Called once this Screen is closed (unfortunate MCP name)
     */
    @Override
    public void removed() {
        super.removed();
        this.pw.setPassword(new char[0]);
        this.client.keyboard.setRepeatEvents(false);
    }

    private enum LoginType {
        NONE(false, "none"),
        ONLINE(true, "online"),
        OFFLINE(true, "offline");

        private final boolean active;
        private final String translation;

        LoginType(boolean active, String translation) {
            this.active = active;
            this.translation = translation;
        }

        public boolean isActive() {
            return active;
        }

        public String getTranslation() {
            return "reauth.gui.auth.confirm." + translation;
        }
    }

    /**
     * for 1.16.x compat this needs to be copied
     * the superclass method changes from instance to static between 1.16.1 and 1.16.2
     */
    private void drawCenteredString2(MatrixStack matrices, TextRenderer textRenderer, String text, int centerX, int y, int color) {
        textRenderer.drawWithShadow(matrices, text, (float)(centerX - textRenderer.getWidth(text) / 2), (float)y, color);
    }
}
