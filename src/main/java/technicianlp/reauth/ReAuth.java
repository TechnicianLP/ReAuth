package technicianlp.reauth;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLFingerprintViolationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import technicianlp.reauth.configuration.Config;
import technicianlp.reauth.configuration.ProfileList;
import technicianlp.reauth.crypto.Crypto;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Mod(modid = "reauth", name = "ReAuth", version = "4.0.0", canBeDeactivated = true,
        acceptedMinecraftVersions = "[1.7.10]",
        guiFactory = "technicianlp.reauth.gui.GuiFactory",
        certificateFingerprint = "daba0ec4df71b6da841768c49fb873def208a1e3")
public final class ReAuth {

    public static final Logger log = LogManager.getLogger("ReAuth");
    public static final ExecutorService executor;

    public static final VersionChecker versionCheck;

    public static final Config config;
    public static final ProfileList profiles;

    static {
        executor = Executors.newCachedThreadPool(new ReAuthThreadFactory());
        versionCheck = new VersionChecker();

        Path configFile = new File(Minecraft.getMinecraft().mcDataDir, ".ReAuth.cfg").toPath();
        config = new Config(configFile);
        profiles = config.getProfileList();

        Crypto.init();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (FMLLaunchHandler.side() == Side.CLIENT) {
            MinecraftForge.EVENT_BUS.register(this);
            MinecraftForge.EVENT_BUS.register(new EventHandler());
            ReAuth.config.loadConfig();
            executor.submit(versionCheck);
        } else {
            log.warn("###############################");
            log.warn("# ReAuth was loaded on Server #");
            log.warn("#     Consider removing it    #");
            log.warn("###############################");
        }
    }

    @SubscribeEvent
    public void configChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.configID.equals("reauth")) {
            config.loadConfig();
        }
    }

    @Mod.EventHandler
    public void securityError(FMLFingerprintViolationEvent event) {
        if (event.isDirectory) {
            ReAuth.log.warn("+--------------------------------------------------------------+");
            ReAuth.log.warn("| ReAuth is not packaged. ReAuth has likely been tampered with |");
            ReAuth.log.warn("+--------------------------------------------------------------+");
        } else {
            throw new SecurityException("ReAuth is not signed! It was likely modified!");
        }
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
