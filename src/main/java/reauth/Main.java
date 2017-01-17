package reauth;

import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLFingerprintViolationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;

@Mod(modid = "reauth", name = "ReAuth", version = "3.4", guiFactory = "reauth.GuiFactory", canBeDeactivated = true, acceptedMinecraftVersions = "[1.7.2]", certificateFingerprint = "cac6b8578b012cf31142c980b01c13ddb795846c")
public class Main {

    static final Logger log = LogManager.getLogger("ReAuth");
    static Configuration config;

    static boolean OfflineModeEnabled = false;

    @Mod.Instance("reauth")
    Main main;

    @Mod.Metadata
    static ModMetadata meta;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        MinecraftForge.EVENT_BUS.register(new GuiHandler());

        File config = new File(Minecraft.getMinecraft().mcDataDir, ".ReAuth.cfg");
        //new one missing; old one there -> move the file
        if (evt.getSuggestedConfigurationFile().exists() && !config.exists())
            evt.getSuggestedConfigurationFile().renameTo(config);
        //initialize config
        Main.config = new Configuration(config);
        Main.loadConfig();

        Secure.init();
    }

    /**
     * loads config
     */
    private static void loadConfig() {
        Property username = config.get(Configuration.CATEGORY_GENERAL, "username", "", "Your Username");
        Secure.username = username.getString();

        Property password = config.get(Configuration.CATEGORY_GENERAL, "password", "", "Your Password in plaintext if chosen to save to disk");
        Secure.password = password.getString();

        Property offline = config.get(Configuration.CATEGORY_GENERAL, "offlineModeEnabled", false, "Controls wheter a play-offline button is visble in the Re-Login screen");
        Main.OfflineModeEnabled = offline.getBoolean(false);

        Property validator = config.get(Configuration.CATEGORY_GENERAL, "validatorEnabled", true, "Disables the Session Validator");
        GuiHandler.enabled = validator.getBoolean(true);

        Property bold = config.get(Configuration.CATEGORY_GENERAL, "validatorBold", true, "If the Session-Validator look weird disable this");
        GuiHandler.bold = bold.getBoolean(true);

        Main.config.save();
    }

    static void saveConfig() {
        Property username = config.get(Configuration.CATEGORY_GENERAL, "username", Secure.username, "Your Username");
        username.set(Secure.username);

        Property password = config.get(Configuration.CATEGORY_GENERAL, "password", Secure.password, "Your Password in plaintext if chosen to save to disk");
        password.set(Secure.password);

        Property offline = config.get(Configuration.CATEGORY_GENERAL, "offlineModeEnabled", Main.OfflineModeEnabled, "Controls wheter a play-offline button is visble in the Re-Login screen");
        offline.set(Main.OfflineModeEnabled);

        Property validator = config.get(Configuration.CATEGORY_GENERAL, "validatorEnabled", true, "Disables the Session Validator");
        validator.set(GuiHandler.enabled);

        Property bold = config.get(Configuration.CATEGORY_GENERAL, "validatorBold", true, "If the Session-Validator look weird disable this");
        bold.set(GuiHandler.bold);

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
