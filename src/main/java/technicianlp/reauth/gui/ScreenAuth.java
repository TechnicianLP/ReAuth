package technicianlp.reauth.gui;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.VersionChecker;
import org.lwjgl.glfw.GLFW;
import technicianlp.reauth.AuthHelper;
import technicianlp.reauth.ReAuth;

import java.awt.Color;

public final class ScreenAuth extends Screen {

    private TextFieldWidget username;
    private PasswordFieldWidget pw;
    private Button confirm;
    private Button cancel;
    private CheckboxButton save;
    private Button config;

    private Screen prev;

    private int baseY;

    private TranslationTextComponent message = new TranslationTextComponent("");

    public ScreenAuth(Screen prev) {
        super(new TranslationTextComponent("reauth.gui.auth.title"));
        this.prev = prev;
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        getMinecraft().keyboardListener.enableRepeatEvents(true);

        this.baseY = this.field_230709_l_ / 2 - 110 / 2;

        this.username = new TextFieldWidget(this.field_230712_o_, this.field_230708_k_ / 2 - 155, this.baseY + 15, 2 * 155, 20, new TranslationTextComponent("reauth.gui.auth.username"));
        this.username.setMaxStringLength(512);
        this.username.setText(ReAuth.config.getUsername());
        func_230480_a_(username);

        this.pw = new PasswordFieldWidget(this.field_230712_o_, this.field_230708_k_ / 2 - 155, this.baseY + 60, 2 * 155, 20, new TranslationTextComponent("reauth.gui.auth.password"));
        this.pw.setMaxStringLength(Short.MAX_VALUE);
        this.pw.setText(ReAuth.config.getPassword());
        func_230480_a_(this.pw);

        focus(username.getText().isEmpty() ? username : pw);

        this.save = new CheckboxButton(this.field_230708_k_ / 2 - 155, this.baseY + 85, 2 * 155, 20, new TranslationTextComponent("reauth.gui.auth.checkbox"), !pw.getText().isEmpty());
        if (ReAuth.config.hasCrypto()) {
            func_230480_a_(this.save);
        }

        this.confirm = new Button(this.field_230708_k_ / 2 - 155, this.baseY + 110, 153, 20, new TranslationTextComponent("gui.confirm"), (b) -> doLogin());
        func_230480_a_(confirm);
        this.cancel = new Button(this.field_230708_k_ / 2 + 2, this.baseY + 110, 155, 20, new TranslationTextComponent("gui.cancel"), (b) -> this.getMinecraft().displayGuiScreen(prev));
        func_230480_a_(cancel);

        this.config = new Button(this.field_230708_k_ - 80, this.field_230709_l_ - 25, 75, 20, new TranslationTextComponent("reauth.gui.auth.config"), (b) -> {
//            this.getMinecraft().displayGuiScreen(new ConfigGUI(this));
            ReAuth.config.setCredentials("", "", "");
            func_231164_f_();
        });
//        addButton(config);

        VersionChecker.CheckResult result = VersionChecker.getResult(ReAuth.modInfo);
        if (result.status == VersionChecker.Status.OUTDATED || result.status == VersionChecker.Status.BETA_OUTDATED) {
            // Cannot be null but is marked as such :(
            @SuppressWarnings("ConstantConditions")
            String msg = result.changes.get(result.target);
            if (msg != null)
                message = new TranslationTextComponent("reauth.gui.auth.update", msg);
        }
    }

    @Override
    public void func_230430_a_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.func_230446_a_(matrixStack);

        this.func_238472_a_(matrixStack, this.field_230712_o_, new TranslationTextComponent("reauth.gui.auth.text1"), this.field_230708_k_ / 2, this.baseY, Color.WHITE.getRGB());
        this.func_238472_a_(matrixStack, this.field_230712_o_, new TranslationTextComponent("reauth.gui.auth.text2"), this.field_230708_k_ / 2, this.baseY + 45, Color.WHITE.getRGB());
        if (!this.message.getKey().equals("")) {
            this.func_238472_a_(matrixStack, this.field_230712_o_, this.message, this.field_230708_k_ / 2, this.baseY - 15, 0xFFFFFF);
        }

        if (!ReAuth.config.hasCrypto()) {
            this.func_238475_b_(matrixStack, this.field_230712_o_, new TranslationTextComponent("reauth.gui.auth.noCrypto"), this.field_230708_k_ / 2 - 155, this.baseY + 90, Color.WHITE.getRGB());
        }

        LoginType status = getLoginType();
        this.confirm.func_238482_a_(new TranslationTextComponent(status.getTranslation()));
        this.confirm.field_230693_o_ = status.isActive();

        super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
    }

    /**
     * Sets the focus to the given TextFieldWidget
     */
    private void focus(TextFieldWidget widget) {
        IGuiEventListener old = func_241217_q_();
        if (old instanceof TextFieldWidget)
            ((TextFieldWidget) old).setFocused2(false);
        if (widget != null)
            widget.setFocused2(true);
        setFocusedDefault(widget);
    }

    @Override
    public boolean func_231046_a_(int keyCode, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            IGuiEventListener focus = func_241217_q_();
            if (focus == username) {
                focus(pw);
                return true;
            } else if (focus == pw) {
                doLogin();
                return true;
            }
        }
        return super.func_231046_a_(keyCode, p_keyPressed_2_, p_keyPressed_3_);
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
                    ReAuth.auth.login(this.username.getText(), this.pw.getPW(), this.save.isChecked());
                    break;
                case Offline:
                    ReAuth.auth.offline(this.username.getText());
                    break;
                default:
                    return;
            }
            this.message = new TranslationTextComponent("reauth.login.success");
            success = true;
        } catch (AuthenticationException e) {
            this.message = new TranslationTextComponent("reauth.login.fail", e.getMessage());
            ReAuth.log.error("Login failed:", e);
        } catch (Exception e) {
            this.message = new TranslationTextComponent("reauth.login.error", e.getMessage());
            ReAuth.log.error("Error:", e);
        }
        if (success)
            func_231164_f_();
    }

    /**
     * Method called to request this Screen to close itself (unfortunate MCP name)
     */
    @Override
    public void func_231175_as__() {
        this.getMinecraft().displayGuiScreen(prev);
    }

    /**
     * Called once this Screen is closed (unfortunate MCP name)
     */
    @Override
    public void func_231164_f_() {
        super.func_231164_f_();
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
}
