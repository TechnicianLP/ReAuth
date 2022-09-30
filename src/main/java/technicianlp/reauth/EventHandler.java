package technicianlp.reauth;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Session;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import technicianlp.reauth.configuration.Profile;
import technicianlp.reauth.configuration.ProfileConstants;
import technicianlp.reauth.gui.MainScreen;
import technicianlp.reauth.mixin.DisconnectedScreenMixin;
import technicianlp.reauth.session.SessionChecker;
import technicianlp.reauth.session.SessionStatus;

import java.util.List;

public enum EventHandler {
    ;

    public static void register() {
        ScreenEvents.AFTER_INIT.register(EventHandler::afterInit);
        ScreenEvents.BEFORE_INIT.register(EventHandler::beforeInit);
    }

    public static void afterInit(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) {
        if (screen instanceof MultiplayerScreen) {
            // Add Button to MultiplayerScreen
            Screens.getButtons(screen).add(new ButtonWidget(5, 5, 100, 20,
                Text.translatable("reauth.gui.button"), button -> openAuthenticationScreen(screen)));
            ScreenEvents.afterRender(screen).register(EventHandler::afterRender);
        } else if (screen instanceof TitleScreen) {
            // Support for Custom Main Menu (add button outside of viewport)
            Screens.getButtons(screen).add(new ButtonWidget(-50, -50, 20, 20,
                Text.translatable("reauth.gui.button"), button -> openAuthenticationScreen(screen)));
        } else if (screen instanceof DisconnectedScreen) {
            // Add Buttons to DisconnectedScreen if its reason is an invalid session
            handleDisconnectScreen(screen);
        } else if (screen instanceof ConnectScreen) {
            // Save Screen to retrieve server later
            ReconnectHelper.setConnectScreen((ConnectScreen) screen);
        }
    }

    private static void handleDisconnectScreen(Screen screen) {
        if ("connect.failed".equals(ReconnectHelper.getTranslationKey(screen.getTitle(), false))) {
            if (ReconnectHelper.getTranslationKey(((DisconnectedScreenMixin) screen).getReason(), true)
                .startsWith("disconnect.loginFailed")) {
                List<ClickableWidget> buttons = Screens.getButtons(screen);
                ClickableWidget menu = buttons.get(0);

                Profile profile = ReAuth.profiles.getProfile();
                Text retryText;
                if (profile != null) {
                    retryText = Text.translatable("reauth.retry", profile.getValue(ProfileConstants.NAME,
                        "Steve"));
                } else {
                    retryText = Text.translatable("reauth.retry.disabled");
                }
                ButtonWidget retryButton = new ButtonWidget(menu.x, menu.y + 25, 200, 20, retryText,
                    button -> ReconnectHelper.retryLogin(profile));
                if (profile == null || !ReconnectHelper.hasConnectionInfo()) {
                    retryButton.active = false;
                }
                buttons.add(retryButton);
            }
        }
    }

    private static void openAuthenticationScreen(Screen screen) {
        Screens.getClient(screen).setScreen(new MainScreen(screen));
    }

    public static void afterRender(Screen screen, MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
        if (screen instanceof MultiplayerScreen) {
            MinecraftClient client = Screens.getClient(screen);
            Session session = client.getSession();
            SessionStatus state = SessionChecker.getSessionStatus(session.getAccessToken(), session.getUuid());
            Screens.getTextRenderer(screen).drawWithShadow(matrices, I18n.translate(state.getTranslationKey()),
                110, 10,
                0xFFFFFFFF);
        }
    }

    // TODO: add refresh on clicking status
    public static void beforeInit(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) {
        if (screen instanceof MultiplayerScreen && MinecraftClient.getInstance().currentScreen instanceof
            MultiplayerScreen && Screen.hasShiftDown()) {
            SessionChecker.invalidate();
        }
    }
}
