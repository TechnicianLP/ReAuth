package reauth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.URL;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.crypto.SecretKey;

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
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.network.NetServerHandler;
import net.minecraft.util.CryptManager;
import net.minecraft.util.Session;

class Secure {

	/** Username/email */
	protected static String username = "";
	/** password if saved to config else empty */
	protected static String password = "";

	/** mojang authservice */
	private static final YggdrasilUserAuthentication yua;

	private static final String fakeServerHash;

	/** checks for clases that do not belong into this package */
	protected static void init() {
		String base = "reauth.";
		List<String> classes = ImmutableList.of(base + "ConfigGUI", base + "GuiCheckBox", base + "GuiLogin", base + "GuiMultiplayerExtended", base + "GuiPasswordField", base + "Main", base + "Secure");
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
		YggdrasilAuthenticationService yas = new YggdrasilAuthenticationService(Minecraft.getMinecraft().getProxy(), UUID.randomUUID().toString());
		yua = (YggdrasilUserAuthentication) yas.createUserAuthentication(Agent.MINECRAFT);

		
		/** create a serverhash for the seessionvalidator*/
		String id = Long.toString(new Random().nextLong(), 16);
		PublicKey pub = CryptManager.createNewKeyPair().getPublic();
		SecretKey sec = CryptManager.createNewSharedKey();
		fakeServerHash = (new BigInteger(CryptManager.getServerIdHash(id, pub, sec))).toString(16);
	}

	/** LOgs you in; replaces the Session in your client; and saves to config */
	protected static void login(String user, String pw, boolean savePassToConfig) throws AuthenticationException, IllegalArgumentException, IllegalAccessException {
		/** set credentials */
		Secure.yua.setUsername(user);
		Secure.yua.setPassword(pw);

		/** login */
		Secure.yua.logIn();

		Main.log.info("Login successfull!");

		/** put together the new Session with the auth-data */
		String username = Secure.yua.getSelectedProfile().getName();
		String access = Secure.yua.getAuthenticatedToken();
		Sessionutil.set(new Session(username, access));

		/** logout to discard the credentials in the object */
		Secure.yua.logOut();

		/** save username to config */
		Secure.username = user;
		Main.config.get(Main.config.CATEGORY_GENERAL, "username", "", "Your Username").set(Secure.username);
		/** save password to config if desired */
		if (savePassToConfig) {
			Secure.password = pw;
			Main.config.get(Main.config.CATEGORY_GENERAL, "password", "", "Your Password in plaintext if chosen to save to disk").set(Secure.password);
		}
		Main.config.save();
	}

	protected static void offlineMode(String username) throws IllegalArgumentException, IllegalAccessException {
		UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(Charsets.UTF_8));
		Sessionutil.set(new Session(username, "NotValid"));
		Main.log.info("Username set! you can only pay on offline-mode servers now!");
		Secure.username = username;
	}
	
	/**
	 * checks online if the session is valid (uses code from
	 * {@link NetClientHandler#sendSessionRequest})
	 */
	protected static boolean SessionValid() {
		try {
			Session s = Sessionutil.get();
			String base = "http://session.minecraft.net/game/joinserver.jsp?user=%s&sessionId=%s&serverId=%s";
			URL url = new URL(String.format(base, s.getUsername(), s.getSessionID(), fakeServerHash));
			InputStream inputstream = url.openConnection(Minecraft.getMinecraft().getProxy()).getInputStream();
			BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(inputstream));
			String answer = bufferedreader.readLine();
			bufferedreader.close();
			if ("ok".equalsIgnoreCase(answer)) {
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

	/** Fixes Sessions with null entries to avoid crashing */
	protected static void fixSession() {
		try {
			Session s = Sessionutil.get();

			if (s.getSessionID() != null && s.getUsername() != null)
				return;

			String name = s.getUsername();
			name = (name == null) ? "MissingNo" : name;

			String session = s.getSessionID();
			session = (session == null) ? "INVALID" : name;

			Sessionutil.set(new Session(name, session));
		} catch (Exception e) {
			e.printStackTrace();
		}
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
