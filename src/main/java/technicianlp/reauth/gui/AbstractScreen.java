package technicianlp.reauth.gui;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.opengl.GL11;

abstract class AbstractScreen extends Screen {

    static final int BUTTON_WIDTH = 196;

    private final String title;

    protected int baseX;
    protected int centerX;
    protected int baseY;
    protected int centerY;
    protected int screenWidth = 300;
    protected int screenHeight = 175;

    protected final Screen background;

    AbstractScreen(String title, Screen background) {
        super(new TranslationTextComponent("reauth.gui.auth.title"));
        this.title = title;
        this.background = background;
    }

    @Override
    public void init() {
        super.init();
        this.getMinecraft().keyboardListener.enableRepeatEvents(true);

        this.centerX = this.width / 2;
        this.baseX = this.centerX - this.screenWidth / 2;
        this.centerY = this.height / 2;
        this.baseY = this.centerY - this.screenHeight / 2;

        Button cancel = new Button(this.centerX + this.screenWidth / 2 - 22, this.baseY + 2, 20, 20, I18n.format("reauth.gui.close"), (b) -> this.onClose());
        this.addButton(cancel);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        background.render(Integer.MAX_VALUE, Integer.MAX_VALUE, partialTicks);
        this.fillGradient(0, 0, this.width, this.height, 0xc0101010, 0xd0101010);

        // modified renderDirtBackground(0);
        this.getMinecraft().getTextureManager().bindTexture(BACKGROUND_LOCATION);
        GL11.glColor4f(80F / 256F, 80F / 256F, 80F / 256F, 1F);
        AbstractGui.blit(this.baseX, this.baseY, this.screenWidth, this.screenHeight, 0F, 0F, this.screenWidth, this.screenHeight, 32, 32);
        GL11.glColor4f(1F, 1F, 1F, 1F);

        super.render(mouseX, mouseY, partialTicks);

        this.font.drawStringWithShadow(I18n.format(this.title), this.centerX - (BUTTON_WIDTH / 2f), this.baseY + 8, 0xFFFFFFFF);
    }

    protected void requestClose(boolean completely) {
        if (completely) {
            this.getMinecraft().displayGuiScreen(this.background);
        } else {
            this.getMinecraft().displayGuiScreen(new MainScreen(this.background));
        }
    }

    /**
     * Method called to request this Screen to close itself
     */
    @Override
    public final void onClose() {
        this.requestClose(false);
    }

    /**
     * Called once this Screen is closed
     */
    @Override
    public void removed() {
        super.removed();
        this.getMinecraft().keyboardListener.enableRepeatEvents(false);
    }
}
