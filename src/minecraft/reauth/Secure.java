package reauth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.URL;
import java.security.PublicKey;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.crypto.SecretKey;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.util.CryptManager;
import net.minecraft.util.Session;
import net.minecraftforge.common.Configuration;

class Secure {

	/** Username/email */
	static String username = "";
	/** password if saved to config else empty */
	static String password = "";

	/** mojang authservice */
	private static final YggdrasilUserAuthentication yua;

	private static final String fakeServerHash;

	/** checks for clases that do not belong into this package */
	static void init() {
		String base = "reauth.";
		List<String> classes = ImmutableList.of(base + "ConfigGUI", base + "GuiCheckBox", base + "GuiLogin", base + "GuiMultiplayerExtended", base + "GuiPasswordField", base + "Main", base + "Secure", base + "VersionChecker");
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
		/* initialize the authservices */
		YggdrasilAuthenticationService yas = new YggdrasilAuthenticationService(Minecraft.getMinecraft().getProxy(), UUID.randomUUID().toString());
		yua = (YggdrasilUserAuthentication) yas.createUserAuthentication(Agent.MINECRAFT);

		/* create a serverHash for the sessionValidator */
		String id = Long.toString(new Random().nextLong(), 16);
		PublicKey pub = CryptManager.createNewKeyPair().getPublic();
		SecretKey sec = CryptManager.createNewSharedKey();
		fakeServerHash = (new BigInteger(CryptManager.getServerIdHash(id, pub, sec))).toString(16);
	}

	/** LOgs you in; replaces the Session in your client; and saves to config */
	static void login(String user, String pw, boolean savePassToConfig) throws AuthenticationException, IllegalArgumentException, IllegalAccessException {
		if (!VersionChecker.isVersionAllowed())
			throw new AuthenticationException("ReAuth has a critical update!");
		
		/* set credentials */
		Secure.yua.setUsername(user);
		Secure.yua.setPassword(pw);

		/* login */
		Secure.yua.logIn();

		Main.log.info("Login successfull!");

		/* put together the new Session with the auth-data */
		String username = Secure.yua.getSelectedProfile().getName();
		String access = Secure.yua.getAuthenticatedToken();
		SessionUtil.set(new Session(username, access));

		/* logout to discard the credentials in the object */
		Secure.yua.logOut();

		/* save username to config */
		Secure.username = user;
		Main.config.get(Configuration.CATEGORY_GENERAL, "username", "", "Your Username").set(Secure.username);
		/* save password to config if desired */
		if (savePassToConfig) {
			Secure.password = pw;
			Main.config.get(Configuration.CATEGORY_GENERAL, "password", "", "Your Password in plaintext if chosen to save to disk").set(Secure.password);
		}
		Main.config.save();
	}

	static void offlineMode(String username) throws IllegalArgumentException, IllegalAccessException {
		SessionUtil.set(new Session(username, "NotValid"));
		Main.log.info("Offline Username set!");
		Secure.username = username;
	}

	/**
	 * checks online if the session is valid (uses code from
	 * {@link NetClientHandler#sendSessionRequest})
	 */
	static boolean SessionValid() {
		try {
			Session s = SessionUtil.get();
			String base = "http://session.minecraft.net/game/joinserver.jsp?user=%s&sessionId=%s&serverId=%s";
			URL url = new URL(String.format(base, s.getUsername(), s.getSessionID(), fakeServerHash));
			InputStream inputstream = url.openConnection(Minecraft.getMinecraft().getProxy()).getInputStream();
			BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(inputstream));
			String answer = bufferedreader.readLine();
			bufferedreader.close();
			if ("ok".equalsIgnoreCase(answer)) {
				Main.log.info("Session validation successful");
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
	static void fixSession() {
		try {
			Session s = SessionUtil.get();

			if (s.getSessionID() != null && s.getUsername() != null)
				return;

			String name = s.getUsername();
			name = (name == null) ? "MissingNo" : name;

			String session = s.getSessionID();
			session = (session == null) ? "INVALID" : name;

			SessionUtil.set(new Session(name, session));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static class SessionUtil {
		/**
		 * as the Session field in Minecraft.class is static final we have to
		 * access it via reflection
		 */
		private static Field sessionField = ReflectionHelper.findField(Minecraft.class, "session", "S", "field_71449_j");

		static Session get() throws IllegalArgumentException, IllegalAccessException {
			return (Session) SessionUtil.sessionField.get(Minecraft.getMinecraft());
		}

		static void set(Session s) throws IllegalArgumentException, IllegalAccessException {
			SessionUtil.sessionField.set(Minecraft.getMinecraft(), s);
		}
	}

}
