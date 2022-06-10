package technicianlp.reauth;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Session;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import technicianlp.reauth.configuration.Profile;
import technicianlp.reauth.gui.MainScreen;
import technicianlp.reauth.session.SessionChecker;
import technicianlp.reauth.session.SessionStatus;
import technicianlp.reauth.util.ReflectionUtils;

import java.lang.reflect.Field;

public final class EventHandler {

    private static final Field disconnectReason = ReflectionUtils.findObfuscatedField(GuiDisconnected.class, "field_146306_a", "reason");
    private static final Field disconnectMessage = ReflectionUtils.findObfuscatedField(GuiDisconnected.class, "field_146304_f", "message");

    @SubscribeEvent
    public final void onInitGui(InitGuiEvent.Post event) {
        GuiScreen screen = event.gui;
        if (screen instanceof GuiMultiplayer) {
            // Add Button to MultiplayerScreen
            event.buttonList.add(new GuiButton(17325, 5, 5, 100, 20, I18n.format("reauth.gui.button")));
        } else if (screen instanceof GuiMainMenu) {
            // Support for Custom Main Menu (add button outside of viewport)
            event.buttonList.add(new GuiButton(17325, -50, -50, 20, 20, I18n.format("reauth.gui.button")));
        } else if (screen instanceof GuiDisconnected) {
            // Add Buttons to DisconnectedScreen if its reason is an invalid session
            this.handleDisconnectScreen(event, screen);
        } else if (screen instanceof GuiConnecting) {
            // Save Screen to retrieve server later
            ReconnectHelper.setConnectScreen((GuiConnecting) screen);
        }
    }

    private void handleDisconnectScreen(InitGuiEvent.Post event, GuiScreen screen) {
        if (I18n.format("connect.failed").equals(ReflectionUtils.getField(disconnectReason, screen))) {
            if (ReconnectHelper.getTranslationKey(ReflectionUtils.getField(disconnectMessage, screen)).startsWith("disconnect.loginFailed")) {
                GuiButton menu = (GuiButton) event.buttonList.get(0);

                Profile profile = ReAuth.profiles.getProfile();
                String retryText;
                if (profile != null) {
                    retryText = I18n.format("reauth.retry", profile.getValue(Profile.NAME, "Steve"));
                } else {
                    retryText = I18n.format("reauth.retry.disabled");
                }
                GuiButton retryButton = new GuiButton(17325, menu.xPosition, menu.yPosition + 25, 200, 20, retryText);
                if (profile == null || !ReconnectHelper.hasConnectionInfo()) {
                    retryButton.enabled = false;
                }
                event.buttonList.add(retryButton);
            }
        }
    }

    @SubscribeEvent
    public final void onDrawGui(DrawScreenEvent.Post e) {
        if (e.gui instanceof GuiMultiplayer) {
            Session user = Minecraft.getMinecraft().getSession();
            SessionStatus state = SessionChecker.getSessionStatus(user.getToken(), user.getPlayerID());
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(I18n.format(state.getTranslationKey()), 110, 10, 0xFFFFFFFF);
        }
    }

    @SubscribeEvent
    public final void action(GuiScreenEvent.ActionPerformedEvent.Post e) {
        GuiScreen screen = e.gui;
        if ((screen instanceof GuiMainMenu || screen instanceof GuiMultiplayer) && e.button.id == 17325) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.displayGuiScreen(new MainScreen(mc.currentScreen));
        } else if ((screen instanceof GuiDisconnected) && e.button.id == 17325) {
            Profile profile = ReAuth.profiles.getProfile();
            if (profile != null) {
                ReconnectHelper.retryLogin(profile);
            }
        }
    }

    @SubscribeEvent
    public final void action(GuiScreenEvent.ActionPerformedEvent.Pre e) {
        if (e.gui instanceof GuiMultiplayer && e.button.id == 8 && GuiScreen.isShiftKeyDown()) {
            SessionChecker.invalidate();
        }
    }
}
