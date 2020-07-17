package technicianlp.reauth;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;

public final class AuthHelper {

    /**
     * Time for which the Validity gets cached (5 Minutes)
     */
    private static final long cacheTime = 5 * 1000 * 60L;

    /**
     * Reflective YggdrasilUserAuthentication#accessToken
     */
    private static final Field accessToken;
    /**
     * Reflective Minecraft#session
     */
    private static Field sessionField;
    /**
     * Reflective YggdrasilUserAuthentication#checkTokenValidity
     */
    private static final Method checkTokenValidity;
    /**
     * Reflective YggdrasilUserAuthentication#logInWithPassword
     */
    private static final Method logInWithPassword;

    static {
        sessionField = ObfuscationReflectionHelper.findField(Minecraft.class, "field_71449_j");
        try {
            Class<?> clz = YggdrasilUserAuthentication.class;
            accessToken = clz.getDeclaredField("accessToken");
            accessToken.setAccessible(true);
            checkTokenValidity = clz.getDeclaredMethod("checkTokenValidity");
            checkTokenValidity.setAccessible(true);
            logInWithPassword = clz.getDeclaredMethod("logInWithPassword");
            logInWithPassword.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed Reflective access", e);
        }
    }

    /**
     * Current cached Session Validity
     */
    private SessionStatus status = SessionStatus.UNKNOWN;
    private long lastCheck = 0;

    /**
     * Two Authentication Service are required as a
     * Validation-Request may not have a clientToken,
     * yet a Login-Request requires it.
     */
    private final YggdrasilUserAuthentication checkAuth;
    private final YggdrasilUserAuthentication loginAuth;

    /**
     * Pattern for valid Minecraft Names according to Wiki
     */
    private final Pattern namePattern = Pattern.compile("[A-Za-z0-9_]{2,16}");

    public AuthHelper() {
        Proxy proxy = Minecraft.getInstance().getProxy();
        String clientId = UUID.randomUUID().toString();
        checkAuth = new YggdrasilUserAuthentication(new YggdrasilAuthenticationService(proxy, null), Agent.MINECRAFT);
        loginAuth = new YggdrasilUserAuthentication(new YggdrasilAuthenticationService(proxy, clientId), Agent.MINECRAFT);
    }

    /**
     * Get the cached Validity Status of the accessToken
     * Re-Validation is done if the cache expires or it is forced by the Parameter of the same name
     */
    public SessionStatus getSessionStatus(boolean force) {
        if (force || lastCheck + cacheTime < System.currentTimeMillis())
            status = SessionStatus.UNKNOWN;

        if (status == SessionStatus.UNKNOWN) {
            status = SessionStatus.REFRESHING;
            lastCheck = System.currentTimeMillis();

            Thread t = new Thread(this::updateSessionStatus, "ReAuth Session Validator");
            t.setDaemon(true);
            t.start();
        }
        return status;
    }

    /**
     * Uses the Validate Endpoint to check the current Tokens validity and updates the cache accordingly
     */
    private void updateSessionStatus() {
        setToken(getSession().getToken());
        boolean valid = callCheckTokenValidity();
        status = valid ? SessionStatus.VALID : SessionStatus.INVALID;
        lastCheck = System.currentTimeMillis();
    }

    /**
     * Login with the Supplied Username and Password
     * Password is saved to config if {@code savePassword} is true
     **/
    public void login(String user, char[] password, boolean savePassword) throws AuthenticationException {
        login(user, new String(password), savePassword);
    }

    public void login(String user, String password, boolean savePassword) throws AuthenticationException {
        loginAuth.setUsername(user);
        loginAuth.setPassword(password);
        try {
            callLogInWithPassword();

            String username = loginAuth.getSelectedProfile().getName();
            String uuid = UUIDTypeAdapter.fromUUID(loginAuth.getSelectedProfile().getId());
            String access = loginAuth.getAuthenticatedToken();
            String type = loginAuth.getUserType().getName();

            Session session = new Session(username, uuid, access, type);
            session.setProperties(loginAuth.getUserProperties());

            loginAuth.logOut();

            setSession(session);

            ReAuth.config.setCredentials(username, user, savePassword ? password : "");
        } finally {
            loginAuth.logOut();
        }
    }

    /**
     * Sets the Players Offline Username
     * UUID is generated from the supplied Username
     */
    public void offline(String username) {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
        setSession(new Session(username, uuid.toString(), "invalid", "legacy"));
        ReAuth.log.info("Offline Username set!");
        ReAuth.config.setCredentials(username, "", "");
    }

    private Session getSession() {
        return Minecraft.getInstance().getSession();
    }

    private void setSession(Session s) {
        try {
            sessionField.set(Minecraft.getInstance(), s);
        } catch (ReflectiveOperationException throwable) {
            throw new RuntimeException("Failed Reflective Access", throwable);
        }
        status = SessionStatus.UNKNOWN;
    }

    private void setToken(String token) {
        try {
            accessToken.set(checkAuth, token);
        } catch (ReflectiveOperationException throwable) {
            throw new RuntimeException("Failed Reflective Access", throwable);
        }
    }

    private boolean callCheckTokenValidity() {
        try {
            return (Boolean) checkTokenValidity.invoke(checkAuth);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed Reflective Access", e);
        }
    }

    private void callLogInWithPassword() throws AuthenticationException {
        try {
            logInWithPassword.invoke(loginAuth);
        } catch (InvocationTargetException exception) {
            Throwable parent = exception.getCause();
            if (parent instanceof AuthenticationException)
                throw (AuthenticationException) parent;
            ReAuth.log.error("LogInWithPassword has thrown unexpected exception:", parent);
        } catch (ReflectiveOperationException throwable) {
            throw new RuntimeException("Failed Reflective Access", throwable);
        }
    }

    public boolean isValidName(String username) {
        return namePattern.matcher(username).matches();
    }

    public enum SessionStatus {
        VALID("valid"),
        UNKNOWN("unknown"),
        REFRESHING("unknown"),
        INVALID("invalid");

        private final String translationKey;

        SessionStatus(String translationKey) {
            this.translationKey = "reauth.status." + translationKey;
        }

        public String getTranslationKey() {
            return translationKey;
        }
    }
}
