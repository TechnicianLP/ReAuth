package technicianlp.reauth.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public class MultiOptionButton extends ButtonWidget {
    static final Identifier SERVER_SELECTION_TEXTURE = new Identifier("textures/gui/server_selection.png");

    private final Screen screen;

    private final IntConsumer onSelect;
    private final List<Text> options;
    private int currentOptionIndex;

    public MultiOptionButton(Screen screen, int x, int y, int width, int height, List<Text> options,
                             IntConsumer onSelect) {
        super(x, y, width, height, options.stream().findFirst().orElse(ScreenTexts.EMPTY), button -> {});
        this.screen = screen;
        this.onSelect = onSelect;
        this.options = new ArrayList<>(options);
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderButton(matrices, mouseX, mouseY, delta);
        if (this.isHovered()) {
            RenderSystem.setShaderTexture(0, SERVER_SELECTION_TEXTURE);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            boolean isDropdownHovered = this.hovered && mouseX > this.x + this.width - 16;
            DrawableHelper.drawTexture(matrices, this.x + this.width - 17, this.y + 6, 64.0f,
                isDropdownHovered ? 52f : 20f, 16, 12, 256, 256);
            if (isDropdownHovered) {
                this.screen.renderTooltip(matrices,
                    Text.of((this.currentOptionIndex + 1) + "/" + this.options.size()),
                    mouseX, mouseY);
            }
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (mouseX > this.x + this.width - 16) {
            this.currentOptionIndex = (this.currentOptionIndex + 1) % this.options.size();
            this.setMessage(this.options.get(this.currentOptionIndex));
        } else {
            this.onSelect.accept(this.currentOptionIndex);
        }
    }
}
