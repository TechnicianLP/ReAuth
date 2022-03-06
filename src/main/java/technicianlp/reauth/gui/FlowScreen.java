package technicianlp.reauth.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
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

/**
 * Please hold the line while this flow runs
 * - shows progress
 * - shows failure
 * - has back button
 */
public final class FlowScreen extends AbstractScreen implements FlowCallback {

    private Flow flow;
    private FlowStage stage = FlowStage.INITIAL;
    private String[] formatArgs = new String[0];
    private boolean autoClose = true;

    public FlowScreen() {
        super("reauth.gui.title.flow");
    }

    public void setFlow(Flow flow) {
        this.flow = flow;
        flow.getSession().thenAccept(SessionHelper::setSession);
        if (flow.hasProfile()) {
            flow.getProfile().whenComplete(this::profileComplete);
        }
    }

    @Override
    public void init() {
        super.init();

        int buttonWidth = 196;
        int buttonWidthH = buttonWidth / 2;

        this.formatArgs = new String[0];
        if (this.stage == FlowStage.MS_AWAIT_AUTH_CODE && this.flow instanceof AuthorizationCodeFlow) {
            try {
                URL url = new URL(((AuthorizationCodeFlow) this.flow).getLoginUrl());
                this.addRenderableWidget(new Button(this.centerX - buttonWidthH, this.baseY + this.screenHeight - 42, buttonWidth, 20, new TranslatableComponent("reauth.msauth.button.browser"), (b) -> Util.getPlatform().openUrl((url))));
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
                    this.addRenderableWidget(new Button(this.centerX - buttonWidthH, this.baseY + this.screenHeight - 42, buttonWidth, 20, new TranslatableComponent("reauth.msauth.button.browser"), (b) -> Util.getPlatform().openUrl((url))));
                    this.formatArgs = new String[]{urlString, code};
                }
            } catch (MalformedURLException e) {
                ReAuth.log.error("Browser button failed", e);
            }
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);

        String text = I18n.get(this.stage.getRawName(), (Object[]) this.formatArgs);
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
                poseStack.pushPose();
                poseStack.scale(2, 2, 1);
                this.font.drawShadow(poseStack, line, (float) (this.centerX - this.font.width(line)) / 2, (float) y / 2, 0xFFFFFFFF);
                y += 18;
                poseStack.popPose();
            } else {
                this.font.drawShadow(poseStack, line, (float) (this.centerX - this.font.width(line) / 2), (float) y, 0xFFFFFFFF);
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

    public void disableAutoClose() {
        this.autoClose = false;
    }

    @Override
    public void removed() {
        super.removed();
        this.flow.cancel();
    }

    @Override
    public void transitionStage(FlowStage newStage) {
        this.stage = newStage;
        ReAuth.log.info(this.stage.getLogLine());
        this.init(Minecraft.getInstance(), this.width, this.height);

        if (newStage == FlowStage.MS_AWAIT_AUTH_CODE && this.flow instanceof AuthorizationCodeFlow) {
            try {
                Util.getPlatform().openUrl(new URL(((AuthorizationCodeFlow) this.flow).getLoginUrl()));
            } catch (MalformedURLException e) {
                ReAuth.log.error("Failed to open page", e);
            }
        } else if (this.autoClose && newStage == FlowStage.FINISHED) {
            this.requestClose(true);
        }
    }

    @Override
    public Executor getExecutor() {
        return ReAuth.executor;
    }
}
