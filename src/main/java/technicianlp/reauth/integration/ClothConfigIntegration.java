package technicianlp.reauth.integration;


import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import technicianlp.reauth.Configuration;

public final class ClothConfigIntegration {

    public static boolean isAvailable() {
        return FabricLoader.getInstance().isModLoaded("cloth-config2");
    }

    public static Screen getConfigScreen(Screen parent) {
        if(isAvailable()) {
            return AutoConfig.getConfigScreen(Configuration.class, parent).get();
        } else {
            return null;
        }
    }
}
