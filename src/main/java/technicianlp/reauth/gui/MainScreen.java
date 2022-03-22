package technicianlp.reauth.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.VersionChecker;
import technicianlp.reauth.ReAuth;
import technicianlp.reauth.authentication.flows.Flows;
import technicianlp.reauth.configuration.Profile;

public final class MainScreen extends AbstractScreen {

    private String message = null;

    public MainScreen(Screen background) {
        super("reauth.gui.title.main", background);
    }

    @Override
    public final void init() {
        super.init();
        this.getMinecraft().keyboardListener.enableRepeatEvents(true);

        int buttonWidthH = BUTTON_WIDTH / 2;
        int y = this.centerY - 55;

        SaveButton.ITooltip saveButtonTooltip = (button, mouseX, mouseY) -> this.renderTooltip(this.font.listFormattedStringToWidth(I18n.format("reauth.gui.button.save.tooltip"), 250), mouseX, mouseY);
        SaveButton saveButton = new SaveButton(this.centerX - buttonWidthH, y + 70, I18n.format("reauth.gui.button.save"), saveButtonTooltip);
        this.addButton(saveButton);

        Profile profile = ReAuth.profiles.getProfile();
        if (profile != null) {
            String text = I18n.format("reauth.gui.profile", profile.getValue(Profile.NAME, "Steve"));
            this.addButton(new Button(this.centerX - buttonWidthH, y + 10, BUTTON_WIDTH, 20, text, (b) -> {
                FlowScreen screen = new FlowScreen(this.background);
                screen.setFlow(Flows.loginWithProfile(profile, screen));
                this.getMinecraft().displayGuiScreen(screen);
            }));
        } else {
            Button profileButton = new Button(this.centerX - buttonWidthH, y + 10, BUTTON_WIDTH, 20, I18n.format("reauth.gui.noProfile"), (b) -> {
            });
            profileButton.active = false;
            this.addButton(profileButton);
        }

        this.addButton(new Button(this.centerX - buttonWidthH, y + 45, buttonWidthH - 1, 20, I18n.format("This Device"), (b) -> {
            FlowScreen screen = new FlowScreen(this.background);
            screen.setFlow(Flows.loginWithAuthCode(saveButton.isChecked(), screen));
            this.getMinecraft().displayGuiScreen(screen);
        }));
        this.addButton(new Button(this.centerX + 1, y + 45, buttonWidthH - 1, 20, I18n.format("Any Device"), (b) -> {
            FlowScreen screen = new FlowScreen(this.background);
            screen.setFlow(Flows.loginWithDeviceCode(saveButton.isChecked(), screen));
            this.getMinecraft().displayGuiScreen(screen);
        }));
        this.addButton(new Button(this.centerX - buttonWidthH, y + 105, BUTTON_WIDTH, 20, I18n.format("Choose Username"), (b) -> this.getMinecraft().displayGuiScreen(new OfflineLoginScreen(this.background))));


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
    public final void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        int x = this.centerX - BUTTON_WIDTH / 2;
        this.font.drawStringWithShadow(I18n.format("reauth.gui.text.profile"), x, this.centerY - 55, 0xFFFFFFFF);
        this.font.drawStringWithShadow(I18n.format("reauth.gui.text.microsoft"), x, this.centerY - 20, 0xFFFFFFFF);
        this.font.drawStringWithShadow(I18n.format("reauth.gui.text.offline"), x, this.centerY + 40, 0xFFFFFFFF);

        if (this.message != null) {
            this.font.drawStringWithShadow(this.message, x, this.baseY + 20, 0xFFFFFFFF);
        }
    }

    @Override
    protected final void requestClose(boolean completely) {
        this.getMinecraft().displayGuiScreen(this.background);
    }
}
