package technicianlp.reauth.mixin;

import com.mojang.authlib.minecraft.UserApiService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.util.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface MinecraftClientMixin {
    @Accessor
    @Mutable
    void setSession(Session session);

    @Accessor
    @Mutable
    void setUserApiService(UserApiService userApiService);

    @Accessor
    @Mutable
    void setSocialInteractionsManager(SocialInteractionsManager manager);
}
