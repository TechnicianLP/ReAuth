package reauth;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

@SideOnly(Side.CLIENT)
class GuiPasswordField extends GuiTextField {

	GuiPasswordField(FontRenderer renderer, int posx, int posy, int x, int y) {
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

	String getPW() {
		return super.getText();
	}

}