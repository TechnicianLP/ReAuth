package reauth;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/**
 * This class provides a checkbox style control. <br>
 * Backported from 1.7.10
 * 
 * @author bspkrs
 */
public class GuiCheckBox extends GuiButton {
	private boolean isChecked;
	private int boxWidth;

	public GuiCheckBox(int id, int xPos, int yPos, String displayString, boolean isChecked) {
		super(id, xPos, yPos, displayString);
		this.isChecked = isChecked;
		this.boxWidth = 11;
		this.height = 11;
		this.width = this.boxWidth + 2 + Minecraft.getMinecraft().fontRenderer.getStringWidth(displayString);
	}

	/**
	 * Draws this button to the screen.
	 */
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if (this.visible) {
			this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition
					&& mouseX < this.xPosition + this.boxWidth && mouseY < this.yPosition + this.height;

			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(buttonTextures);
			this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46, 11, 11);
			this.drawTexturedModalRect(this.xPosition, this.yPosition + 10, 0, 46, 11, 1);
			this.drawTexturedModalRect(this.xPosition + 10, this.yPosition, 0, 46, 1, 11);

			this.mouseDragged(mc, mouseX, mouseY);
			int color = 14737632;

			if (packedFGColour != 0) {
				color = packedFGColour;
			} else if (!this.enabled) {
				color = 10526880;
			}

			if (this.isChecked)
				this.drawCenteredString(mc.fontRenderer, "x", this.xPosition + this.boxWidth / 2 + 1,
						this.yPosition + 1, 14737632);

			this.drawString(mc.fontRenderer, displayString, xPosition + this.boxWidth + 2, yPosition + 2, color);
		}
	}

	/**
	 * Returns true if the mouse has been pressed on this control. Equivalent of
	 * MouseListener.mousePressed(MouseEvent e).
	 */
	@Override
	public boolean mousePressed(Minecraft p_146116_1_, int p_146116_2_, int p_146116_3_) {
		if (this.enabled && this.visible && p_146116_2_ >= this.xPosition && p_146116_3_ >= this.yPosition
				&& p_146116_2_ < this.xPosition + this.width && p_146116_3_ < this.yPosition + this.height) {
			this.isChecked = !this.isChecked;
			return true;
		}

		return false;
	}

	public boolean isChecked() {
		return this.isChecked;
	}

	public void setIsChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
}