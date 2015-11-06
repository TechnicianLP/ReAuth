package reauth;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ChatAllowedCharacters;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiPasswordField extends GuiTextField {

	public GuiPasswordField(FontRenderer renderer, int posx, int posy, int x, int y) {
		super(renderer, posx, posy, x, y);
	}

	public void drawTextBox() {
		String s = this.getPW();
		setText(this.getText());
		super.drawTextBox();
		this.setText(s);
	}

	@Override
	public String getText() {
		return super.getText().replaceAll(".", "\u25CF");
	}

	public String getPW() {
		return super.getText();
	}

}