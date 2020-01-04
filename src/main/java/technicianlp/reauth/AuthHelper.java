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
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class AuthHelper extends YggdrasilUserAuthentication {

    // Reflective Field access to private (final) Fields
    private static Field sessionField;
    private static Field tokenField;

    static {
        sessionField = ObfuscationReflectionHelper.findField(Minecraft.class, "field_71449_j");
        try {
            tokenField = YggdrasilUserAuthentication.class.getDeclaredField("accessToken");
            tokenField.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException("Failed Reflective access", e);
        }
    }

    /**
     * Current cached Session Validity
     */
    private SessionStatus status = SessionStatus.Unknown;
    private long lastCheck = 0;
    /**
     * Time for which the Validity gets cached (5 Minutes)
     */
    private final long cacheTime = 5 * 1000 * 60;

    public AuthHelper() {
        super(new YggdrasilAuthenticationService(Minecraft.getInstance().getProxy(), UUID.randomUUID().toString()), Agent.MINECRAFT);
    }

    /**
     * Get the cached Validity Status of the accessToken
     * Re-Validation is done if the cache expires or it is forced by the Parameter of the same name
     */
    public SessionStatus getSessionStatus(boolean force) {
        if (force || lastCheck + cacheTime < System.currentTimeMillis())
            status = SessionStatus.Unknown;

        if (status == SessionStatus.Unknown) {
            status = SessionStatus.Refreshing;
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
        boolean valid = false;
        try {
            valid = checkTokenValidity();
        } catch (AuthenticationException ignored) {
        }
        status = valid ? SessionStatus.Valid : SessionStatus.Invalid;
        lastCheck = System.currentTimeMillis();
    }

    /**
     * Login with the Supplied Username and Password
     * Password is saved to config if {@code savePassword} is true
     **/
    public void login(String user, String password, boolean savePassword) throws AuthenticationException {
        setUsername(user);
        setPassword(password);
        try {
            logInWithPassword();

            String username = getSelectedProfile().getName();
            String uuid = UUIDTypeAdapter.fromUUID(getSelectedProfile().getId());
            String access = getAuthenticatedToken();
            String type = getUserType().getName();

            Session session = new Session(username, uuid, access, type);
            session.setProperties(getUserProperties());

            logOut();

            setSession(session);

            Main.config.setCredentials(user, savePassword ? password : "");
        } catch (AuthenticationException e) {
            throw e;
        } finally {
            logOut();
        }
    }

    /**
     * Sets the Players Offline Username
     * UUID is generated from the supplied Username
     */
    public void offline(String username) {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
        setSession(new Session(username, uuid.toString(), "invalid", "legacy"));
        Main.log.info("Offline Username set!");
        Main.config.setCredentials(username, "");
    }

    private void setToken(String accessToken) {
        try {
            tokenField.set(this, accessToken);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed Reflective Access", e);
        }
    }

    private Session getSession() {
        return Minecraft.getInstance().getSession();
    }

    private void setSession(Session s) {
        try {
            AuthHelper.sessionField.set(Minecraft.getInstance(), s);
            status = SessionStatus.Unknown;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed Reflective Access", e);
        }
    }

    public enum SessionStatus {
        Valid("valid", 0xFF00FF00),
        Unknown("unknown", 0xFF808080),
        Refreshing("unknown", 0xFF808080),
        Invalid("invalid", 0xFFFF0000);

        private final String translationKey;
        private final int color;

        SessionStatus(String translationKey, int color) {
            this.translationKey = "reauth.status." + translationKey;
            this.color = color;
        }

        public String getTranslationKey() {
            return translationKey;
        }

        public int getColor() {
            return color;
        }
    }

}
