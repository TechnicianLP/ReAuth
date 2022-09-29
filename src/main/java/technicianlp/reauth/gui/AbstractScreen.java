package technicianlp.reauth.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

abstract class AbstractScreen extends Screen {

    static final int BUTTON_WIDTH = 196;

    private final Screen parent;

    private final CompletableFuture<Boolean> closed = new CompletableFuture<>();

    protected int baseX;
    protected int centerX;
    protected int baseY;
    protected int centerY;
    protected static final int screenWidth = 300;
    protected static final int screenHeight = 175;

    AbstractScreen(String title) {
        this(title, MinecraftClient.getInstance().currentScreen);
    }

    AbstractScreen(String title, Screen parent) {
        super(Text.translatable(title));
        this.parent = parent;
    }

    @Override
    public void init() {
        super.init();
        Objects.requireNonNull(this.client).keyboard.setRepeatEvents(true);

        this.centerX = this.width / 2;
        this.baseX = this.centerX - screenWidth / 2;
        this.centerY = this.height / 2;
        this.baseY = this.centerY - screenHeight / 2;

        ButtonWidget cancel = new ButtonWidget(this.centerX + screenWidth / 2 - 22, this.baseY + 2, 20, 20,
            Text.translatable("reauth.gui.close"), button -> this.close());
        this.addDrawableChild(cancel);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.closed.isDone()) {
            try {
                this.requestClose(this.closed.get());
            } catch (InterruptedException | ExecutionException e) {
                this.requestClose(true);
            }
            return;
        }

        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        drawCenteredText(matrices, this.textRenderer, this.title, this.centerX, this.baseY + 8, 0xFFFFFF);
    }

    protected final void transitionScreen(Screen newScreen) {
        Objects.requireNonNull(this.client).setScreen(newScreen);
    }

    protected void requestClose(boolean completely) {
        if (!MinecraftClient.getInstance().isOnThread()) {
            this.closed.complete(completely);
            return;
        }

        Screen parent = this.parent;
        if (completely) {
            while (parent instanceof AbstractScreen abstractScreen) {
                parent = abstractScreen.parent;
            }
        }
        this.transitionScreen(parent);
    }

    /**
     * Method called to request this Screen to close itself
     */
    @Override
    public final void close() {
        this.requestClose(false);
    }

    /**
     * Called once this Screen is closed
     */
    @Override
    public void removed() {
        super.removed();
        Objects.requireNonNull(this.client).keyboard.setRepeatEvents(false);
    }
}
