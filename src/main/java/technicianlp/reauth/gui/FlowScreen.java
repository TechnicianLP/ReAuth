package technicianlp.reauth.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Util;
import org.lwjgl.opengl.GL11;
import technicianlp.reauth.ReAuth;
import technicianlp.reauth.authentication.flows.AuthorizationCodeFlow;
import technicianlp.reauth.authentication.flows.DeviceCodeFlow;
import technicianlp.reauth.authentication.flows.Flow;
import technicianlp.reauth.authentication.flows.FlowCallback;
import technicianlp.reauth.authentication.flows.FlowStage;
import technicianlp.reauth.configuration.Profile;
import technicianlp.reauth.session.SessionHelper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class FlowScreen extends AbstractScreen implements FlowCallback {

    private Flow flow;
    private FlowStage stage = FlowStage.INITIAL;
    private String[] formatArgs = new String[0];
    private boolean autoClose = true;

    public FlowScreen(Screen background) {
        super("reauth.gui.title.flow", background);
    }

    public final void setFlow(Flow flow) {
        this.flow = flow;
        flow.getSession().thenAccept(SessionHelper::setSession);
        if (flow.hasProfile()) {
            flow.getProfile().whenComplete(this::profileComplete);
        }
    }

    @Override
    public final void init() {
        super.init();

        int buttonWidth = 196;
        int buttonWidthH = buttonWidth / 2;

        this.formatArgs = new String[0];
        if (this.stage == FlowStage.MS_AWAIT_AUTH_CODE && this.flow instanceof AuthorizationCodeFlow) {
            try {
                URL url = new URL(((AuthorizationCodeFlow) this.flow).getLoginUrl());
                this.addButton(new Button(this.centerX - buttonWidthH, this.baseY + this.screenHeight - 42, buttonWidth, 20, I18n.format("reauth.msauth.button.browser"), (b) -> Util.getOSType().openURL(url)));
            } catch (MalformedURLException e) {
                ReAuth.log.error("Browser button failed", e);
            }
        } else if (this.stage == FlowStage.MS_POLL_DEVICE_CODE && this.flow instanceof DeviceCodeFlow) {
            try {
                DeviceCodeFlow flow = (DeviceCodeFlow) this.flow;
                if (CompletableFuture.allOf(flow.getLoginUrl(), flow.getCode()).isDone()) {
                    String urlString = flow.getLoginUrl().join();
                    String code = flow.getCode().join();
                    URL url = new URL(urlString);
                    this.addButton(new Button(this.centerX - buttonWidthH, this.baseY + this.screenHeight - 42, buttonWidth, 20, I18n.format("reauth.msauth.button.browser"), (b) -> Util.getOSType().openURL(url)));
                    this.formatArgs = new String[]{urlString, code};
                }
            } catch (MalformedURLException e) {
                ReAuth.log.error("Browser button failed", e);
            }
        }
    }

    @Override
    public final void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        String text = I18n.format(this.stage.getRawName(), (Object[]) this.formatArgs);
        String[] lines = text.split("\\R");
        int height = lines.length * 9;
        for (String s : lines) {
            if (s.startsWith("$")) {
                height += 9;
            }
        }

        int y = this.centerY - height / 2;
        for (String line : lines) {
            if (line.startsWith("$")) {
                line = line.substring(1);
                GL11.glPushMatrix();
                GL11.glScalef(2, 2, 1);
                this.font.drawStringWithShadow(line, (float) (this.centerX - this.font.getStringWidth(line)) / 2, (float) y / 2, 0xFFFFFFFF);
                y += 18;
                GL11.glPopMatrix();
            } else {
                this.font.drawStringWithShadow(line, (float) (this.centerX - this.font.getStringWidth(line) / 2), (float) y, 0xFFFFFFFF);
                y += 9;
            }
        }
    }

    private void profileComplete(Profile profile, Throwable throwable) {
        if (throwable == null) {
            ReAuth.profiles.storeProfile(profile);
            ReAuth.log.info("Profile saved successfully");
        } else {
            ReAuth.log.error("Profile failed to save", throwable);
        }
    }

    public final void disableAutoClose() {
        this.autoClose = false;
    }

    @Override
    public final void removed() {
        super.removed();
        this.flow.cancel();
    }

    @Override
    public final void transitionStage(FlowStage newStage) {
        this.stage = newStage;
        ReAuth.log.info(this.stage.getLogLine());
        this.init(Minecraft.getInstance(), this.width, this.height);

        if (newStage == FlowStage.MS_AWAIT_AUTH_CODE && this.flow instanceof AuthorizationCodeFlow) {
            try {
                Util.getOSType().openURL(new URL(((AuthorizationCodeFlow) this.flow).getLoginUrl()));
            } catch (MalformedURLException e) {
                ReAuth.log.error("Failed to open page", e);
            }
        } else if (this.autoClose && newStage == FlowStage.FINISHED) {
            this.requestClose(true);
        }
    }

    @Override
    public final Executor getExecutor() {
        return ReAuth.executor;
    }
}
