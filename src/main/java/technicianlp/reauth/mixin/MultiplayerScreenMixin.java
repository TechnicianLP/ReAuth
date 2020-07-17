package technicianlp.reauth.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import technicianlp.reauth.AuthHelper;
import technicianlp.reauth.ReAuth;
import technicianlp.reauth.gui.AuthScreen;

@Mixin(MultiplayerScreen.class)
public class MultiplayerScreenMixin extends Screen {
    protected MultiplayerScreenMixin() {
        super(new LiteralText("Dummy Constructor"));
    }

    @Inject(at = @At("TAIL"), method = "init()V")
    private void reauthInit(CallbackInfo info) {
        if(Screen.hasShiftDown()) {
            ReAuth.auth.getSessionStatus(true);
        }
        addButton(new ButtonWidget(5, 5, 100, 20, new TranslatableText("reauth.gui.button"),
                b -> MinecraftClient.getInstance().openScreen(new AuthScreen(this))));
    }

    @Inject(at = @At("TAIL"), method = "render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V")
    private void reauthRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {
        AuthHelper.SessionStatus state = ReAuth.auth.getSessionStatus(false);
        textRenderer.draw(matrices, I18n.translate(state.getTranslationKey()), 110, 10, 0xFFFFFFFF);
    }
}
