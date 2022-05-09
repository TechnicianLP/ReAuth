package technicianlp.reauth;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
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

@Mod(modid = "reauth", name = "ReAuth", canBeDeactivated = true, clientSideOnly = true,
        guiFactory = "technicianlp.reauth.gui.GuiFactory",
        updateJSON = "https://raw.githubusercontent.com/TechnicianLP/ReAuth/master/update.json",
        certificateFingerprint = "daba0ec4df71b6da841768c49fb873def208a1e3")
@Mod.EventBusSubscriber(value = Side.CLIENT)
public final class ReAuth {

    public static final Logger log = LogManager.getLogger("ReAuth");
    public static final ExecutorService executor;

    @Mod.Instance("reauth")
    static ReAuth main;

    public static final Config config;
    public static final ProfileList profiles;

    static {
        executor = Executors.newCachedThreadPool(new ReAuthThreadFactory());
        Path configFile = new File(Minecraft.getMinecraft().mcDataDir, ".ReAuth.cfg").toPath();
        config = new Config(configFile);
        profiles = config.getProfileList();
        Crypto.init();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ReAuth.config.loadConfig();
    }

    @SubscribeEvent
    public static void configChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals("reauth")) {
            config.loadConfig();
        }
    }

    @Mod.EventHandler
    public void securityError(FMLFingerprintViolationEvent event) {
        throw new SecurityException("ReAuth is not signed! It was likely modified!");
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
