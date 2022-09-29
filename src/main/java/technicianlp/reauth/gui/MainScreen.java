package technicianlp.reauth.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import technicianlp.reauth.ReAuth;
import technicianlp.reauth.VersionChecker;
import technicianlp.reauth.authentication.flows.Flows;
import technicianlp.reauth.configuration.Profile;
import technicianlp.reauth.configuration.ProfileConstants;

public final class MainScreen extends AbstractScreen {

    private String message;

    public MainScreen(Screen parent) {
        super("reauth.gui.title.main", parent);
    }

    @Override
    public void init() {
        super.init();

        int buttonWidthH = BUTTON_WIDTH / 2;
        int y = this.centerY - 55;

        SaveButton.ITooltip saveButtonTooltip =
            (button, matrixStack, mouseX, mouseY) -> this.renderOrderedTooltip(matrixStack,
                this.textRenderer.wrapLines(Text.translatable("reauth.gui.button.save.tooltip"), 250),
                mouseX, mouseY);
        SaveButton saveButton = new SaveButton(this.centerX - buttonWidthH, y + 70,
            Text.translatable("reauth.gui.button.save"), saveButtonTooltip);
        this.addDrawableChild(saveButton);

        Profile profile = ReAuth.profiles.getProfile();
        if (profile != null) {
            Text text = Text.translatable("reauth.gui.profile", profile.getValue(ProfileConstants.NAME, "Steve"));
            this.addDrawableChild(new ButtonWidget(this.centerX - buttonWidthH, y + 10, BUTTON_WIDTH, 20, text,
                button -> FlowScreen.open(Flows::loginWithProfile, profile)));
        } else {
            ButtonWidget profileButton = new ButtonWidget(this.centerX - buttonWidthH, y + 10, BUTTON_WIDTH, 20,
                Text.translatable("reauth.gui.noProfile"), button -> {
            });
            profileButton.active = false;
            this.addDrawableChild(profileButton);
        }

        this.addDrawableChild(new ButtonWidget(this.centerX - buttonWidthH, y + 45, buttonWidthH - 1, 20,
            Text.translatable("reauth.gui.button.authcode"), button -> FlowScreen.open(Flows::loginWithAuthCode,
            saveButton.isChecked())));
        this.addDrawableChild(new ButtonWidget(this.centerX + 1, y + 45, buttonWidthH - 1, 20, Text.translatable(
            "reauth.gui.button.devicecode"), button -> FlowScreen.open(Flows::loginWithDeviceCode,
            saveButton.isChecked())));
        this.addDrawableChild(new ButtonWidget(this.centerX - buttonWidthH, y + 105, BUTTON_WIDTH, 20,
            Text.translatable("reauth.gui.button.offline"),
            button -> this.transitionScreen(new OfflineLoginScreen())));


        VersionChecker.Status result = ReAuth.versionCheck.getStatus();
        if (result == VersionChecker.Status.OUTDATED) {
            // Cannot be null but is marked as such :(
            String changes = ReAuth.versionCheck.getChanges();
            if (changes != null) {
                this.message = I18n.translate("reauth.gui.auth.update", changes);
            }
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        int x = this.centerX - BUTTON_WIDTH / 2;
        this.textRenderer.drawWithShadow(matrices, I18n.translate("reauth.gui.text.profile"),
            x, this.centerY - 55, 0xA0A0A0);
        this.textRenderer.drawWithShadow(matrices, I18n.translate("reauth.gui.text.microsoft"),
            x, this.centerY - 20, 0xA0A0A0);
        this.textRenderer.drawWithShadow(matrices, I18n.translate("reauth.gui.text.offline"),
            x, this.centerY + 40, 0xA0A0A0);

        if (this.message != null) {
            this.textRenderer.drawWithShadow(matrices, this.message, x, this.baseY + 20, 0xFFFFFFFF);
        }
    }

    @Override
    protected void requestClose(boolean completely) {
        super.requestClose(true);
    }
}
