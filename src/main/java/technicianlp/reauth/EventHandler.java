package technicianlp.reauth;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Session;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import technicianlp.reauth.configuration.Profile;
import technicianlp.reauth.configuration.ProfileConstants;
import technicianlp.reauth.gui.MainScreen;
import technicianlp.reauth.session.SessionChecker;
import technicianlp.reauth.session.SessionStatus;
import technicianlp.reauth.util.ReflectionUtils;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = "reauth", value = Dist.CLIENT)
public final class EventHandler {

    private static final Field disconnectMessage = ReflectionUtils.findObfuscatedField(DisconnectedScreen.class, "field_146304_f", "message");

    @SubscribeEvent
    public static void onInitGui(InitGuiEvent.Post event) {
        Screen screen = event.getGui();
        if (screen instanceof MultiplayerScreen) {
            // Add Button to MultiplayerScreen
            event.addWidget(new Button(5, 5, 100, 20, I18n.format("reauth.gui.button"), b -> openAuthenticationScreen()));
        } else if (screen instanceof MainMenuScreen) {
            // Support for Custom Main Menu (add button outside of viewport)
            event.addWidget(new Button(-50, -50, 20, 20, I18n.format("reauth.gui.button"), b -> openAuthenticationScreen()));
        } else if (screen instanceof DisconnectedScreen) {
            // Add Buttons to DisconnectedScreen if its reason is an invalid session
            handleDisconnectScreen(event, screen);
        } else if (screen instanceof ConnectingScreen) {
            // Save Screen to retrieve server later
            ReconnectHelper.setConnectScreen((ConnectingScreen) screen);
        }
    }

    private static void handleDisconnectScreen(InitGuiEvent.Post event, Screen screen) {
        if ("connect.failed".equals(ReconnectHelper.getTranslationKey(screen.getTitle()))) {
            if (ReconnectHelper.getTranslationKey(ReflectionUtils.getField(disconnectMessage, screen)).startsWith("disconnect.loginFailed")) {
                Widget menu = event.getWidgetList().get(0);

                Profile profile = ReAuth.profiles.getProfile();
                String retryText;
                if (profile != null) {
                    retryText = I18n.format("reauth.retry", profile.getValue(ProfileConstants.NAME, "Steve"));
                } else {
                    retryText = I18n.format("reauth.retry.disabled");
                }
                Button retryButton = new Button(menu.x, menu.y + 25, 200, 20, retryText, b -> ReconnectHelper.retryLogin(profile));
                if (profile == null || !ReconnectHelper.hasConnectionInfo()) {
                    retryButton.active = false;
                }
                event.addWidget(retryButton);
            }
        }
    }

    private static void openAuthenticationScreen() {
        Minecraft mc = Minecraft.getInstance();
        mc.displayGuiScreen(new MainScreen(mc.currentScreen));
    }

    @SubscribeEvent
    public static void onDrawGui(DrawScreenEvent.Post e) {
        if (e.getGui() instanceof MultiplayerScreen) {
            Session user = Minecraft.getInstance().getSession();
            SessionStatus state = SessionChecker.getSessionStatus(user.getToken(), user.getPlayerID());
            Minecraft.getInstance().fontRenderer.drawStringWithShadow(I18n.format(state.getTranslationKey()), 110, 10, 0xFFFFFFFF);
        }
    }

    /**
     * No more IDs = No more checking for correct Button :(
     */
    @SubscribeEvent
    public static void onOpenGui(GuiOpenEvent e) {
        if (e.getGui() instanceof MultiplayerScreen && Minecraft.getInstance().currentScreen instanceof MultiplayerScreen && Screen.hasShiftDown()) {
            SessionChecker.invalidate();
        }
    }
}
