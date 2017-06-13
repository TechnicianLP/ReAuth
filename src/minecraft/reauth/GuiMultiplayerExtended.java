package reauth;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

class GuiMultiplayerExtended extends GuiMultiplayer {

	private String validText;
	private int validColor;
	private Thread validator;

	static boolean enabled = true;
	static boolean bold = true;

	GuiMultiplayerExtended(GuiScreen par1GuiScreen) {
		super(par1GuiScreen);
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.add(new GuiButton(17325, 5, 5, 100, 20, "Re-Login"));

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

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		super.drawScreen(par1, par2, par3);
		if (!enabled)
			return;
		drawString(fontRenderer, "Online:", 110, 10, Color.WHITE.getRGB());
		drawString(fontRenderer, (bold ? EnumChatFormatting.BOLD : "") + validText, 145, 10, validColor);
	}

	@Override
	protected void actionPerformed(GuiButton b) {
		super.actionPerformed(b);
		if (b.id == 17325) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiLogin(Minecraft.getMinecraft().currentScreen));
		}
	}

}
