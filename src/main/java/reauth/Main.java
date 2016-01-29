package reauth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

@Mod(modid = "ReAuth", guiFactory = "reauth.GuiFactory", canBeDeactivated = true, acceptedMinecraftVersions = "[1.7.2]", version = "2.1")
public class Main {

	protected static final Logger log = LogManager.getLogger("ReAuth");
	protected static Configuration config;

	protected static boolean OfflineModeEnabled = false;

	@Mod.Instance("ReAuth")
	Main main;

	@Mod.EventHandler
	public void preinit(FMLPreInitializationEvent evt)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		MinecraftForge.EVENT_BUS.register(new GuiHandler());

		Main.config = new Configuration(evt.getSuggestedConfigurationFile());
		Main.loadConfig();

		Secure.init();
	}

	/** (re-)loads config */
	public static void loadConfig() {
		Property pu = config.get(config.CATEGORY_GENERAL, "username", "", "Your Username");
		Secure.username = pu.getString();
		Property pp = config.get(config.CATEGORY_GENERAL, "password", "",
				"Your Password in plaintext if chosen to save to disk");
		Secure.password = pp.getString();
		Property po = config.get(config.CATEGORY_GENERAL, "offlineModeEnabled", false,
				"Controls wheter a play-offline button is visble in the Re-Login screen");
		Main.OfflineModeEnabled = po.getBoolean(false);
		Main.config.save();
	}

	public static void saveConfig() {
		Property pu = config.get(config.CATEGORY_GENERAL, "username", Secure.username, "Your Username");
		pu.set(Secure.username);
		Property pp = config.get(config.CATEGORY_GENERAL, "password", Secure.password,
				"Your Password in plaintext if chosen to save to disk");
		pp.set(Secure.password);
		Property po = config.get(config.CATEGORY_GENERAL, "offlineModeEnabled", Main.OfflineModeEnabled,
				"Controls wheter a play-offline button is visble in the Re-Login screen");
		po.set(Main.OfflineModeEnabled);
		Main.config.save();
	}

}
