package reauth;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.Proxy;
import java.net.URL;
import java.security.PublicKey;
import java.util.Random;
import java.util.UUID;

import javax.crypto.SecretKey;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.NetClientHandler;
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
		// TODO get classchecker working
	}

	static {
		/** initialize the authservices */
		YggdrasilAuthenticationService yas = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());
		yua = (YggdrasilUserAuthentication) yas.createUserAuthentication(Agent.MINECRAFT);

		/** create a serverhash for the seessionvalidator */
		String id = Long.toString(new Random().nextLong(), 16);
		PublicKey pub = CryptManager.createNewKeyPair().getPublic();
		SecretKey sec = CryptManager.createNewSharedKey();
		fakeServerHash = (new BigInteger(CryptManager.getServerIdHash(id, pub, sec))).toString(16);
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
		String access = Secure.yua.getAuthenticatedToken();
		Minecraft.getMinecraft().session = new Session(username, access);

		/** logout to discard the credentials in the object */
		Secure.yua.logOut();

		/** save username to config */
		Secure.username = user;
		/** save password to config if desired */
		if (savePassToConfig) {
			Secure.password = pw;
		}
		Main.saveConfig();
	}

	protected static void offlineMode(String username) throws IllegalArgumentException, IllegalAccessException {
		Minecraft.getMinecraft().session = new Session(username, "NotValid");
		Main.log.info("Username set! you can only pay on offline-mode servers now!");
		Secure.username = username;
	}

	/**
	 * checks online if the session is valid (uses code from
	 * {@link NetClientHandler#sendSessionRequest})
	 */
	protected static boolean SessionValid() {
		try {
			Session s = Minecraft.getMinecraft().session;
			String base = "http://session.minecraft.net/game/joinserver.jsp?user=%s&sessionId=%s&serverId=%s";
			URL url = new URL(String.format(base, s.username, s.sessionId, fakeServerHash));
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String answer = reader.readLine();
			reader.close();
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
		Minecraft mc = Minecraft.getMinecraft();
		try {
			Session s = mc.session;
			String name = null;
			String session = null;

			if (s != null) {
				if (s.sessionId != null && s.username != null)
					return;

				name = s.username;
				session = s.sessionId;
			}

			name = (name == null) ? "MissingNo" : name;
			session = (session == null) ? "INVALID" : name;

			mc.session = new Session(name, session);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
