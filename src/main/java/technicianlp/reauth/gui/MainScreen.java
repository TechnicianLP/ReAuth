package technicianlp.reauth.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import technicianlp.reauth.ReAuth;
import technicianlp.reauth.authentication.flows.Flows;
import technicianlp.reauth.configuration.Profile;
import technicianlp.reauth.configuration.ProfileConstants;

import java.io.IOException;

public final class MainScreen extends AbstractScreen {

    private SaveButton saveButton;
    private String message = null;

    public MainScreen(GuiScreen background) {
        super("reauth.gui.title.main", background);
    }

    @Override
    public final void initGui() {
        super.initGui();

        int buttonWidthH = BUTTON_WIDTH / 2;
        int y = this.centerY - 55;

        SaveButton.ITooltip saveButtonTooltip = (mouseX, mouseY) -> {
            this.drawHoveringText(this.fontRendererObj.listFormattedStringToWidth(I18n.format("reauth.gui.button.save.tooltip"), 250), mouseX, mouseY);
            RenderHelper.disableStandardItemLighting();
        };
        this.saveButton = new SaveButton(0, this.centerX - buttonWidthH, y + 70, I18n.format("reauth.gui.button.save"), saveButtonTooltip);
        this.buttonList.add(this.saveButton);

        Profile profile = ReAuth.profiles.getProfile();
        if (profile != null) {
            String text = I18n.format("reauth.gui.profile", profile.getValue(ProfileConstants.NAME, "Steve"));
            this.buttonList.add(new GuiButton(2, this.centerX - buttonWidthH, y + 10, BUTTON_WIDTH, 20, text));
        } else {
            GuiButton profileButton = new GuiButton(0, this.centerX - buttonWidthH, y + 10, BUTTON_WIDTH, 20, I18n.format("reauth.gui.noProfile"));
            profileButton.enabled = false;
            this.buttonList.add(profileButton);
        }

        this.buttonList.add(new GuiButton(3, this.centerX - buttonWidthH, y + 45, buttonWidthH - 1, 20, I18n.format("reauth.gui.button.authcode")));
        this.buttonList.add(new GuiButton(4, this.centerX + 1, y + 45, buttonWidthH - 1, 20, I18n.format("reauth.gui.button.devicecode")));
        this.buttonList.add(new GuiButton(5, this.centerX - buttonWidthH, y + 105, BUTTON_WIDTH, 20, I18n.format("reauth.gui.button.offline")));

        ModContainer container = Loader.instance().getIndexedModList().get("reauth");
        ForgeVersion.CheckResult result = ForgeVersion.getResult(container);
        if (result.status == ForgeVersion.Status.OUTDATED) {
            String msg = result.changes.get(result.target);
            if (msg != null) {
                this.message = I18n.format("reauth.gui.auth.update", msg);
            }
        }
    }

    @Override
    public final void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        int x = this.centerX - BUTTON_WIDTH / 2;
        this.fontRendererObj.drawStringWithShadow(I18n.format("reauth.gui.text.profile"), x, this.centerY - 55, 0xFFFFFFFF);
        this.fontRendererObj.drawStringWithShadow(I18n.format("reauth.gui.text.microsoft"), x, this.centerY - 20, 0xFFFFFFFF);
        this.fontRendererObj.drawStringWithShadow(I18n.format("reauth.gui.text.offline"), x, this.centerY + 40, 0xFFFFFFFF);

        if (this.message != null) {
            this.fontRendererObj.drawStringWithShadow(this.message, x, this.baseY + 20, 0xFFFFFFFF);
        }

        this.saveButton.drawTooltip(mouseX, mouseY);
    }

    @Override
    protected final void requestClose(boolean completely) {
        this.mc.displayGuiScreen(this.background);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        switch (button.id) {
            case 2:
                Profile profile = ReAuth.profiles.getProfile();
                if (profile != null) {
                    FlowScreen.open(Flows::loginWithProfile, profile, this.background);
                }
                break;
            case 3:
                FlowScreen.open(Flows::loginWithAuthCode, this.saveButton.isChecked(), this.background);
                break;
            case 4:
                FlowScreen.open(Flows::loginWithDeviceCode, this.saveButton.isChecked(), this.background);
                break;
            case 5:
                this.mc.displayGuiScreen(new OfflineLoginScreen(this.background));
                break;
        }
    }
}
