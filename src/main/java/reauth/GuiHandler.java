package reauth;

import java.awt.Color;

import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GuiHandler {

	private String validText;
	private int validColor;
	private Thread validator;

	public static boolean enabled = true;
	public static boolean bold = true;

	@SubscribeEvent
	public void ongui(InitGuiEvent.Post e) {
		if (e.getGui() instanceof GuiMultiplayer) {
			e.getButtonList().add(new GuiButton(17325, 5, 5, 100, 20, "Re-Login"));

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

		if (e.getGui() instanceof GuiMultiplayer || e.getGui() instanceof GuiMainMenu)
			if (VersionChecker.shouldRun())
				VersionChecker.update();
	}

	@SubscribeEvent
	public void ongui(DrawScreenEvent.Post e) {
		if (e.getGui() instanceof GuiMultiplayer) {
			if (!enabled)
				return;
			e.getGui().drawString(e.getGui().mc.fontRendererObj, "Online:", 110, 10, Color.WHITE.getRGB());
			e.getGui().drawString(e.getGui().mc.fontRendererObj, (bold ? ChatFormatting.BOLD : "") + validText, 145, 10, validColor);
		}
	}

	@SubscribeEvent
	public void ongui(ActionPerformedEvent.Post e) {
		if (e.getGui() instanceof GuiMultiplayer && e.getButton().id == 17325) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiLogin(Minecraft.getMinecraft().currentScreen));
		}
	}
}
