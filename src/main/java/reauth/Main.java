package reauth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

@Mod(modid = "ReAuth", version = "3.2", guiFactory = "reauth.GuiFactory", canBeDeactivated = true, clientSideOnly = true, acceptedMinecraftVersions = "[1.8.0,1.8.9]", certificateFingerprint = "35787b2f97a740b13a05638ab0d20d2107e3a79e")
public class Main {

	protected static final Logger log = LogManager.getLogger("ReAuth");
	protected static Configuration config;

	protected static boolean OfflineModeEnabled;

	@Mod.Instance("ReAuth")
	Main main;

	@Mod.Metadata
	protected static ModMetadata meta;

	@Mod.EventHandler
	public void preinit(FMLPreInitializationEvent evt) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		MinecraftForge.EVENT_BUS.register(new GuiHandler());
		FMLCommonHandler.instance().bus().register(this);

		Main.config = new Configuration(evt.getSuggestedConfigurationFile());
		Main.loadConfig();

		Secure.init();
	}

	@SubscribeEvent
	public void onConfigChanged(OnConfigChangedEvent evt) {
		if (evt.modID.equals("ReAuth")) {
			Main.loadConfig();
		}
	}

	/** (re-)loads config */
	private static void loadConfig() {
		Property pu = config.get(config.CATEGORY_GENERAL, "username", "", "Your Username");
		Secure.username = pu.getString();
		Property pp = config.get(config.CATEGORY_GENERAL, "password", "", "Your Password in plaintext if chosen to save to disk");
		Secure.password = pp.getString();

		Property po = config.get(Configuration.CATEGORY_GENERAL, "offlineModeEnabled", false,
				"Controls wheter a play-offline button is visble in the Re-Login screen");
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
