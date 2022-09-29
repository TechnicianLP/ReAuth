package technicianlp.reauth.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import technicianlp.reauth.ReAuth;
import technicianlp.reauth.authentication.SessionData;
import technicianlp.reauth.authentication.flows.*;
import technicianlp.reauth.configuration.Profile;
import technicianlp.reauth.session.SessionHelper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

public final class FlowScreen extends AbstractScreen implements FlowCallback {

    private static final String[] EMPTY_STRINGS_ARRAY = new String[0];
    private static final Pattern PATTERN_LINE_BREAK = Pattern.compile("\\R");

    public static <F extends Flow, P> F open(BiFunction<P, ? super FlowCallback, F> flowConstructor, P param) {
        FlowScreen screen = new FlowScreen();
        F flow = flowConstructor.apply(param, screen);
        screen.flow = flow;
        MinecraftClient.getInstance().setScreen(screen);
        return flow;
    }

    private Flow flow;
    private FlowStage stage = FlowStage.INITIAL;
    private String[] formatArgs = EMPTY_STRINGS_ARRAY;

    public FlowScreen() {
        super("reauth.gui.title.flow");
    }

    @Override
    public void init() {
        super.init();

        int buttonWidth = 196;
        int buttonWidthH = buttonWidth / 2;

        this.formatArgs = EMPTY_STRINGS_ARRAY;
        if (this.stage == FlowStage.MS_AWAIT_AUTH_CODE && this.flow instanceof AuthorizationCodeFlow) {
            try {
                URL url = new URL(((AuthorizationCodeFlow) this.flow).getLoginUrl());
                this.addDrawableChild(new ButtonWidget(this.centerX - buttonWidthH,
                    this.baseY + screenHeight - 42, buttonWidth, 20, Text.translatable("reauth.msauth.button" +
                    ".browser"), button -> Util.getOperatingSystem().open(url)));
            } catch (MalformedURLException e) {
                ReAuth.log.error("Browser button failed", e);
            }
        } else if (this.stage == FlowStage.MS_POLL_DEVICE_CODE && this.flow instanceof DeviceCodeFlow flow) {
            try {
                if (CompletableFuture.allOf(flow.getLoginUrl(), flow.getCode()).isDone()) {
                    String urlString = flow.getLoginUrl().join();
                    String code = flow.getCode().join();
                    URL url = new URL(urlString);
                    this.addDrawableChild(new ButtonWidget(this.centerX - buttonWidthH,
                        this.baseY + screenHeight - 42, buttonWidth, 20,
                        Text.translatable("reauth.msauth.button.browser"),
                        button -> Util.getOperatingSystem().open(url)));
                    this.formatArgs = new String[]{urlString, code};
                }
            } catch (MalformedURLException e) {
                ReAuth.log.error("Browser button failed", e);
            }
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        String text = I18n.translate(this.stage.getRawName(), (Object[]) this.formatArgs);
        String[] lines = PATTERN_LINE_BREAK.split(text);
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
                matrices.push();
                matrices.scale(2, 2, 1);
                this.textRenderer.drawWithShadow(matrices, line,
                    (this.centerX - this.textRenderer.getWidth(line)) / 2f, y / 2f, 0xFFFFFFFF);
                y += 18;
                matrices.pop();
            } else {
                this.textRenderer.drawWithShadow(matrices, line,
                    this.centerX - this.textRenderer.getWidth(line) / 2f, y, 0xFFFFFFFF);
                y += 9;
            }
        }
    }

    @Override
    public void removed() {
        super.removed();
        if (this.stage != FlowStage.FINISHED) {
            this.flow.cancel();
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
    public void transitionStage(FlowStage newStage) {
        this.stage = newStage;
        ReAuth.log.info(this.stage.getLogLine());
        this.init(MinecraftClient.getInstance(), this.width, this.height);

        if (newStage == FlowStage.MS_AWAIT_AUTH_CODE && this.flow instanceof AuthorizationCodeFlow) {
            try {
                Util.getOperatingSystem().open(new URL(((AuthorizationCodeFlow) this.flow).getLoginUrl()));
            } catch (MalformedURLException e) {
                ReAuth.log.error("Failed to open page", e);
            }
        } else if (newStage == FlowStage.FINISHED) {
            this.requestClose(true);
        }
    }

    @Override
    public Executor getExecutor() {
        return ReAuth.executor;
    }
}
