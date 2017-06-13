package reauth;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLFingerprintViolationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.FMLRelaunchLog;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import org.lwjgl.opengl.Display;

import java.io.File;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

@Mod(modid = "reauth", name = "ReAuth", acceptedMinecraftVersions = "[1.4.7]", version = "3.5.0", certificateFingerprint = "aa395513cd0890f9c69d4229ac5d779667421c85")
public class Main {

    static final Logger log;

    static {
        String name = "ReAuth";
        FMLRelaunchLog.makeLog(name);
        log = Logger.getLogger(name);
    }

    static Configuration config;

    static boolean OfflineModeEnabled = false;

    @Mod.Instance("reauth")
    Main main;

    @Mod.Metadata
    protected static ModMetadata meta;

    @Mod.PreInit
    public void preInit(FMLPreInitializationEvent evt) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InterruptedException {
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

        /* Hacking the Gui ... as no Gui-Events are existent */
        TickRegistry.registerTickHandler(new ITickHandler() {

            Minecraft mc = Minecraft.getMinecraft();

            @Override
            public EnumSet<TickType> ticks() {
                if (mc.currentScreen == null)
                    return EnumSet.noneOf(TickType.class);
                return EnumSet.of(TickType.CLIENT);
            }

            @Override
            public void tickStart(EnumSet<TickType> type, Object... tickData) {
                GuiScreen gui = mc.currentScreen;
                if (gui instanceof GuiMultiplayer && !(gui instanceof GuiMultiplayerExtended)) {
                    GuiScreen gc = ReflectionHelper.getPrivateValue(GuiMultiplayer.class, (GuiMultiplayer) gui, "parentScreen", "c");
                    mc.displayGuiScreen(new GuiMultiplayerExtended(gc));
                    if (VersionChecker.shouldRun())
                        VersionChecker.update();
                }
            }

            @Override
            public void tickEnd(EnumSet<TickType> type, Object... tickData) {
            }

            @Override
            public String getLabel() {
                return "ReAuth";
            }
        }, Side.CLIENT);
    }

    /**
     * checks if the required libraries are installed
     */
    private void checkDependencies() throws InterruptedException {
        boolean l4j = true;
        boolean l4jc = true;
        boolean gson = true;
        boolean al = true;
        boolean commonslang = true;
        boolean commonsio = true;

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
            Class.forName("com.google.gson.Gson");
            log.log(Level.INFO, "Gson found!");
            gson = false;
        } catch (ClassNotFoundException e) {
            log.log(Level.INFO, "Gson missing!");
        }
        try {
            Class.forName("org.apache.commons.lang3.Validate");
            log.log(Level.INFO, "Commons-Lang found!");
            commonslang = false;
        } catch (ClassNotFoundException e) {
            log.log(Level.INFO, "Commons-Lang missing!");
        }
        try {
            Class.forName("org.apache.commons.io.Charsets");
            log.log(Level.INFO, "Commons-IO found!");
            commonsio = false;
        } catch (ClassNotFoundException e) {
            log.log(Level.INFO, "Commons-IO missing!");
        }
        try {
            Class.forName("com.mojang.authlib.Agent");
            log.log(Level.INFO, "Authlib found!");
            al = false;
        } catch (ClassNotFoundException e) {
            log.log(Level.INFO, "Authlib missing!");
        }

        if (l4j || l4jc || gson || commonslang || commonsio || al) {
            log.log(Level.SEVERE, "o----------------------------------------------o");
            log.log(Level.SEVERE, "|                    ReAuth                    |");
            log.log(Level.SEVERE, "o----------------------------------------------o");
            log.log(Level.SEVERE, "| This Mod requires additional Files to work!  |");
            log.log(Level.SEVERE, "| Download and drop them into your mods folder |");
            log.log(Level.SEVERE, "o----------------------------------------------o");
            if (l4j)
                log.log(Level.SEVERE, "| https://libraries.minecraft.net/org/apache/logging/log4j/log4j-api/2.0-beta9/log4j-api-2.0-beta9.jar");
            if (l4jc)
                log.log(Level.SEVERE, "| https://libraries.minecraft.net/org/apache/logging/log4j/log4j-core/2.0-beta9/log4j-core-2.0-beta9.jar");
            if (gson)
                log.log(Level.SEVERE, "| https://libraries.minecraft.net/com/google/code/gson/gson/2.2.4/gson-2.2.4.jar");
            if (commonslang)
                log.log(Level.SEVERE, "| https://libraries.minecraft.net/org/apache/commons/commons-lang3/3.3.2/commons-lang3-3.3.2.jar");
            if (commonsio)
                log.log(Level.SEVERE, "| https://libraries.minecraft.net/commons-io/commons-io/2.4/commons-io-2.4.jar");
            if (al)
                log.log(Level.SEVERE, "| https://libraries.minecraft.net/com/mojang/authlib/1.3/authlib-1.3.jar");
            log.log(Level.SEVERE, "o----------------------------------------------o");
            Display.destroy();
            System.exit(1);
        }

    }

    /**
     * (re-)loads config
     */
    private static void loadConfig() {
        Property username = config.get(Configuration.CATEGORY_GENERAL, "username", "", "Your Username");
        Secure.username = username.value;

        Property password = config.get(Configuration.CATEGORY_GENERAL, "password", "", "Your Password in plaintext if chosen to save to disk");
        Secure.password = password.value;

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
        username.value = Secure.username;

        Property password = config.get(Configuration.CATEGORY_GENERAL, "password", "", "Your Password in plaintext if chosen to save to disk");
        password.value = Secure.password;

        Property offline = config.get(Configuration.CATEGORY_GENERAL, "offlineModeEnabled", false, "Enables play-offline button");
        offline.value = Boolean.toString(Main.OfflineModeEnabled);

        Property validator = config.get(Configuration.CATEGORY_GENERAL, "validatorEnabled", true, "Disables the Session Validator");
        validator.value = Boolean.toString(GuiMultiplayerExtended.enabled);

        Property bold = config.get(Configuration.CATEGORY_GENERAL, "validatorBold", true, "If the Session-Validator look weird disable this");
        bold.value = Boolean.toString(GuiMultiplayerExtended.bold);

        Main.config.save();
    }

    @Mod.FingerprintWarning
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
