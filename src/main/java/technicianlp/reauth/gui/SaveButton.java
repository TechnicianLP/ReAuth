package technicianlp.reauth.gui;

import cpw.mods.fml.client.config.GuiCheckBox;
import net.minecraft.client.Minecraft;

public final class SaveButton extends GuiCheckBox {

    private final ITooltip tooltip;

    public SaveButton(int id, int x, int y, String title, ITooltip tooltip) {
        super(id, x, y, title, false);
        this.tooltip = tooltip;
    }

    @Override
    public final void drawButton(Minecraft mc, int mouseX, int mouseY) {
        super.drawButton(mc, mouseX, mouseY);
    }

    public void drawTooltip(int mouseX, int mouseY) {
        if (this.hovered) {
            this.tooltip.renderTooltip(mouseX, mouseY);
        }
    }

    public interface ITooltip {
        void renderTooltip(int mouseX, int mouseY);
    }
}
