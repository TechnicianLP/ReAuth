package technicianlp.reauth.mixin;

import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.client.util.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SplashTextResourceSupplier.class)
public interface SplashTextResourceSupplierMixin {
    @Accessor
    @Mutable
    void setSession(Session session);
}
