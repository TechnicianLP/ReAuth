package reauth;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSerializationContext;
import com.mojang.authlib.Agent;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import com.mojang.util.UUIDTypeAdapter;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

@Mod(modid = "ReAuth", guiFactory = "reauth.GuiFactory")
public class Main {

	public static final Logger log = LogManager.getLogger("ReAuth");
	public static Configuration config;

	public static String username = "";
	public static String password = "";

	public static final YggdrasilAuthenticationService yas = new YggdrasilAuthenticationService(Minecraft.getMinecraft().getProxy(), UUID.randomUUID().toString());
	public static final YggdrasilUserAuthentication ua = (YggdrasilUserAuthentication) yas.createUserAuthentication(Agent.MINECRAFT);

	@Mod.Instance("ReAuth")
	Main main;

	@Mod.EventHandler
	public void preinit(FMLPreInitializationEvent evt) {
		MinecraftForge.EVENT_BUS.register(new GuiHandler());
		FMLCommonHandler.instance().bus().register(this);

		Main.config = new Configuration(evt.getSuggestedConfigurationFile());
		Main.loadConfig();

		Main.ua.setUsername(Main.username);
		Main.ua.setPassword(password);
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
		Main.username = pu.getString();
		Property pp = config.get(config.CATEGORY_GENERAL, "password", "", "Your Password in plaintext if chosen to save to disk");
		Main.password = pp.getString();
		Main.config.save();
	}

	/** LOgs you in; replaces the Session in your client; and saves to config */
	public static void login(String user, String pw, boolean savePassToConfig) throws AuthenticationException, IllegalArgumentException, IllegalAccessException {
		if (!Main.sessionFound()) {
			Main.ua.setUsername(user);
			Main.ua.setPassword(pw);
		}

		Main.ua.logIn();
		Main.log.info("Login successfull!");

		String username = Main.ua.getSelectedProfile().getName();
		String uuid = UUIDTypeAdapter.fromUUID(Main.ua.getSelectedProfile().getId());
		String access = Main.ua.getAuthenticatedToken();
		String type = Main.ua.getUserType().getName();
		Session session = new Session(username, uuid, access, type);

		Field f = ReflectionHelper.findField(Minecraft.class, "session", "S", "field_71449_j");
		f.set(Minecraft.getMinecraft(), session);

		Main.username = user;
		Main.config.get(Main.config.CATEGORY_GENERAL, "username", "", "Your Username").set(Main.username);
		if (savePassToConfig) {
			Main.password = pw;
			Main.config.get(Main.config.CATEGORY_GENERAL, "password", "", "Your Password in plaintext if chosen to save to disk").set(Main.password);
		}
		Main.config.save();
	}

	/** returns if the session may be valid */
	public static boolean sessionFound() {
		return Main.ua.isLoggedIn() && Main.ua.canPlayOnline();
	}

}
