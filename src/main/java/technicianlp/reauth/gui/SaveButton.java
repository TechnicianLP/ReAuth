package technicianlp.reauth.gui;

import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public final class SaveButton extends CheckboxWidget {

    private final ITooltip tooltip;

    public SaveButton(int x, int y, Text title, ITooltip tooltip) {
        super(x, y, 20, 20, title, false);
        this.tooltip = tooltip;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        if (this.isHovered()) {
            this.tooltip.onTooltip(this, matrixStack, mouseX, mouseY);
        }
    }

    public interface ITooltip {
        void onTooltip(SaveButton button, MatrixStack matrixStack, int mouseX, int mouseY);
    }
}
