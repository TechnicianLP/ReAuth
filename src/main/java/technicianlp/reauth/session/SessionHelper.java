package technicianlp.reauth.session;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import technicianlp.reauth.ReAuth;
import technicianlp.reauth.authentication.SessionData;
import technicianlp.reauth.util.ReflectionHelper;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;

public final class SessionHelper {

    private static final Field sessionField = ReflectionHelper.findMcpField(Minecraft.class, "field_71449_j");

    private static final Pattern usernamePattern = Pattern.compile("[A-Za-z0-9_]{2,16}");

    /**
     * construct a {@link SessionData} for the given offline username
     */
    public static void setOfflineUsername(String username) {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
        setSession(new SessionData(username, uuid.toString(), "invalid", "legacy"));
    }

    /**
     * Set the Session and update dependant fields
     * <p>
     * <li>Clear ProfileProperties and repopulate them
     */
    public static void setSession(SessionData data) {
        try {
            Minecraft minecraft = Minecraft.getMinecraft();

            Session session = new Session(data.username, data.uuid, data.accessToken, data.type);

            ReflectionHelper.setField(sessionField, minecraft, session);
            SessionChecker.invalidate();

            // Update things depending on the Session.
            // TODO keep updated across versions

            // Clear ProfileProperties and repopulate them
            minecraft.getProfileProperties().clear();
            minecraft.getProfileProperties();
            // UserProperties are unused
        } catch (Exception e) {
            ReAuth.log.error("Failed to update Session", e);
        }
    }

    /**
     * checks the username to match the offline username regex
     */
    public static boolean isValidOfflineUsername(String username) {
        return usernamePattern.matcher(username).matches();
    }
}
