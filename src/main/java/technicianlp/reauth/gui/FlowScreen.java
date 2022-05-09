package technicianlp.reauth.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;
import technicianlp.reauth.ReAuth;
import technicianlp.reauth.authentication.flows.AuthorizationCodeFlow;
import technicianlp.reauth.authentication.flows.DeviceCodeFlow;
import technicianlp.reauth.authentication.flows.Flow;
import technicianlp.reauth.authentication.flows.FlowCallback;
import technicianlp.reauth.authentication.flows.FlowStage;
import technicianlp.reauth.configuration.Profile;
import technicianlp.reauth.session.SessionHelper;
import technicianlp.reauth.util.ReflectionHelper;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class FlowScreen extends AbstractScreen implements FlowCallback {

    private static final Method openURL = ReflectionHelper.findMcpMethod(GuiScreen.class, "func_175282_a", void.class, URI.class);

    private Flow flow;
    private FlowStage stage = FlowStage.INITIAL;
    private String[] formatArgs = new String[0];

    public FlowScreen(GuiScreen background) {
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
    public final void initGui() {
        super.initGui();

        int buttonWidth = 196;
        int buttonWidthH = buttonWidth / 2;

        this.formatArgs = new String[0];
        if (this.stage == FlowStage.MS_AWAIT_AUTH_CODE && this.flow instanceof AuthorizationCodeFlow) {
            this.addButton(new GuiButton(2, this.centerX - buttonWidthH, this.baseY + this.screenHeight - 42, buttonWidth, 20, I18n.format("reauth.msauth.button.browser")));
        } else if (this.stage == FlowStage.MS_POLL_DEVICE_CODE && this.flow instanceof DeviceCodeFlow) {
            DeviceCodeFlow flow = (DeviceCodeFlow) this.flow;
            if (CompletableFuture.allOf(flow.getLoginUrl(), flow.getCode()).isDone()) {
                this.addButton(new GuiButton(3, this.centerX - buttonWidthH, this.baseY + this.screenHeight - 42, buttonWidth, 20, I18n.format("reauth.msauth.button.browser")));
                this.formatArgs = new String[]{flow.getLoginUrl().join(), flow.getCode().join()};
            }
        }
    }

    @Override
    public final void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        String text = I18n.format(this.stage.getRawName(), (Object[]) this.formatArgs);
        String[] lines = text.split("\\\\n");
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
                this.fontRenderer.drawStringWithShadow(line, (float) (this.centerX - this.fontRenderer.getStringWidth(line)) / 2, (float) y / 2, 0xFFFFFFFF);
                y += 18;
                GL11.glPopMatrix();
            } else {
                this.fontRenderer.drawStringWithShadow(line, (float) (this.centerX - this.fontRenderer.getStringWidth(line) / 2), (float) y, 0xFFFFFFFF);
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

    @Override
    public final void onGuiClosed() {
        super.onGuiClosed();
        if (this.stage != FlowStage.FINISHED) {
            this.flow.cancel();
        }
    }

    @Override
    public final void transitionStage(FlowStage newStage) {
        this.stage = newStage;
        ReAuth.log.info(this.stage.getLogLine());
        this.setWorldAndResolution(Minecraft.getMinecraft(), this.width, this.height);

        if (newStage == FlowStage.MS_AWAIT_AUTH_CODE && this.flow instanceof AuthorizationCodeFlow) {
            try {
                URI uri = new URI(((AuthorizationCodeFlow) this.flow).getLoginUrl());
                ReflectionHelper.callMethod(openURL, this, uri);
            } catch (URISyntaxException e) {
                ReAuth.log.error("Failed to open page", e);
            }
        } else if (newStage == FlowStage.FINISHED) {
            this.requestClose(true);
        }
    }

    @Override
    public final Executor getExecutor() {
        return ReAuth.executor;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button.id == 2 && this.flow instanceof AuthorizationCodeFlow) {
            try {
                URI uri = new URI(((AuthorizationCodeFlow) this.flow).getLoginUrl());
                ReflectionHelper.callMethod(openURL, this, uri);
            } catch (URISyntaxException e) {
                ReAuth.log.error("Browser button failed", e);
            }
        } else if (button.id == 3 && this.flow instanceof DeviceCodeFlow) {
            try {
                CompletableFuture<String> loginUrl = ((DeviceCodeFlow) this.flow).getLoginUrl();
                if (loginUrl.isDone()) {
                    ReflectionHelper.callMethod(openURL, this, new URI(loginUrl.join()));
                }
            } catch (URISyntaxException e) {
                ReAuth.log.error("Browser button failed", e);
            }
        }
    }
}
