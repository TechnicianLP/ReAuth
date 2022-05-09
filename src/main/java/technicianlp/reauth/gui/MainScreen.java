package technicianlp.reauth.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import technicianlp.reauth.ReAuth;
import technicianlp.reauth.authentication.flows.Flow;
import technicianlp.reauth.authentication.flows.FlowCallback;
import technicianlp.reauth.authentication.flows.Flows;
import technicianlp.reauth.configuration.Profile;

import java.io.IOException;
import java.util.function.BiFunction;

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
            this.drawHoveringText(this.fontRenderer.listFormattedStringToWidth(I18n.format("reauth.gui.button.save.tooltip"), 250), mouseX, mouseY);
            RenderHelper.disableStandardItemLighting();
        };
        this.saveButton = new SaveButton(0, this.centerX - buttonWidthH, y + 70, I18n.format("reauth.gui.button.save"), saveButtonTooltip);
        this.addButton(this.saveButton);

        Profile profile = ReAuth.profiles.getProfile();
        if (profile != null) {
            String text = I18n.format("reauth.gui.profile", profile.getValue(Profile.NAME, "Steve"));
            this.addButton(new GuiButton(2, this.centerX - buttonWidthH, y + 10, BUTTON_WIDTH, 20, text));
        } else {
            GuiButton profileButton = new GuiButton(0, this.centerX - buttonWidthH, y + 10, BUTTON_WIDTH, 20, I18n.format("reauth.gui.noProfile"));
            profileButton.enabled = false;
            this.addButton(profileButton);
        }

        this.addButton(new GuiButton(3, this.centerX - buttonWidthH, y + 45, buttonWidthH - 1, 20, I18n.format("This Device")));
        this.addButton(new GuiButton(4, this.centerX + 1, y + 45, buttonWidthH - 1, 20, I18n.format("Any Device")));
        this.addButton(new GuiButton(5, this.centerX - buttonWidthH, y + 105, BUTTON_WIDTH, 20, I18n.format("Choose Username")));

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
        this.fontRenderer.drawStringWithShadow(I18n.format("reauth.gui.text.profile"), x, this.centerY - 55, 0xFFFFFFFF);
        this.fontRenderer.drawStringWithShadow(I18n.format("reauth.gui.text.microsoft"), x, this.centerY - 20, 0xFFFFFFFF);
        this.fontRenderer.drawStringWithShadow(I18n.format("reauth.gui.text.offline"), x, this.centerY + 40, 0xFFFFFFFF);

        if (this.message != null) {
            this.fontRenderer.drawStringWithShadow(this.message, x, this.baseY + 20, 0xFFFFFFFF);
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
                    this.openFlow(Flows::loginWithProfile, profile);
                }
                break;
            case 3:
                this.openFlow(Flows::loginWithAuthCode, this.saveButton.isChecked());
                break;
            case 4:
                this.openFlow(Flows::loginWithDeviceCode, this.saveButton.isChecked());
                break;
            case 5:
                this.mc.displayGuiScreen(new OfflineLoginScreen(this.background));
                break;
        }
    }

    private <P> void openFlow(BiFunction<P, FlowCallback, Flow> flow, P param) {
        FlowScreen screen = new FlowScreen(this.background);
        screen.setFlow(flow.apply(param, screen));
        this.mc.displayGuiScreen(screen);
    }
}
