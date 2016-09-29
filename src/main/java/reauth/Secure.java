package reauth;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraftforge.common.config.Configuration;

class Secure {

	/** Username/email */
	protected static String username = "";
	/** password if saved to config else empty */
	protected static String password = "";

	/** Mojang authentificationservice */
	private static final YggdrasilAuthenticationService yas;
	private static final YggdrasilUserAuthentication yua;
	private static final YggdrasilMinecraftSessionService ymss;

	/** currently used to load the class */
	protected static void init() {
		String base = "reauth.";
		List<String> classes = ImmutableList.of(base + "ConfigGUI", base + "GuiFactory", base + "GuiCheckBox", base + "GuiHandler", base + "GuiLogin", base + "GuiPasswordField", base + "Main", base + "Secure", base + "VersionChecker");
		try {
			Set<ClassInfo> set = ClassPath.from(Secure.class.getClassLoader()).getTopLevelClassesRecursive("reauth");
			for (ClassInfo info : set)
				if (!classes.contains(info.getName())) {
					throw new RuntimeException("Detected unauthorized class trying to access reauth-data! Offender: " + info.url().getPath());
				}
		} catch (IOException e) {
			throw new RuntimeException("Classnames could not be fetched!");
		}
	}

	static {
		/** initialize the authservices */
		yas = new YggdrasilAuthenticationService(Minecraft.getMinecraft().getProxy(), UUID.randomUUID().toString());
		yua = (YggdrasilUserAuthentication) yas.createUserAuthentication(Agent.MINECRAFT);
		ymss = (YggdrasilMinecraftSessionService) yas.createMinecraftSessionService();

	}

	/** LOgs you in; replaces the Session in your client; and saves to config */
	protected static void login(String user, String pw, boolean savePassToConfig) throws AuthenticationException, IllegalArgumentException, IllegalAccessException {
		if (!VersionChecker.isVersionAllowed())
			throw new AuthenticationException("ReAuth has a critical update!");

		/** set credentials */
		Secure.yua.setUsername(user);
		Secure.yua.setPassword(pw);

		/** login */
		Secure.yua.logIn();

		Main.log.info("Login successfull!");

		/** put together the new Session with the auth-data */
		String username = Secure.yua.getSelectedProfile().getName();
		String uuid = Secure.yua.getSelectedProfile().getId();
		String access = Secure.yua.getAuthenticatedToken();
		Sessionutil.set(new Session(username, uuid, access));

		/** logout to discard the credentials in the object */
		Secure.yua.logOut();

		/** save username to config */
		Secure.username = user;
		Main.config.get(Configuration.CATEGORY_GENERAL, "username", "", "Your Username").set(Secure.username);
		/** save password to config if desired */
		if (savePassToConfig) {
			Secure.password = pw;
			Main.config.get(Configuration.CATEGORY_GENERAL, "password", "", "Your Password in plaintext if chosen to save to disk").set(Secure.password);
		}
		Main.config.save();
	}

	protected static void offlineMode(String username) throws IllegalArgumentException, IllegalAccessException {
		/**  */
		UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(Charsets.UTF_8));
		Sessionutil.set(new Session(username, uuid.toString(), null));
		Main.log.info("Username set! you can only pay on offline-mode servers now!");
		Secure.username = username;
	}

	/** returns if the session may be valid */
	protected static boolean sessionFound() {
		return yua.isLoggedIn() && yua.canPlayOnline();
	}

	/** checks online if the session is valid */
	protected static boolean SessionValid() {
		try {
			GameProfile gp = Sessionutil.get().func_148256_e();
			String token = Sessionutil.get().getToken();
			String id = UUID.randomUUID().toString();

			Secure.ymss.joinServer(gp, token, id);
			if (Secure.ymss.hasJoinedServer(gp, id).isComplete()) {
				Main.log.info("Session validation successfull");
				return true;
			}
		} catch (Exception e) {
			Main.log.info("Session validation failed: " + e.getMessage());
			return false;
		}
		Main.log.info("Session validation failed!");
		return false;
	}

	protected static class Sessionutil {
		/**
		 * as the Session field in Minecraft.class is static final we have to
		 * access it via reflection
		 */
		private static Field sessionField = ReflectionHelper.findField(Minecraft.class, "session", "S", "field_71449_j");

		protected static Session get() throws IllegalArgumentException, IllegalAccessException {
			return (Session) Sessionutil.sessionField.get(Minecraft.getMinecraft());
		}

		protected static void set(Session s) throws IllegalArgumentException, IllegalAccessException {
			Sessionutil.sessionField.set(Minecraft.getMinecraft(), s);
		}
	}

}
