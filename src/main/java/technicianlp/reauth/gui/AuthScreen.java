package technicianlp.reauth.gui;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.VersionChecker;
import org.lwjgl.glfw.GLFW;
import technicianlp.reauth.AuthHelper;
import technicianlp.reauth.ReAuth;

import java.awt.Color;

public final class AuthScreen extends Screen {

    private TextFieldWidget username;
    private PasswordFieldWidget pw;
    private Button confirm;
    private Button cancel;
    private CheckboxButton save;
    private Button config;

    private Screen prev;

    private int baseY;

    private String message = "";

    public AuthScreen(Screen prev) {
        super(new TranslationTextComponent("reauth.gui.auth.title"));
        this.prev = prev;
    }

    public AuthScreen(Screen prev, String message) {
        this(prev);
        this.message = message;
    }

    @Override
    public void init() {
        super.init();
        getMinecraft().keyboardListener.enableRepeatEvents(true);

        this.baseY = this.height / 2 - 110 / 2;

        this.username = new TextFieldWidget(this.font, this.width / 2 - 155, this.baseY + 15, 2 * 155, 20, new TranslationTextComponent("reauth.gui.auth.username"));
        this.username.setMaxStringLength(512);
        this.username.setText(ReAuth.config.getUsername());
        addButton(username);

        this.pw = new PasswordFieldWidget(this.font, this.width / 2 - 155, this.baseY + 60, 2 * 155, 20, new TranslationTextComponent("reauth.gui.auth.password"));
        this.pw.setMaxStringLength(Short.MAX_VALUE);
        this.pw.setText(ReAuth.config.getPassword());
        addButton(this.pw);

        focus(username.getText().isEmpty() ? username : pw);

        this.save = new CheckboxButton(this.width / 2 - 155, this.baseY + 85, 2 * 155, 20, new TranslationTextComponent("reauth.gui.auth.checkbox"), !pw.getText().isEmpty());
        if (ReAuth.config.hasCrypto()) {
            addButton(this.save);
        }

        this.confirm = new Button(this.width / 2 - 155, this.baseY + 110, 153, 20, StringTextComponent.EMPTY, (b) -> doLogin());
        addButton(confirm);
        this.cancel = new Button(this.width / 2 + 2, this.baseY + 110, 155, 20, new TranslationTextComponent("gui.cancel"), (b) -> this.getMinecraft().displayGuiScreen(prev));
        addButton(cancel);

        this.config = new Button(this.width - 80, this.height - 25, 75, 20, new TranslationTextComponent("reauth.gui.auth.config"), (b) -> {
//            this.getMinecraft().displayGuiScreen(new ConfigGUI(this));
            ReAuth.config.setCredentials("", "", "");
            onClose();
        });
//        addButton(config);

        VersionChecker.CheckResult result = VersionChecker.getResult(ReAuth.modInfo);
        if (message.isEmpty() && result.status == VersionChecker.Status.OUTDATED) {
            // Cannot be null but is marked as such :(
            @SuppressWarnings("ConstantConditions")
            String msg = result.changes.get(result.target);
            if (msg != null)
                message = I18n.format("reauth.gui.auth.update", msg);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrices);

        drawCenteredString2(matrices, this.font, I18n.format("reauth.gui.auth.text1"), this.width / 2, this.baseY, Color.WHITE.getRGB());
        drawCenteredString2(matrices, this.font, I18n.format("reauth.gui.auth.text2"), this.width / 2, this.baseY + 45, Color.WHITE.getRGB());
        if (!this.message.isEmpty()) {
            drawCenteredString2(matrices, this.font, this.message, this.width / 2, this.baseY - 15, 0xFFFFFF);
        }

        if (!ReAuth.config.hasCrypto()) {
            this.font.drawStringWithShadow(matrices, I18n.format("reauth.gui.auth.noCrypto"), this.width / 2 - 155, this.baseY + 90, Color.WHITE.getRGB());
        }

        LoginType status = getLoginType();
        this.confirm.setMessage(new TranslationTextComponent(status.getTranslation()));
        this.confirm.active = status.isActive();

        super.render(matrices, mouseX, mouseY, partialTicks);
    }

    /**
     * Sets the focus to the given TextFieldWidget
     */
    private void focus(TextFieldWidget widget) {
        IGuiEventListener old = getFocused();
        if (old instanceof TextFieldWidget)
            ((TextFieldWidget) old).setFocused2(false);
        if (widget != null)
            widget.setFocused2(true);
        setFocused(widget);
    }

    @Override
    public boolean keyPressed(int keyCode, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            IGuiEventListener focus = getFocused();
            if (focus == username) {
                focus(pw);
                return true;
            } else if (focus == pw) {
                doLogin();
                return true;
            }
        }
        return super.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
    }

    /**
     * Determines which {@link LoginType} is applicable<br>
     * - {@link LoginType#Online} requires a Password<br>
     * - {@link LoginType#Offline} needs the name to match the Regex for valid Minecraft Names
     */
    private LoginType getLoginType() {
        String user = this.username.getText();
        if (user.isEmpty()) {
            return LoginType.None;
        } else if (this.pw.getText().isEmpty()) {
            if (ReAuth.auth.isValidName(user)) {
                return LoginType.Offline;
            } else {
                return LoginType.None;
            }
        } else {
            return LoginType.Online;
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
                case Online:
                    ReAuth.auth.login(this.username.getText(), this.pw.getPassword(), this.save.isChecked());
                    break;
                case Offline:
                    ReAuth.auth.offline(this.username.getText());
                    break;
                default:
                    return;
            }
            this.message = I18n.format("reauth.login.success");
            success = true;
        } catch (AuthenticationException e) {
            this.message = I18n.format("reauth.login.fail", e.getMessage());
            ReAuth.log.error("Login failed:", e);
        } catch (Exception e) {
            this.message = I18n.format("reauth.login.error", e.getMessage());
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
        this.getMinecraft().displayGuiScreen(prev);
    }

    /**
     * Called once this Screen is closed (unfortunate MCP name)
     */
    @Override
    public void removed() {
        super.removed();
        this.pw.setPassword(new char[0]);
        getMinecraft().keyboardListener.enableRepeatEvents(false);
    }

    private enum LoginType {
        None(false, "none"),
        Online(true, "online"),
        Offline(true, "offline");

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
    private void drawCenteredString2(MatrixStack matrix, FontRenderer font, String text, int x, int y, int color) {
        font.drawStringWithShadow(matrix, text, (float) (x - font.getStringWidth(text) / 2), (float) y, color);
    }
}
