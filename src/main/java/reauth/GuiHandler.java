package reauth;

import java.awt.Color;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;

public class GuiHandler {

    private String validText;
    private int validColor;
    private Thread validator;

    static boolean enabled = true;
    static boolean bold = true;

    @SubscribeEvent
    public void open(InitGuiEvent.Post e) {
        if (e.gui instanceof GuiMultiplayer) {
            e.buttonList.add(new GuiButton(17325, 5, 5, 100, 20, "Re-Login"));

            if (!enabled)
                return;

            validText = "?";
            validColor = Color.GRAY.getRGB();

            if (this.validator != null)
                validator.interrupt();
            validator = new Thread(new Runnable() {
                public void run() {
                    if (Secure.SessionValid()) {
                        validText = "\u2714";
                        validColor = Color.GREEN.getRGB();
                    } else {
                        validText = "\u2718";
                        validColor = Color.RED.getRGB();
                    }
                }
            }, "Session-Validator");
            validator.start();
        }

        if (e.gui instanceof GuiMainMenu) {
            //Support for Custom Main Menu
            e.buttonList.add(new GuiButton(17325, -50, -50, 20, 20, "ReAuth"));
        }

        if (e.gui instanceof GuiMultiplayer || e.gui instanceof GuiMainMenu)
            if (VersionChecker.shouldRun())
                VersionChecker.update();
    }

    @SubscribeEvent
    public void draw(DrawScreenEvent.Post e) {
        if (e.gui instanceof GuiMultiplayer) {
            if (!enabled)
                return;
            e.gui.drawString(e.gui.mc.fontRenderer, "Online:", 110, 10, Color.WHITE.getRGB());
            e.gui.drawString(e.gui.mc.fontRenderer, (bold ? EnumChatFormatting.BOLD : "") + validText, 145, 10, validColor);
        }
    }

    @SubscribeEvent
    public void action(ActionPerformedEvent.Post e) {
        if ((e.gui instanceof GuiMultiplayer || e.gui instanceof GuiMainMenu) && e.button.id == 17325) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiLogin(Minecraft.getMinecraft().currentScreen));
        }
    }
}
