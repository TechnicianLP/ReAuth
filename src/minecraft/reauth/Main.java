package reauth;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLFingerprintViolationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import net.minecraftforge.event.ForgeSubscribe;

@Mod(modid = "reauth", name = "ReAuth", acceptedMinecraftVersions = "[1.6.4]", version = "3.4", certificateFingerprint = "cac6b8578b012cf31142c980b01c13ddb795846c")
public class Main {

    static final Logger log = Logger.getLogger("ReAuth");
    static Configuration config;

    static boolean OfflineModeEnabled = false;

    @Mod.Instance("reauth")
    Main main;

    @Mod.Metadata
    protected static ModMetadata meta;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        checkDependencies();

        MinecraftForge.EVENT_BUS.register(this);

        //Moved ReAuth config out of /config
        File config = new File(Minecraft.getMinecraft().mcDataDir, ".ReAuth.cfg");
        //new one missing; old one there -> move the file
        if (evt.getSuggestedConfigurationFile().exists() && !config.exists())
            evt.getSuggestedConfigurationFile().renameTo(config);
        //initialize config
        Main.config = new Configuration(config);
        Main.loadConfig();

        Secure.init();
        Secure.fixSession();
    }

    @ForgeSubscribe
    public void openGui(GuiOpenEvent e) {
        if (e.gui instanceof GuiMultiplayer || e.gui instanceof GuiMainMenu)
            if (VersionChecker.shouldRun())
                VersionChecker.update();

        if (e.gui instanceof GuiMultiplayer) {
            e.gui = new GuiMultiplayerExtended(((GuiMultiplayer) e.gui).parentScreen);
        }
    }

    /**
     * checks if the required libraries are installed
     */
    private void checkDependencies() {
        boolean l4j = true;
        boolean l4jc = true;
        boolean al = true;

        try {
            Class.forName("org.apache.logging.log4j.Logger");
            log.log(Level.INFO, "Log4J found!");
            l4j = false;
        } catch (ClassNotFoundException e) {
            log.log(Level.INFO, "Log4J missing!");
        }
        try {
            Class.forName("org.apache.logging.log4j.core.Logger");
            log.log(Level.INFO, "Log4J-Core found!");
            l4jc = false;
        } catch (ClassNotFoundException e) {
            log.log(Level.INFO, "Log4J-Core missing!");
        }

        try {
            Class.forName("com.mojang.authlib.Agent");
            log.log(Level.INFO, "Authlib found!");
            al = false;
        } catch (ClassNotFoundException e) {
            log.log(Level.INFO, "Authlib missing!");
        }

        if (l4j || l4jc || al) {
            log.log(Level.SEVERE, "o----------------------------------------------o");
            log.log(Level.SEVERE, "|                    ReAuth                    |");
            log.log(Level.SEVERE, "o----------------------------------------------o");
            log.log(Level.SEVERE, "| This Mod requires additional Files to work!  |");
            log.log(Level.SEVERE, "| Download and drop them into your mods folder |");
            log.log(Level.SEVERE, "o----------------------------------------------o");
            if (al)
                log.log(Level.SEVERE, "| https://libraries.minecraft.net/com/mojang/authlib/1.3/authlib-1.3.jar");
            if (l4j)
                log.log(Level.SEVERE, "| https://libraries.minecraft.net/org/apache/logging/log4j/log4j-api/2.0-beta9/log4j-api-2.0-beta9.jar");
            if (l4jc)
                log.log(Level.SEVERE, "| https://libraries.minecraft.net/org/apache/logging/log4j/log4j-core/2.0-beta9/log4j-core-2.0-beta9.jar");
            log.log(Level.SEVERE, "o----------------------------------------------o");
            System.exit(1);
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
        Main.OfflineModeEnabled = offline.getBoolean(false);

        Property validator = config.get(Configuration.CATEGORY_GENERAL, "validatorEnabled", true, "Disables the Session Validator");
        GuiMultiplayerExtended.enabled = validator.getBoolean(true);

        Property bold = config.get(Configuration.CATEGORY_GENERAL, "validatorBold", true, "If the Session-Validator look weird disable this");
        GuiMultiplayerExtended.bold = bold.getBoolean(true);

        Main.config.save();
    }

    static void saveConfig() {
        Property username = config.get(Configuration.CATEGORY_GENERAL, "username", "", "Your Username");
        username.set(Secure.username);

        Property password = config.get(Configuration.CATEGORY_GENERAL, "password", "", "Your Password in plaintext if chosen to save to disk");
        password.set(Secure.password);

        Property offline = config.get(Configuration.CATEGORY_GENERAL, "offlineModeEnabled", false, "Enables play-offline button");
        offline.set(Main.OfflineModeEnabled);

        Property validator = config.get(Configuration.CATEGORY_GENERAL, "validatorEnabled", true, "Disables the Session Validator");
        validator.set(GuiMultiplayerExtended.enabled);

        Property bold = config.get(Configuration.CATEGORY_GENERAL, "validatorBold", true, "If the Session-Validator look weird disable this");
        bold.set(GuiMultiplayerExtended.bold);

        Main.config.save();
    }

    @Mod.EventHandler
    public void securityError(FMLFingerprintViolationEvent event) {
        boolean dev = false;
        if (dev) {
            log.log(Level.SEVERE, "+-----------------------------------------------------------------------------------+");
            log.log(Level.SEVERE, "|The Version of ReAuth is not signed! It was modified! Ignoring because of Dev-Mode!|");
            log.log(Level.SEVERE, "+-----------------------------------------------------------------------------------+");
        } else
            throw new SecurityException("The Version of ReAuth is not signed! It is a modified version!");
    }

}
