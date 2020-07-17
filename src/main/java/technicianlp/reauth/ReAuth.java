package technicianlp.reauth;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ReAuth implements ModInitializer {

    public static final Logger log = LogManager.getLogger("ReAuth");
    public static final AuthHelper auth = new AuthHelper();
    public static final VersionChecker versionCheck = new VersionChecker();
    public static ConfigWrapper config = new ConfigWrapper();
    public static ModContainer container;

    @Override
    public void onInitialize() {
        ConfigWrapper.registerConfig();
        container = FabricLoader.getInstance().getModContainer("reauth").orElse(null);
        versionCheck.runVersionCheck();
    }
}
