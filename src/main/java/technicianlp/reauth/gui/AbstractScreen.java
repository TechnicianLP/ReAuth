package technicianlp.reauth.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TranslationTextComponent;

abstract class AbstractScreen extends Screen {

    static final int BUTTON_WIDTH = 196;

    private final String title;

    protected int baseX;
    protected int centerX;
    protected int baseY;
    protected int centerY;
    protected int screenWidth = 300;
    protected int screenHeight = 175;

    AbstractScreen(String title) {
        super(new TranslationTextComponent("reauth.gui.auth.title"));
        this.title = title;
    }

    @Override
    public void init() {
        super.init();
        this.getMinecraft().keyboardListener.enableRepeatEvents(true);

        this.centerX = this.width / 2;
        this.baseX = this.centerX - this.screenWidth / 2;
        this.centerY = this.height / 2;
        this.baseY = this.centerY - this.screenHeight / 2;

        Button cancel = new Button(this.centerX + this.screenWidth / 2 - 22, this.baseY + 2, 20, 20, new TranslationTextComponent("reauth.gui.close"), (b) -> this.closeScreen());
        this.addButton(cancel);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.fillGradient(matrices, 0, 0, this.width, this.height, 0xc0101010, 0xd0101010);

        // modified renderDirtBackground(0);
        this.getMinecraft().getTextureManager().bindTexture(BACKGROUND_LOCATION);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(this.baseX, this.baseY + this.screenHeight, 0.0D).tex(0.0F, this.screenHeight / 32.0F).color(80, 80, 80, 255).endVertex();
        bufferbuilder.pos(this.baseX + this.screenWidth, this.baseY + this.screenHeight, 0.0D).tex(this.screenWidth / 32.0F, this.screenHeight / 32.0F).color(80, 80, 80, 255).endVertex();
        bufferbuilder.pos(this.baseX + this.screenWidth, this.baseY, 0.0D).tex(this.screenWidth / 32.0F, 0F).color(80, 80, 80, 255).endVertex();
        bufferbuilder.pos(this.baseX, this.baseY, 0.0D).tex(0.0F, 0F).color(80, 80, 80, 255).endVertex();
        tessellator.draw();

        super.render(matrices, mouseX, mouseY, partialTicks);

        this.font.drawStringWithShadow(matrices, I18n.format(this.title), this.centerX - (BUTTON_WIDTH / 2f), this.baseY + 8, 0xFFFFFFFF);
    }

    protected final void transitionScreen(Screen newScreen) {
        this.getMinecraft().popGuiLayer();
        this.getMinecraft().pushGuiLayer(newScreen);
    }

    protected void requestClose(boolean completely) {
        if (completely) {
            this.getMinecraft().popGuiLayer();
        } else {
            this.transitionScreen(new MainScreen());
        }
    }

    /**
     * Method called to request this Screen to close itself
     */
    @Override
    public final void closeScreen() {
        this.requestClose(false);
    }

    /**
     * Called once this Screen is closed
     */
    @Override
    public void onClose() {
        super.onClose();
        this.getMinecraft().keyboardListener.enableRepeatEvents(false);
    }
}
