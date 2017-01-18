package reauth;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = "reauth", name = "ReAuth", version = "3.4.1", guiFactory = "reauth.GuiFactory", canBeDeactivated = true, clientSideOnly = true, acceptedMinecraftVersions = "[1.8.0,1.8.9]", certificateFingerprint = "cac6b8578b012cf31142c980b01c13ddb795846c")
public class Main {

    static final Logger log = LogManager.getLogger("ReAuth");
    static Configuration config;

    static boolean OfflineModeEnabled;

    @Mod.Instance("reauth")
    static Main main;

    @Mod.Metadata
    static ModMetadata meta;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        MinecraftForge.EVENT_BUS.register(new GuiHandler());
        FMLCommonHandler.instance().bus().register(this);

        //Moved ReAuth config out of /config
        File config = new File(Minecraft.getMinecraft().mcDataDir, ".ReAuth.cfg");
        //new one missing; old one there -> move the file
        if (evt.getSuggestedConfigurationFile().exists() && !config.exists())
            evt.getSuggestedConfigurationFile().renameTo(config);
        //initialize config
        Main.config = new Configuration(config);
        Main.loadConfig();

        Secure.init();
    }

    @SubscribeEvent
    public void onConfigChanged(OnConfigChangedEvent evt) {
        if (evt.modID.equals("reauth")) {
            Main.loadConfig();
        }
    }

    /**
     * (re-)loads config
     */
    private static void loadConfig() {
        Property username = config.get(Configuration.CATEGORY_GENERAL, "username", "", "Your Username");
        Secure.username = username.getString();

        Property password = config.get(Configuration.CATEGORY_GENERAL, "password", "", "Your Password in plaintext if chosen to save to disk");
        Secure.password = password.getString();

        Property offline = config.get(Configuration.CATEGORY_GENERAL, "offlineModeEnabled", false, "Enables play-offline button");
        Main.OfflineModeEnabled = offline.getBoolean();

        Property validator = config.get(Configuration.CATEGORY_GENERAL, "validatorEnabled", true, "Disables the Session Validator");
        GuiHandler.enabled = validator.getBoolean();

        Property bold = config.get(Configuration.CATEGORY_GENERAL, "validatorBold", true, "If the Session-Validator look weird disable this");
        GuiHandler.bold = bold.getBoolean();

        Main.config.save();
    }

    @Mod.EventHandler
    public void securityError(FMLFingerprintViolationEvent event) {
        boolean dev = false;
        if (dev) {
            log.fatal("+-----------------------------------------------------------------------------------+");
            log.fatal("|The Version of ReAuth is not signed! It was modified! Ignoring because of Dev-Mode!|");
            log.fatal("+-----------------------------------------------------------------------------------+");
        } else
            throw new SecurityException("The Version of ReAuth is not signed! It is a modified version!");
    }

}
