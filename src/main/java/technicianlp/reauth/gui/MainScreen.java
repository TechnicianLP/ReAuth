package technicianlp.reauth.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.VersionChecker;
import technicianlp.reauth.ReAuth;
import technicianlp.reauth.authentication.flows.Flows;
import technicianlp.reauth.configuration.Profile;

public final class MainScreen extends AbstractScreen {

    private String message = null;

    public MainScreen() {
        super("reauth.gui.title.main");
    }

    @Override
    public final void init() {
        super.init();
        this.getMinecraft().keyboardListener.enableRepeatEvents(true);

        int buttonWidthH = BUTTON_WIDTH / 2;
        int y = this.centerY - 55;

        SaveButton.ITooltip saveButtonTooltip = (button, matrixStack, mouseX, mouseY) -> this.renderTooltip(matrixStack, this.font.trimStringToWidth(new TranslationTextComponent("reauth.gui.button.save.tooltip"), 250), mouseX, mouseY);
        SaveButton saveButton = new SaveButton(this.centerX - buttonWidthH, y + 70, new TranslationTextComponent("reauth.gui.button.save"), saveButtonTooltip);
        this.addButton(saveButton);

        Profile profile = ReAuth.profiles.getProfile();
        if (profile != null) {
            ITextComponent text = new TranslationTextComponent("reauth.gui.profile", profile.getValue(Profile.NAME, "Steve"));
            this.addButton(new Button(this.centerX - buttonWidthH, y + 10, BUTTON_WIDTH, 20, text, (b) -> FlowScreen.open(Flows::loginWithProfile, profile, false)));
        } else {
            Button profileButton = new Button(this.centerX - buttonWidthH, y + 10, BUTTON_WIDTH, 20, new TranslationTextComponent("reauth.gui.noProfile"), (b) -> {
            });
            profileButton.active = false;
            this.addButton(profileButton);
        }

        this.addButton(new Button(this.centerX - buttonWidthH, y + 45, buttonWidthH - 1, 20, new TranslationTextComponent("reauth.gui.button.authcode"), (b) -> FlowScreen.open(Flows::loginWithAuthCode, saveButton.isChecked(), false)));
        this.addButton(new Button(this.centerX + 1, y + 45, buttonWidthH - 1, 20, new TranslationTextComponent("reauth.gui.button.devicecode"), (b) -> FlowScreen.open(Flows::loginWithDeviceCode, saveButton.isChecked(), false)));
        this.addButton(new Button(this.centerX - buttonWidthH, y + 105, BUTTON_WIDTH, 20, new TranslationTextComponent("reauth.gui.button.offline"), (b) -> this.transitionScreen(new OfflineLoginScreen())));

        VersionChecker.CheckResult result = VersionChecker.getResult(ReAuth.modInfo);
        if (result.status == VersionChecker.Status.OUTDATED) {
            // Cannot be null but is marked as such :(
            @SuppressWarnings("ConstantConditions")
            String msg = result.changes.get(result.target);
            if (msg != null) {
                this.message = I18n.format("reauth.gui.auth.update", msg);
            }
        }
    }

    @Override
    public final void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        super.render(matrices, mouseX, mouseY, partialTicks);

        int x = this.centerX - BUTTON_WIDTH / 2;
        this.font.drawStringWithShadow(matrices, I18n.format("reauth.gui.text.profile"), x, this.centerY - 55, 0xFFFFFFFF);
        this.font.drawStringWithShadow(matrices, I18n.format("reauth.gui.text.microsoft"), x, this.centerY - 20, 0xFFFFFFFF);
        this.font.drawStringWithShadow(matrices, I18n.format("reauth.gui.text.offline"), x, this.centerY + 40, 0xFFFFFFFF);

        if (this.message != null) {
            this.font.drawStringWithShadow(matrices, this.message, x, this.baseY + 20, 0xFFFFFFFF);
        }
    }

    @Override
    protected final void requestClose(boolean completely) {
        this.getMinecraft().popGuiLayer();
    }
}
