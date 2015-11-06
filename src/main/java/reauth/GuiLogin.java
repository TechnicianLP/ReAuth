package reauth;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import com.mojang.authlib.exceptions.AuthenticationException;

import cpw.mods.fml.client.GuiDupesFound;
import cpw.mods.fml.client.GuiModsMissing;
import cpw.mods.fml.client.GuiOldSaveLoadConfirm;
import cpw.mods.fml.client.config.GuiCheckBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiYesNo;

public class GuiLogin extends GuiScreen {

	GuiTextField username;
	GuiPasswordField pw;
	GuiButton login;
	GuiButton cancel;
	GuiCheckBox save;

	GuiScreen prev;

	int basex;

	String error = "";

	public GuiLogin(GuiScreen prev) {
		this.mc = Minecraft.getMinecraft();
		this.fontRendererObj = mc.fontRenderer;
		this.prev = prev;
	}

	@Override
	protected void actionPerformed(GuiButton b) {
		switch (b.id) {
		case 0:
			if (!login())
				break;
		case 1:
			this.mc.displayGuiScreen(prev);
			break;
		}

	}

	@Override
	public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_) {
		this.drawDefaultBackground();

		this.drawCenteredString(this.fontRendererObj, "Username/E-Mail:", this.width / 2, this.basex, Color.WHITE.getRGB());
		this.drawCenteredString(this.fontRendererObj, "Password:", this.width / 2, this.basex + 45, Color.WHITE.getRGB());
		if (!(this.error.isEmpty() || this.error == null)) {
			int color = this.error.startsWith("E") ? Color.RED.getRGB() : Color.GREEN.getRGB();
			this.drawCenteredString(this.fontRendererObj, this.error.substring(1), this.width / 2, this.basex - 15, color);
		}
		this.username.drawTextBox();
		this.pw.drawTextBox();

		super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		this.username.drawTextBox();
		this.pw.drawTextBox();
	}

	@Override
	public void initGui() {
		super.initGui();
		this.basex = this.height / 2 - 110 / 2;

		this.username = new GuiTextField(this.fontRendererObj, this.width / 2 - 155, this.basex + 15, 2 * 155, 20);
		this.username.setText(Main.username);
		this.pw = new GuiPasswordField(this.fontRendererObj, this.width / 2 - 155, this.basex + 60, 2 * 155, 20);
		this.pw.setMaxStringLength(64);
		this.pw.setText(Main.password);
		this.login = new GuiButton(0, this.width / 2 - 155, this.basex + 105, 155, 20, "Login");
		this.cancel = new GuiButton(1, this.width / 2 + 5, this.basex + 105, 155, 20, "Cancel");
		this.save = new GuiCheckBox(2, this.width / 2 - 155, this.basex + 85, "Save Password to Config (WARNING: SECURITY RISK!)", false);

		this.buttonList.add(this.login);
		this.buttonList.add(this.cancel);
		this.buttonList.add(this.save);

		this.username.setFocused(true);

		if (Main.sessionFound())
			this.error = "SOld Accesstoken found, try to just hit \"Login\"";
	}

	@Override
	protected void keyTyped(char c, int k) {
		super.keyTyped(c, k);
		this.username.textboxKeyTyped(c, k);
		this.pw.textboxKeyTyped(c, k);
		if (k == Keyboard.KEY_TAB) {
			this.username.setFocused(!this.username.isFocused());
			this.pw.setFocused(!this.pw.isFocused());
		} else if (k == Keyboard.KEY_RETURN) {
			if (this.username.isFocused()) {
				this.username.setFocused(false);
				this.pw.setFocused(true);
			} else if (this.pw.isFocused()) {
				this.actionPerformed(this.login);
			}
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int b) {
		super.mouseClicked(x, y, b);
		this.username.mouseClicked(x, y, b);
		this.pw.mouseClicked(x, y, b);
	}

	private boolean login() {
		boolean bol = true;
		try {
			Main.login(this.username.getText(), this.pw.getPW(), this.save.isChecked());
		} catch (AuthenticationException e) {
			this.error = "ELogin failed: " + e.getMessage();
			Main.log.error("Login failed:", e);
			bol = false;
		} catch (Exception e) {
			this.error = "EError: Something went wrong!";
			Main.log.error("Session could not be updated IN YOUR CLIENT because of access restrictions", e);
			bol = false;
		}
		if (bol) {
			this.error = "SLogin successfull!";
		} else {
			if (Main.sessionFound()) {
				Main.ua.logOut();
				this.login();
			}
		}
		return bol;
	}

}
