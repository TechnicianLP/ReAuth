package technicianlp.reauth.gui;

import net.minecraft.client.gui.widget.button.CheckboxButton;

public final class SaveButton extends CheckboxButton {

    private final ITooltip tooltip;

    public SaveButton(int x, int y, String title, ITooltip tooltip) {
        super(x, y, 20, 20, title, false);
        this.tooltip = tooltip;
    }

    @Override
    public final void renderButton(int mouseX, int mouseY, float partialTicks) {
        super.renderButton(mouseX, mouseY, partialTicks);
        if (this.isHovered()) {
            this.tooltip.onTooltip(this, mouseX, mouseY);
        }
    }

    public interface ITooltip {
        void onTooltip(CheckboxButton button, int mouseX, int mouseY);
    }
}
