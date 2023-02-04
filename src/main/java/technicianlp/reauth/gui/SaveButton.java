package technicianlp.reauth.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;

public final class SaveButton extends Checkbox {

    private final ITooltip tooltip;

    public SaveButton(int x, int y, Component title, ITooltip tooltip) {
        super(x, y, 20, 20, title, false);
        this.tooltip = tooltip;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(poseStack, mouseX, mouseY, partialTicks);
        if (this.isHoveredOrFocused()) {
            this.tooltip.onTooltip(this, poseStack, mouseX, mouseY);
        }
    }

    public interface ITooltip {
        void onTooltip(SaveButton button, PoseStack poseStack, int mouseX, int mouseY);
    }
}
