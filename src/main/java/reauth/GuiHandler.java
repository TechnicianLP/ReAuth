package reauth;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

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
            });
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
            e.gui.drawString(e.gui.mc.fontRendererObj, "Online:", 110, 10, Color.WHITE.getRGB());
            e.gui.drawString(e.gui.mc.fontRendererObj, (bold ? ChatFormatting.BOLD : "") + validText, 145, 10, validColor);
        }
    }

    @SubscribeEvent
    public void action(ActionPerformedEvent.Post e) {
        if ((e.gui instanceof GuiMainMenu || e.gui instanceof GuiMultiplayer) && e.button.id == 17325) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiLogin(Minecraft.getMinecraft().currentScreen));
        }
    }
}
