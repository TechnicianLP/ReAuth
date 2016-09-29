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

	public static boolean enabled = true;
	public static boolean bold = true;

	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public void ongui(InitGuiEvent.Post e) {
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

		if (e.gui instanceof GuiMultiplayer || e.gui instanceof GuiMainMenu)
			if (VersionChecker.shouldRun())
				VersionChecker.update();
	}

	@SubscribeEvent
	public void ongui(DrawScreenEvent.Post e) {
		if (e.gui instanceof GuiMultiplayer) {
			if (!enabled)
				return;
			e.gui.drawString(e.gui.mc.fontRenderer, "Online:", 110, 10, Color.WHITE.getRGB());
			e.gui.drawString(e.gui.mc.fontRenderer, (bold ? EnumChatFormatting.BOLD : "")  + validText, 145, 10, validColor);
		}
	}

	@SubscribeEvent
	public void ongui(ActionPerformedEvent.Post e) {
		if (e.gui instanceof GuiMultiplayer && e.button.id == 17325) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiLogin(Minecraft.getMinecraft().currentScreen));
		}
	}
}
