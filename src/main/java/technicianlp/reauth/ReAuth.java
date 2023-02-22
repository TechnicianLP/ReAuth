package technicianlp.reauth;

import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.network.NetworkConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import technicianlp.reauth.configuration.Config;
import technicianlp.reauth.configuration.ProfileList;
import technicianlp.reauth.gui.ButtonFactory;
import technicianlp.reauth.mojangfix.MojangJavaFix;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

@Mod("reauth")
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ReAuth {

    public static final Logger log = LogManager.getLogger("ReAuth");
    public static final ExecutorService executor;

    public static IModInfo modInfo;

    public static final Config config;
    public static final ProfileList profiles;

    public static final BiFunction<String, Object[], String> i18n;

    public static final ButtonFactory buttonFactory;

    static {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            MojangJavaFix.fixMojangJava();

            executor = Executors.newCachedThreadPool(new ReAuthThreadFactory());

            config = new Config();
            profiles = config.getProfileList();
            i18n = I18n::get;

            ButtonFactory factory;
            try {
                factory = new ButtonFactory.ButtonFactoryMc19();
            } catch (ReflectiveOperationException e) {
                log.info("Button constructor not found - using 1.19.3 Builder instead");
                factory = new ButtonFactory.ButtonFactoryMc19_3();
            }
            buttonFactory = factory;
        } else {
            config = null;
            profiles = null;
            executor = null;
            i18n = null;
            buttonFactory = null;
        }
    }

    public ReAuth() {
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        modLoadingContext.registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modLoadingContext.registerConfig(ModConfig.Type.CLIENT, config.getSpec(), "../reauth.toml");
            modInfo = modLoadingContext.getActiveContainer().getModInfo();
        } else {
            log.warn("###############################");
            log.warn("# ReAuth was loaded on Server #");
            log.warn("#     Consider removing it    #");
            log.warn("###############################");
        }
    }

    @SubscribeEvent
    public static void setup(ModConfigEvent event) {
        config.updateConfig(event.getConfig());
    }

    private static final class ReAuthThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final ThreadGroup group = new ThreadGroup("ReAuth");

        @Override
        public Thread newThread(Runnable runnable) {
            Thread t = new Thread(this.group, runnable, "ReAuth-" + this.threadNumber.getAndIncrement());
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
