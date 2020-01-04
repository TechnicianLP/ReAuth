package technicianlp.reauth;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "reauth", value = Dist.CLIENT)
public final class GuiHandler {

    @SubscribeEvent
    public static void onInitGui(InitGuiEvent.Post e) {
        if (e.getGui() instanceof MultiplayerScreen) {
            e.addWidget(new Button(5, 5, 100, 20, I18n.format("reauth.gui.button"), GuiHandler::openAuthenticationScreen));
        } else if (e.getGui() instanceof MainMenuScreen) {
            // Support for Custom Main Menu (add button outside of viewport)
            e.addWidget(new Button(-50, -50, 20, 20, I18n.format("reauth.gui.button"), GuiHandler::openAuthenticationScreen));
        }
    }

    public static void openAuthenticationScreen(Button e) {
        Minecraft.getInstance().displayGuiScreen(new ScreenAuth(Minecraft.getInstance().currentScreen));
    }

    @SubscribeEvent
    public static void onDrawGui(DrawScreenEvent.Post e) {
        if (e.getGui() instanceof MultiplayerScreen) {
            AuthHelper.SessionStatus state = Main.auth.getSessionStatus(false);
            e.getGui().drawString(e.getGui().getMinecraft().fontRenderer, I18n.format(state.getTranslationKey()), 110, 10, 0xFFFFFFFF);
        }
    }

    /**
     * No more IDs = No more checking for correct Button :(
     */
    @SubscribeEvent
    public static void onOpenGui(GuiOpenEvent e) {
        if (Minecraft.getInstance().currentScreen instanceof MultiplayerScreen && e.getGui() instanceof MultiplayerScreen && Screen.hasShiftDown()) {
            Main.auth.getSessionStatus(true);
        }
    }
}
