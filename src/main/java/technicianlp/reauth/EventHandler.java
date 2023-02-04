package technicianlp.reauth;

import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import technicianlp.reauth.configuration.Profile;
import technicianlp.reauth.configuration.ProfileConstants;
import technicianlp.reauth.gui.MainScreen;
import technicianlp.reauth.session.SessionChecker;
import technicianlp.reauth.session.SessionStatus;
import technicianlp.reauth.util.ReflectionUtils;

@Mod.EventBusSubscriber(modid = "reauth", value = Dist.CLIENT)
public final class EventHandler {

    private static final Field disconnectMessage = ReflectionUtils.findObfuscatedField(DisconnectedScreen.class, "f_95988_", "reason");

    @SubscribeEvent
    public static void onInitGui(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (screen instanceof JoinMultiplayerScreen) {
            // Add Button to MultiplayerScreen
            event.addListener(ReAuth.button(5, 5, 100, 20, Component.translatable("reauth.gui.button"), b -> openAuthenticationScreen()));
        } else if (screen instanceof TitleScreen) {
            // Support for Custom Main Menu (add button outside of viewport)
            event.addListener(ReAuth.button(-50, -50, 20, 20, Component.translatable("reauth.gui.button"), b -> openAuthenticationScreen()));
        } else if (screen instanceof DisconnectedScreen) {
            // Add Buttons to DisconnectedScreen if its reason is an invalid session
            handleDisconnectScreen(event, screen);
        } else if (screen instanceof ConnectScreen) {
            // Save Screen to retrieve server later
            ReconnectHelper.setConnectScreen((ConnectScreen) screen);
        }
    }

    private static void handleDisconnectScreen(ScreenEvent.Init.Post event, Screen screen) {
        if ("connect.failed".equals(ReconnectHelper.getTranslationKey(screen.getTitle(), false))) {
            if (ReconnectHelper.getTranslationKey(ReflectionUtils.getField(disconnectMessage, screen), true).startsWith("disconnect.loginFailed")) {
                AbstractWidget menu = (AbstractWidget) event.getListenersList().get(0);

                Profile profile = ReAuth.profiles.getProfile();
                Component retryText;
                if (profile != null) {
                    retryText = Component.translatable("reauth.retry", profile.getValue(ProfileConstants.NAME, "Steve"));
                } else {
                    retryText = Component.translatable("reauth.retry.disabled");
                }
                Button retryButton = ReAuth.button(menu.getX(), menu.getY() + 25, 200, 20, retryText, b -> ReconnectHelper.retryLogin(profile));
                if (profile == null || !ReconnectHelper.hasConnectionInfo()) {
                    retryButton.active = false;
                }
                event.addListener(retryButton);
            }
        }
    }

    private static void openAuthenticationScreen() {
        Minecraft.getInstance().pushGuiLayer(new MainScreen());
    }

    @SuppressWarnings("resource")
    @SubscribeEvent
    public static void onDrawGui(ScreenEvent.Render.Post e) {
        if (e.getScreen() instanceof JoinMultiplayerScreen) {
            User user = Minecraft.getInstance().getUser();
            SessionStatus state = SessionChecker.getSessionStatus(user.getAccessToken(), user.getUuid());
            Minecraft.getInstance().font.drawShadow(e.getPoseStack(), I18n.get(state.getTranslationKey()), 110, 10, 0xFFFFFFFF);
        }
    }

    /**
     * No more IDs = No more checking for correct Button :(
     */
    @SubscribeEvent
    public static void onOpenGui(ScreenEvent.Opening e) {
        if (e.getScreen() instanceof JoinMultiplayerScreen && Minecraft.getInstance().screen instanceof JoinMultiplayerScreen && Screen.hasShiftDown()) {
            SessionChecker.invalidate();
        }
    }
}
