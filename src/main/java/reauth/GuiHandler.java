package reauth;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

public class GuiHandler {

    static boolean enabled = true;
    static boolean bold = true;

    @SubscribeEvent
    public void open(InitGuiEvent.Post e) {
        boolean run = false;
        if (e.getGui() instanceof GuiMultiplayer) {
            e.getButtonList().add(new GuiButton(17325, 5, 5, 100, 20, "Re-Login"));
            run = true;

            if (enabled)
                Secure.checkSessionValidity();
        } else if (e.getGui() instanceof GuiMainMenu) {
            run = true;
            // Support for Custom Main Menu (add button outside of viewport)
            e.getButtonList().add(new GuiButton(17325, -50, -50, 20, 20, "ReAuth"));
        }

        if (run && VersionChecker.shouldRun())
            VersionChecker.update();
    }

    @SubscribeEvent
    public void draw(DrawScreenEvent.Post e) {
        if (enabled && e.getGui() instanceof GuiMultiplayer) {
            e.getGui().drawString(e.getGui().mc.fontRendererObj, "Online:", 110, 10, 0xFFFFFFFF);
            Secure.ValidationStatus state = Secure.getSessionValidity();
            e.getGui().drawString(e.getGui().mc.fontRendererObj, (bold ? ChatFormatting.BOLD : "") + state.text, 145, 10, state.color);
        }
    }

    @SubscribeEvent
    public void action(ActionPerformedEvent.Post e) {
        if ((e.getGui() instanceof GuiMainMenu || e.getGui() instanceof GuiMultiplayer) && e.getButton().id == 17325) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiLogin(Minecraft.getMinecraft().currentScreen));
        }
    }

    @SubscribeEvent
    public void action(ActionPerformedEvent.Pre e) {
        if (enabled && e.getGui() instanceof GuiMultiplayer && e.getButton().id == 8 && GuiScreen.isShiftKeyDown()) {
            Secure.invalidateCache();
        }
    }
}
