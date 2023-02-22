package technicianlp.reauth.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;
import technicianlp.reauth.ReAuth;
import technicianlp.reauth.authentication.SessionData;
import technicianlp.reauth.authentication.flows.AuthorizationCodeFlow;
import technicianlp.reauth.authentication.flows.DeviceCodeFlow;
import technicianlp.reauth.authentication.flows.Flow;
import technicianlp.reauth.authentication.flows.FlowCallback;
import technicianlp.reauth.authentication.flows.FlowStage;
import technicianlp.reauth.authentication.flows.Flows;
import technicianlp.reauth.configuration.Profile;
import technicianlp.reauth.session.SessionHelper;

import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;

public final class FlowScreen extends AbstractScreen implements FlowCallback {

    public static <F extends Flow, P> F open(BiFunction<P, FlowCallback, F> flowConstructor, P param, GuiScreen background) {
        FlowScreen screen = new FlowScreen(background);
        F flow = flowConstructor.apply(param, screen);
        screen.flow = flow;
        Minecraft.getMinecraft().displayGuiScreen(screen);
        return flow;
    }

    private Flow flow;
    private FlowStage stage = FlowStage.INITIAL;
    private String[] formatArgs = new String[0];
    private String errorText = null;

    private FlowScreen(GuiScreen background) {
        super("reauth.gui.title.flow", background);
    }

    @Override
    public final void initGui() {
        super.initGui();

        int buttonWidth = 196;
        int buttonWidthH = buttonWidth / 2;

        this.formatArgs = new String[0];
        this.errorText = null;
        if (this.stage == FlowStage.MS_AWAIT_AUTH_CODE && this.flow instanceof AuthorizationCodeFlow) {
            this.buttonList.add(new GuiButton(2, this.centerX - buttonWidthH, this.baseY + this.screenHeight - 42, buttonWidth, 20, I18n.format("reauth.msauth.button.browser")));
        } else if (this.stage == FlowStage.MS_POLL_DEVICE_CODE && this.flow instanceof DeviceCodeFlow) {
            DeviceCodeFlow flow = (DeviceCodeFlow) this.flow;
            if (CompletableFuture.allOf(flow.getLoginUrl(), flow.getCode()).isDone()) {
                this.buttonList.add(new GuiButton(3, this.centerX - buttonWidthH, this.baseY + this.screenHeight - 42, buttonWidth, 20, I18n.format("reauth.msauth.button.browser")));
                this.formatArgs = new String[]{flow.getLoginUrl().join(), flow.getCode().join()};
            }
        } else if (this.stage == FlowStage.FAILED) {
            this.errorText = Flows.getFailureReason(this.flow);
        }
    }

    @Override
    public final void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        String text = I18n.format(this.stage.getRawName(), (Object[]) this.formatArgs);
        List<String> lines = new ArrayList<>();

        Collections.addAll(lines, text.split("\\\\n"));
        if(this.errorText != null) {
            lines.add("");
            Collections.addAll(lines, I18n.format(this.errorText).split("\\\\n"));
        }

        int height = lines.size() * 9;
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
                this.fontRendererObj.drawStringWithShadow(line, (this.centerX - this.fontRendererObj.getStringWidth(line)) / 2, y / 2, 0xFFFFFFFF);
                y += 18;
                GL11.glPopMatrix();
            } else {
                this.fontRendererObj.drawStringWithShadow(line, this.centerX - this.fontRendererObj.getStringWidth(line) / 2, y, 0xFFFFFFFF);
                y += 9;
            }
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
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);

        String uriString = null;
        if (button.id == 2 && this.flow instanceof AuthorizationCodeFlow) {
            uriString = ((AuthorizationCodeFlow) this.flow).getLoginUrl();
        } else if (button.id == 3 && this.flow instanceof DeviceCodeFlow) {
            CompletableFuture<String> loginUrl = ((DeviceCodeFlow) this.flow).getLoginUrl();
            if (loginUrl.isDone()) {
                uriString = loginUrl.join();
            }
        }
        if (uriString != null) {
            try {
                URI uri = new URI(uriString);
                this.openLink(uri);
            } catch (URISyntaxException e) {
                ReAuth.log.error("Browser button failed", e);
            }
        }
    }

    @Override
    public void onSessionComplete(SessionData session, Throwable throwable) {
        if (throwable == null) {
            SessionHelper.setSession(session);
            ReAuth.log.info("Login complete");
        } else {
            if (throwable instanceof CancellationException || throwable.getCause() instanceof CancellationException) {
                ReAuth.log.info("Login cancelled");
            } else {
                ReAuth.log.error("Login failed", throwable);
            }
        }
    }

    @Override
    public void onProfileComplete(Profile profile, Throwable throwable) {
        if (throwable == null) {
            ReAuth.profiles.storeProfile(profile);
            ReAuth.log.info("Profile saved successfully");
        } else {
            if (throwable instanceof CancellationException || throwable.getCause() instanceof CancellationException) {
                ReAuth.log.info("Profile saving cancelled");
            } else {
                ReAuth.log.error("Profile failed to save", throwable);
            }
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
                this.openLink(uri);
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

    /**
     * @see GuiChat#openLink(URI)
     */
    private void openLink(URI uri) {
        try {
            Desktop.getDesktop().browse(uri);
        } catch (Throwable throwable) {
            ReAuth.log.error("Couldn't open link", throwable);
        }
    }
}
