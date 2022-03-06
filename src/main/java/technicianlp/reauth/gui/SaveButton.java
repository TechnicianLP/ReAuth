package technicianlp.reauth.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.util.text.ITextComponent;

public final class SaveButton extends CheckboxButton {

    private final ITooltip tooltip;

    public SaveButton(int x, int y, ITextComponent title, ITooltip tooltip) {
        super(x, y, 20, 20, title, false);
        this.tooltip = tooltip;
    }

    @Override
    public final void renderWidget(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(matrixStack, mouseX, mouseY, partialTicks);
        if (this.isHovered()) {
            this.tooltip.onTooltip(this, matrixStack, mouseX, mouseY);
        }
    }

    public interface ITooltip {
        void onTooltip(CheckboxButton button, MatrixStack matrixStack, int mouseX, int mouseY);
    }
}
