package technicianlp.reauth;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.resource.language.I18n;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import technicianlp.reauth.configuration.Config;
import technicianlp.reauth.configuration.ProfileList;
import technicianlp.reauth.mojangfix.MojangJavaFix;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

public final class ReAuth implements ClientModInitializer {

    public static final Logger log = LogManager.getLogger("ReAuth");
    public static final ExecutorService executor = Executors.newCachedThreadPool(new ReAuthThreadFactory());
    public static final VersionChecker versionCheck = new VersionChecker();
    public static final Config config = new Config();
    public static ProfileList profiles;
    public static final BiFunction<String, Object[], String> i18n = I18n::translate;
    public static final ModContainer container = FabricLoader.getInstance().getModContainer("reauth").orElse(null);

    @Override
    public void onInitializeClient() {
        MojangJavaFix.fixMojangJava();
        profiles = config.getProfileList();
        versionCheck.runVersionCheck();
        EventHandler.register();
    }

    private static final class ReAuthThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final ThreadGroup group = new ThreadGroup("ReAuth");

        @Override
        public Thread newThread(
            @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter") @NotNull Runnable runnable) {
            Thread t = new Thread(this.group, runnable, "ReAuth-" + this.threadNumber.getAndIncrement());
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
