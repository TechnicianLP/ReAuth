package technicianlp.reauth.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

abstract class AbstractScreen extends GuiScreen {

    static final int BUTTON_WIDTH = 196;

    private final String title;

    protected int baseX;
    protected int centerX;
    protected int baseY;
    protected int centerY;
    protected final int screenWidth = 300;
    protected final int screenHeight = 175;

    protected final GuiScreen background;

    AbstractScreen(String title, GuiScreen background) {
        this.title = title;
        this.background = background;
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);

        this.centerX = this.width / 2;
        this.baseX = this.centerX - this.screenWidth / 2;
        this.centerY = this.height / 2;
        this.baseY = this.centerY - this.screenHeight / 2;

        GuiButton cancel = new GuiButton(1, this.centerX + this.screenWidth / 2 - 22, this.baseY + 2, 20, 20, I18n.format("reauth.gui.close"));
        this.buttonList.add(cancel);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.background.drawScreen(Integer.MAX_VALUE, Integer.MAX_VALUE, partialTicks);
        this.drawGradientRect(0, 0, this.width, this.height, 0xc0101010, 0xd0101010);

        // modified renderDirtBackground(0);
        this.mc.getTextureManager().bindTexture(optionsBackground);
        GL11.glColor4f(80F / 256F, 80F / 256F, 80F / 256F, 1F);
        GuiScreen.drawModalRectWithCustomSizedTexture(this.baseX, this.baseY, 0, 0, this.screenWidth, this.screenHeight, 32, 32);
        GL11.glColor4f(1F, 1F, 1F, 1F);

        super.drawScreen(mouseX, mouseY, partialTicks);

        this.fontRendererObj.drawStringWithShadow(I18n.format(this.title), this.centerX - (BUTTON_WIDTH / 2), this.baseY + 8, 0xFFFFFFFF);
    }

    protected void requestClose(boolean completely) {
        if (completely) {
            this.mc.displayGuiScreen(this.background);
        } else {
            this.mc.displayGuiScreen(new MainScreen(this.background));
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) {
            this.requestClose(false);
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
        if (button.id == 1) {
            this.requestClose(false);
        }
    }
}
