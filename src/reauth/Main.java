package reauth;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;

import cpw.mods.fml.client.CustomModLoadingErrorDisplayException;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import net.minecraftforge.event.ForgeSubscribe;

@Mod(modid = "ReAuth", acceptedMinecraftVersions = "[1.6.4]", version = "2.1")
public class Main {

	protected static final Logger log = Logger.getLogger("ReAuth");
	protected static Configuration config;

	protected static boolean OfflineModeEnabled = false;

	@Mod.Instance("ReAuth")
	Main main;

	@Mod.EventHandler
	public void preinit(FMLPreInitializationEvent evt) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		checkDependencies();

		MinecraftForge.EVENT_BUS.register(this);

		Main.config = new Configuration(evt.getSuggestedConfigurationFile());
		Main.loadConfig();

		Secure.init();

		Secure.fixSession();
	}

	@ForgeSubscribe
	public void ongui(GuiOpenEvent e) {
		if (e.gui instanceof GuiMultiplayer) {
			e.gui = new GuiMultiplayerExtended(((GuiMultiplayer) e.gui).parentScreen);
		}
	}

	/** checks if the required libraries are installed */
	public void checkDependencies() {
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

	/** (re-)loads config */
	public static void loadConfig() {
		Property pu = config.get(config.CATEGORY_GENERAL, "username", "", "Your Username");
		Secure.username = pu.getString();
		Property pp = config.get(config.CATEGORY_GENERAL, "password", "", "Your Password in plaintext if chosen to save to disk");
		Secure.password = pp.getString();
		Property po = config.get(config.CATEGORY_GENERAL, "offlineModeEnabled", false, "Controls wheter a play-offline button is visble in the Re-Login screen");
		Main.OfflineModeEnabled = po.getBoolean(false);
		Main.config.save();
	}

	public static void saveConfig() {
		Property pu = config.get(config.CATEGORY_GENERAL, "username", Secure.username, "Your Username");
		pu.set(Secure.username);
		Property pp = config.get(config.CATEGORY_GENERAL, "password", Secure.password, "Your Password in plaintext if chosen to save to disk");
		pp.set(Secure.password);
		Property po = config.get(config.CATEGORY_GENERAL, "offlineModeEnabled", Main.OfflineModeEnabled, "Controls wheter a play-offline button is visble in the Re-Login screen");
		po.set(Main.OfflineModeEnabled);
		Main.config.save();
	}

}
