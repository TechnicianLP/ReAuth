package technicianlp.reauth.session;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.OfflineSocialInteractions;
import com.mojang.authlib.minecraft.SocialInteractionsService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.social.FilterManager;
import net.minecraft.client.util.Splashes;
import net.minecraft.util.Session;
import technicianlp.reauth.ReAuth;
import technicianlp.reauth.authentication.SessionData;
import technicianlp.reauth.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;

public final class SessionHelper {

    private static final Field sessionField = ReflectionUtils.findObfuscatedField(Minecraft.class, "field_71449_j", "session");
    private static final Field interactionServiceField = ReflectionUtils.findObfuscatedField(Minecraft.class, "field_244734_au", "field_244734_au");
    private static final Field filterManagerField = ReflectionUtils.findObfuscatedField(Minecraft.class, "field_244597_aC", "field_244597_aC");
    private static final Field splashesSessionField = ReflectionUtils.findObfuscatedField(Splashes.class, "field_215281_d", "gameSession");

    private static final Pattern usernamePattern = Pattern.compile("[A-Za-z0-9_]{2,16}");

    /**
     * construct a {@link SessionData} for the given offline username
     */
    public static void setOfflineUsername(String username) {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
        setSession(new SessionData(username, uuid.toString(), "invalid", "legacy"), false);
    }

    /**
     * Set the Session and update dependant fields
     */
    public static void setSession(SessionData data) {
        setSession(data, true);
    }

    /**
     * Set the Session and update dependant fields
     * <p>
     * <li>Clear ProfileProperties and repopulate them
     * <li>Recreate {@link SocialInteractionsService}, using logic from {@link Minecraft#func_244735_a}
     * <li>Recreate {@link FilterManager} with the new SocialInteractionsService
     * <li>Update {@link Splashes#gameSession}
     */
    private static void setSession(SessionData data, boolean online) {
        try {
            Minecraft minecraft = Minecraft.getInstance();

            Session session = new Session(data.username, data.uuid, data.accessToken, data.type);

            ReflectionUtils.setField(sessionField, minecraft, session);
            SessionChecker.invalidate();

            // Update things depending on the Session.
            // TODO keep updated across versions

            // Clear ProfileProperties and repopulate them
            minecraft.getProfileProperties().clear();
            minecraft.getProfileProperties();
            // UserProperties are unused

            // Recreate SocialInteractionsService
            SocialInteractionsService interactionsService = null;
            if (online) {
                YggdrasilMinecraftSessionService sessionService = (YggdrasilMinecraftSessionService) minecraft.getSessionService();
                YggdrasilAuthenticationService authService = sessionService.getAuthenticationService();
                try {
                    interactionsService = authService.createSocialInteractionsService(session.getToken());
                } catch (AuthenticationException authenticationexception) {
                    ReAuth.log.error("Failed to create SocialInteractionsService", authenticationexception);
                }
            }
            if (interactionsService == null) {
                interactionsService = new OfflineSocialInteractions();
            }
            ReflectionUtils.setField(interactionServiceField, minecraft, interactionsService);

            // Recreate FilterManager
            FilterManager filterManager = new FilterManager(minecraft, interactionsService);
            ReflectionUtils.setField(filterManagerField, minecraft, filterManager);

            // Update Splashes session
            ReflectionUtils.setField(splashesSessionField, minecraft.getSplashes(), session);
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
