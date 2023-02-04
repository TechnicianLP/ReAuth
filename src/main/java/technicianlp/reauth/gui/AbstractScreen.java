package technicianlp.reauth.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import technicianlp.reauth.ReAuth;

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
        super(Component.translatable("reauth.gui.auth.title"));
        this.title = title;
    }

    @Override
    public void init() {
        super.init();

        this.centerX = this.width / 2;
        this.baseX = this.centerX - this.screenWidth / 2;
        this.centerY = this.height / 2;
        this.baseY = this.centerY - this.screenHeight / 2;

        Button cancel = ReAuth.button(this.centerX + this.screenWidth / 2 - 22, this.baseY + 2, 20, 20, Component.translatable("reauth.gui.close"), (b) -> this.onClose());
        this.addRenderableWidget(cancel);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.fillGradient(poseStack, 0, 0, this.width, this.height, 0xc0101010, 0xd0101010);

        // modified renderDirtBackground(0);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, BACKGROUND_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(this.baseX, this.baseY + this.screenHeight, 0.0D).uv(0.0F, this.screenHeight / 32.0F).color(80, 80, 80, 255).endVertex();
        bufferbuilder.vertex(this.baseX + this.screenWidth, this.baseY + this.screenHeight, 0.0D).uv(this.screenWidth / 32.0F, this.screenHeight / 32.0F).color(80, 80, 80, 255).endVertex();
        bufferbuilder.vertex(this.baseX + this.screenWidth, this.baseY, 0.0D).uv(this.screenWidth / 32.0F, 0F).color(80, 80, 80, 255).endVertex();
        bufferbuilder.vertex(this.baseX, this.baseY, 0.0D).uv(0.0F, 0F).color(80, 80, 80, 255).endVertex();
        tesselator.end();

        super.render(poseStack, mouseX, mouseY, partialTicks);

        this.font.drawShadow(poseStack, I18n.get(this.title), this.centerX - (BUTTON_WIDTH / 2f), this.baseY + 8, 0xFFFFFFFF);
    }

    protected final void transitionScreen(Screen newScreen) {
        this.getMinecraft().popGuiLayer();
        this.getMinecraft().pushGuiLayer(newScreen);
    }

    protected void requestClose(boolean completely) {
        if (completely) {
            super.onClose();
        } else {
            this.transitionScreen(new MainScreen());
        }
    }

    /**
     * Method called to request this Screen to close itself
     */
    @Override
    public final void onClose() {
        this.requestClose(false);
    }

}
