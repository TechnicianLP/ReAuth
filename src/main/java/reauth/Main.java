package reauth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = "reauth", version = "3.3", guiFactory = "reauth.GuiFactory", canBeDeactivated = true, clientSideOnly = true, acceptedMinecraftVersions = "[1.9,1.10.2]", certificateFingerprint = "cac6b8578b012cf31142c980b01c13ddb795846c")
public final class Main {

	protected static final Logger log = LogManager.getLogger("ReAuth");
	protected static Configuration config;

	protected static boolean OfflineModeEnabled;

	@Mod.Instance("reauth")
	protected static Main main;

	@Mod.Metadata
	protected static ModMetadata meta;

	@Mod.EventHandler
	public void preinit(FMLPreInitializationEvent evt) {
		MinecraftForge.EVENT_BUS.register(new GuiHandler());
		MinecraftForge.EVENT_BUS.register(this);

		Main.config = new Configuration(evt.getSuggestedConfigurationFile());
		Main.loadConfig();

		Secure.init();
	}

	@SubscribeEvent
	public void onConfigChanged(OnConfigChangedEvent evt) {
		if (evt.getModID().equals("reauth")) {
			Main.loadConfig();
		}
	}

	/** (re-)loads config */
	private static void loadConfig() {
		Property pu = config.get(Configuration.CATEGORY_GENERAL, "username", "", "Your Username");
		Secure.username = pu.getString();
		Property pp = config.get(Configuration.CATEGORY_GENERAL, "password", "",
				"Your Password in plaintext if chosen to save to disk");
		Secure.password = pp.getString();
		
		Property po = config.get(Configuration.CATEGORY_GENERAL, "offlineModeEnabled", false,
				"Controls whether a play-offline button is visible in the Re-Login screen");
		Main.OfflineModeEnabled = po.getBoolean();
		
		Property ve = config.get(Configuration.CATEGORY_GENERAL, "validatorEnabled", true,
				"Disables the Session Validator");
		GuiHandler.enabled = ve.getBoolean();
		
		Property vb = config.get(Configuration.CATEGORY_GENERAL, "validatorBold", true,
				"If the Session-Validator look weird disable this");
		GuiHandler.bold = vb.getBoolean();
		
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
