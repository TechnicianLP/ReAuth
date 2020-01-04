package technicianlp.reauth;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("reauth")
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Main {

    static final Logger log = LogManager.getLogger("ReAuth");
    static final Configuration config;
    static final AuthHelper auth;

    static {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            config = new Configuration();
            auth = new AuthHelper();
        } else {
            config = null;
            auth = null;
        }
    }

    public Main() {
        if (FMLEnvironment.dist == Dist.CLIENT)
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, config.getSpec(), "../reauth.toml");
        else {
            log.warn("#########################################");
            log.warn("#      ReAuth was loaded on Server      #");
            log.warn("# Consider removing it to save some RAM #");
            log.warn("#########################################");
        }
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));

//        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, sc) -> );
    }

    @SubscribeEvent
    public static void setup(ModConfig.ModConfigEvent event) {
        config.setConfig(event.getConfig());
    }

//    @Mod.EventHandler
//    public void securityError(FMLFingerprintViolationEvent event) {
//        log.fatal("+-----------------------------------------------------------------------------------+");// @Replace()
//        log.fatal("|The Version of ReAuth is not signed! It was modified! Ignoring because of Dev-Mode!|");// @Replace()
//        log.fatal("+-----------------------------------------------------------------------------------+");// @Replace()
//        // @Replace(throw new SecurityException("The Version of ReAuth is not signed! It is a modified version!");)
//    }

}
