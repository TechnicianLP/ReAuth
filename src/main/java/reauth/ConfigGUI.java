package reauth;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class ConfigGUI extends GuiScreen {

	private GuiScreen prev;

	private GuiTextField username;
	private GuiTextField password;
	private GuiCheckBox offline;
	private GuiButton ok;
	private GuiButton cancel;
	
	private GuiCheckBox validatorBold;
	private GuiCheckBox validatorEnabled;

	public ConfigGUI(GuiScreen g) {
		prev = g;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();

		int y = this.height / 2;

		username = new GuiTextField(fontRendererObj, width / 2 - 100, y - 30, 255, 10);
		username.setText(Secure.username);

		password = new GuiTextField(fontRendererObj, width / 2 - 100, y - 15, 255, 10);
		password.setText(Secure.password);

		offline = new GuiCheckBox(1, width / 2 - 155, y, "Enable the Play-Offline Button", Main.OfflineModeEnabled);
		this.buttonList.add(offline);

		ok = new GuiButton(2, width / 2 - 155, height - 40, 155, 20, "Save");
		this.buttonList.add(ok);

		cancel = new GuiButton(3, width / 2, height - 40, 155, 20, "Cancel");
		this.buttonList.add(cancel);
		
		validatorBold = new GuiCheckBox(4, width / 2 - 155, y + 15, "Session-Validator Bold", GuiHandler.bold);
		this.buttonList.add(validatorBold);
		
		validatorEnabled = new GuiCheckBox(5, width / 2 - 155, y + 30, "Enable Session-Validator", GuiHandler.enabled);
		this.buttonList.add(validatorEnabled);


	}

	@Override
	protected void actionPerformed(GuiButton b) {
		switch (b.id) {
		case 2:
			Secure.username = username.getText();
			Secure.password = password.getText();
			Main.OfflineModeEnabled = offline.isChecked();
			GuiHandler.bold = validatorBold.isChecked();
			GuiHandler.enabled = validatorEnabled.isChecked();
			Main.saveConfig();
		case 3:
			this.mc.displayGuiScreen(prev);
			break;
		}
		super.actionPerformed(b);
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();

		int y = this.height / 2;

		this.drawString(fontRendererObj, "Username:", width / 2 - 155, y - 30, Color.WHITE.getRGB());
		this.drawString(fontRendererObj, "Password:", width / 2 - 155, y - 15, Color.WHITE.getRGB());

		GL11.glScalef(2f, 2f, 1f);
		this.drawCenteredString(fontRendererObj, "Config for ReAuth", width / 4, 15, Color.WHITE.getRGB());
		GL11.glScalef(0.5f, 0.5f, 1f);

		username.drawTextBox();
		password.drawTextBox();

		super.drawScreen(par1, par2, par3);
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		username.textboxKeyTyped(par1, par2);
		password.textboxKeyTyped(par1, par2);
		super.keyTyped(par1, par2);
	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3) {
		username.mouseClicked(par1, par2, par3);
		password.mouseClicked(par1, par2, par3);
		super.mouseClicked(par1, par2, par3);
	}
}
