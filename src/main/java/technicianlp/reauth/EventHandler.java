package technicianlp.reauth;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import technicianlp.reauth.gui.ScreenAuth;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = "reauth", value = Dist.CLIENT)
public final class EventHandler {

    private static final Field disconnectMessage = ObfuscationReflectionHelper.findField(DisconnectedScreen.class, "field_146304_f");
    private static final Field previousScreen = ObfuscationReflectionHelper.findField(DisconnectedScreen.class, "field_146307_h");

    @SubscribeEvent
    public static void onInitGui(InitGuiEvent.Post event) {
        Screen screen = event.getGui();
        if (screen instanceof MultiplayerScreen) {
            // Add Button to MultiplayerScreen
            event.addWidget(new Button(5, 5, 100, 20, I18n.format("reauth.gui.button"), (b) -> openAuthenticationScreen(screen)));
        } else if (screen instanceof MainMenuScreen) {
            // Support for Custom Main Menu (add button outside of viewport)
            event.addWidget(new Button(-50, -50, 20, 20, I18n.format("reauth.gui.button"), (b) -> openAuthenticationScreen(screen)));
        } else if (screen instanceof DisconnectedScreen) {
            // Change DisconnectedScreen if its reason is an invalid session
            if ("connect.failed".equals(getTranslationKey(screen.getTitle()))) {
                if (getTranslationKey(ReAuth.getField(disconnectMessage, screen)).startsWith("disconnect.loginFailed")) {
                    Widget menu = event.getWidgetList().get(0);
                    event.addWidget(new Button(menu.x, menu.y + 25, 200, 20, I18n.format("reauth.open"),
                            (b) -> openAuthenticationScreen(ReAuth.getField(previousScreen, screen))));
                }
            }
        }
    }

    private static void openAuthenticationScreen(Screen parent) {
        Minecraft.getInstance().displayGuiScreen(new ScreenAuth(parent));
    }

    private static String getTranslationKey(Object component) {
        if (component instanceof TranslationTextComponent) {
            return ((TranslationTextComponent) component).getKey();
        }
        return "";
    }

    @SubscribeEvent
    public static void onDrawGui(DrawScreenEvent.Post e) {
        if (e.getGui() instanceof MultiplayerScreen) {
            AuthHelper.SessionStatus state = ReAuth.auth.getSessionStatus(false);
            e.getGui().drawString(e.getGui().getMinecraft().fontRenderer, I18n.format(state.getTranslationKey()), 110, 10, 0xFFFFFFFF);
        }
    }

    /**
     * No more IDs = No more checking for correct Button :(
     */
    @SubscribeEvent
    public static void onOpenGui(GuiOpenEvent e) {
        if (e.getGui() instanceof MultiplayerScreen && Minecraft.getInstance().currentScreen instanceof MultiplayerScreen && Screen.hasShiftDown()) {
            ReAuth.auth.getSessionStatus(true);
        }
    }
}
