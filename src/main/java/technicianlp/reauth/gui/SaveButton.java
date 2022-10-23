package technicianlp.reauth.gui;

import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public final class SaveButton extends CheckboxWidget {

    private final ITooltip tooltip;

    public SaveButton(int x, int y, Text message, ITooltip tooltip) {
        super(x, y, 20, 20, message, false);
        this.tooltip = tooltip;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderButton(matrices, mouseX, mouseY, delta);
        if (this.isHovered()) {
            this.tooltip.onTooltip(this, matrices, mouseX, mouseY);
        }
    }

    public interface ITooltip {
        void onTooltip(SaveButton button, MatrixStack matrixStack, int mouseX, int mouseY);
    }
}
