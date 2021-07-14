package technicianlp.reauth.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import technicianlp.reauth.ReAuth;
import technicianlp.reauth.gui.AuthScreen;
import technicianlp.reauth.mixinUtil.DisconnectUtil;
import technicianlp.reauth.mixinUtil.DisconnectedScreenDuck;

import static technicianlp.reauth.mixinUtil.DisconnectUtil.getTranslationKey;

@Mixin(DisconnectedScreen.class)
public abstract class DisconnectedScreenMixin extends Screen implements DisconnectedScreenDuck {
    protected DisconnectedScreenMixin() {
        super(new LiteralText("Dummy Constructor"));
    }

    @Shadow
    private int reasonHeight;

    @Override
    @Accessor("reason")
    public abstract Text reauthGetReason();

    @Override
    @Accessor("parent")
    public abstract Screen reauthGetParent();

    @Inject(at = @At("TAIL"), method = "init()V")
    private void reauthInit(CallbackInfo info) {
        if ("connect.failed".equals(getTranslationKey(this.getTitle()))) {
            if (getTranslationKey(reauthGetReason()).startsWith("disconnect.loginFailed")) {
                int x = this.width / 2 - 100;
                int y = this.height / 2 + this.reasonHeight / 2;

                String key = DisconnectUtil.canRetryLogin() ? "reauth.retry" : "reauth.retry.disabled";
                Text retryText = new TranslatableText(key, ReAuth.config.getProfile());
                ButtonWidget retryButton = new ButtonWidget(x, y + 25, 200, 20, retryText, b -> {
                    DisconnectUtil.retryLogin();
                });
                if (!DisconnectUtil.canRetryLogin()) {
                    retryButton.active = false;
                }
                this.addDrawableChild(retryButton);
                this.addDrawableChild(new ButtonWidget(x, y + 50, 200, 20, new TranslatableText("reauth.open"), b -> {
                    MinecraftClient.getInstance().openScreen(new AuthScreen(reauthGetParent()));
                }));
            }
        }
    }
}
