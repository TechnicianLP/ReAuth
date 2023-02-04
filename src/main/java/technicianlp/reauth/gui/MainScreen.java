package technicianlp.reauth.gui;

import java.util.Map;

import org.apache.maven.artifact.versioning.ComparableVersion;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.VersionChecker;
import technicianlp.reauth.ReAuth;
import technicianlp.reauth.authentication.flows.Flows;
import technicianlp.reauth.configuration.Profile;
import technicianlp.reauth.configuration.ProfileConstants;

public final class MainScreen extends AbstractScreen {

    private String message = null;

    public MainScreen() {
        super("reauth.gui.title.main");
    }

    @Override
    public void init() {
        super.init();

        int buttonWidthH = BUTTON_WIDTH / 2;
        int y = this.centerY - 55;

        SaveButton.ITooltip saveButtonTooltip = (button, matrixStack, mouseX, mouseY) -> this.renderTooltip(matrixStack, this.font.split(Component.translatable("reauth.gui.button.save.tooltip"), 250), mouseX, mouseY);
        SaveButton saveButton = new SaveButton(this.centerX - buttonWidthH, y + 70, Component.translatable("reauth.gui.button.save"), saveButtonTooltip);
        this.addRenderableWidget(saveButton);

        Profile profile = ReAuth.profiles.getProfile();
        if (profile != null) {
            Component text = Component.translatable("reauth.gui.profile", profile.getValue(ProfileConstants.NAME, "Steve"));
            this.addRenderableWidget(ReAuth.button(this.centerX - buttonWidthH, y + 10, BUTTON_WIDTH, 20, text, (b) -> FlowScreen.open(Flows::loginWithProfile, profile, false)));
        } else {
            Button profileButton = ReAuth.button(this.centerX - buttonWidthH, y + 10, BUTTON_WIDTH, 20, Component.translatable("reauth.gui.noProfile"), (b) -> {
            });
            profileButton.active = false;
            this.addRenderableWidget(profileButton);
        }

        this.addRenderableWidget(ReAuth.button(this.centerX - buttonWidthH, y + 45, buttonWidthH - 1, 20, Component.translatable("reauth.gui.button.authcode"), (b) -> FlowScreen.open(Flows::loginWithAuthCode, saveButton.selected(), false)));
        this.addRenderableWidget(ReAuth.button(this.centerX + 1, y + 45, buttonWidthH - 1, 20, Component.translatable("reauth.gui.button.devicecode"), (b) -> FlowScreen.open(Flows::loginWithDeviceCode, saveButton.selected(), false)));
        this.addRenderableWidget(ReAuth.button(this.centerX - buttonWidthH, y + 105, BUTTON_WIDTH, 20, Component.translatable("reauth.gui.button.offline"), (b) -> this.transitionScreen(new OfflineLoginScreen())));


        VersionChecker.CheckResult result = VersionChecker.getResult(ReAuth.modInfo);
        if (result.status() == VersionChecker.Status.OUTDATED) {
            // Cannot be null but is marked as such :(
            Map<ComparableVersion, String> changes = result.changes();
            if (changes != null) {
                String msg = changes.get(result.target());
                if (msg != null) {
                    this.message = I18n.get("reauth.gui.auth.update", msg);
                }
            }
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);

        int x = this.centerX - BUTTON_WIDTH / 2;
        this.font.drawShadow(poseStack, I18n.get("reauth.gui.text.profile"), x, this.centerY - 55, 0xFFFFFFFF);
        this.font.drawShadow(poseStack, I18n.get("reauth.gui.text.microsoft"), x, this.centerY - 20, 0xFFFFFFFF);
        this.font.drawShadow(poseStack, I18n.get("reauth.gui.text.offline"), x, this.centerY + 40, 0xFFFFFFFF);

        if (this.message != null) {
            this.font.drawShadow(poseStack, this.message, x, this.baseY + 20, 0xFFFFFFFF);
        }
    }

    @Override
    protected void requestClose(boolean completely) {
        super.requestClose(true);
    }
}
