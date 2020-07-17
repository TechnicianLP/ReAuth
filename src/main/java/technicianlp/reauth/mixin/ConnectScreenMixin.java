package technicianlp.reauth.mixin;

import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.ClientConnection;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import technicianlp.reauth.mixinUtil.ConnectScreenDuck;
import technicianlp.reauth.mixinUtil.DisconnectUtil;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenMixin extends Screen implements ConnectScreenDuck {
    protected ConnectScreenMixin() {
        super(new LiteralText("Dummy Constructor"));
    }

    @Override
    @Accessor("connection")
    public abstract ClientConnection reauthGetConnection();

    @Override
    @Accessor("parent")
    public abstract Screen reauthGetParent();

    @Inject(at = @At("TAIL"), method = "init()V")
    private void reauthInit(CallbackInfo info) {
        DisconnectUtil.setConnectScreen(this);
    }
}
