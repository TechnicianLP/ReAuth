package technicianlp.reauth;

import com.mojang.authlib.exceptions.AuthenticationException;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.VersionChecker;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;

final class ScreenAuth extends Screen {

    private TextFieldWidget username;
    private PasswordFieldWidget pw;
    private Button confirm;
    private Button cancel;
    private CheckboxButton save;
    private Button config;

    private Screen prev;

    private int baseY;

    private String message = "";

    ScreenAuth(Screen prev) {
        super(new TranslationTextComponent("reauth.gui.auth.title"));
        this.prev = prev;
    }

    @Override
    public void init() {
        super.init();
        getMinecraft().keyboardListener.enableRepeatEvents(true);

        this.baseY = this.height / 2 - 110 / 2;

        this.username = new TextFieldWidget(this.font, this.width / 2 - 155, this.baseY + 15, 2 * 155, 20, I18n.format("reauth.gui.auth.username"));
        this.username.setMaxStringLength(512);
        this.username.setText(ReAuth.config.getUsername());
        addButton(username);

        this.pw = new PasswordFieldWidget(this.font, this.width / 2 - 155, this.baseY + 60, 2 * 155, 20, I18n.format("reauth.gui.auth.password"));
        this.pw.setMaxStringLength(Short.MAX_VALUE);
        this.pw.setText(ReAuth.config.getPassword());
        addButton(this.pw);

        focus(username.getText().isEmpty() ? username : pw);

        this.save = new CheckboxButton(this.width / 2 - 155, this.baseY + 85, 2 * 155, 20, I18n.format("reauth.gui.auth.checkbox"), !pw.getText().isEmpty());
        addButton(this.save);

        this.confirm = new Button(this.width / 2 - 155, this.baseY + 110, 153, 20, "", (b) -> doLogin());
        addButton(confirm);
        this.cancel = new Button(this.width / 2 + 2, this.baseY + 110, 155, 20, I18n.format("gui.cancel"), (b) -> this.getMinecraft().displayGuiScreen(prev));
        addButton(cancel);

        this.config = new Button(this.width - 80, this.height - 25, 75, 20, I18n.format("reauth.gui.auth.config"), (b) -> {
//            this.getMinecraft().displayGuiScreen(new ConfigGUI(this));
            ReAuth.config.setCredentials("", "");
            onClose();
        });
//        addButton(config);

        VersionChecker.CheckResult result = VersionChecker.getResult(ReAuth.modInfo);
        if (result.status == VersionChecker.Status.OUTDATED || result.status == VersionChecker.Status.BETA_OUTDATED) {
            // Cannot be null but is marked as such :(
            @SuppressWarnings("ConstantConditions")
            String msg = result.changes.get(result.target);
            if (msg != null)
                message = I18n.format("reauth.gui.auth.update", msg);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();

        this.drawCenteredString(this.font, I18n.format("reauth.gui.auth.text1"), this.width / 2, this.baseY, Color.WHITE.getRGB());
        this.drawCenteredString(this.font, I18n.format("reauth.gui.auth.text2"), this.width / 2, this.baseY + 45, Color.WHITE.getRGB());
        if (!this.message.isEmpty()) {
            this.drawCenteredString(this.font, this.message, this.width / 2, this.baseY - 15, 0xFFFFFF);
        }

        LoginType status = getLoginType();
        this.confirm.setMessage(I18n.format(status.getTranslation()));
        this.confirm.active = status.active;

        super.render(mouseX, mouseY, partialTicks);
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
        func_212928_a(widget);
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
            if (user.matches("[A-Za-z0-9_]{2,16}")) {
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
                    ReAuth.auth.login(this.username.getText(), this.pw.getPW(), this.save.func_212942_a());
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
//        this.pw.setPassword(new char[0]);
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
}
