package technicianlp.reauth.session;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;

import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.resources.SplashManager;
import technicianlp.reauth.ReAuth;
import technicianlp.reauth.authentication.SessionData;
import technicianlp.reauth.util.ReflectionUtils;

public final class SessionHelper {

    private static final Field userField = ReflectionUtils.findObfuscatedField(Minecraft.class, "f_90998_", "user");
    private static final Field userApiServiceField = ReflectionUtils.findObfuscatedField(Minecraft.class, "f_193584_", "userApiService");
    private static final Field socialManagerField = ReflectionUtils.findObfuscatedField(Minecraft.class, "f_91006_", "playerSocialManager");
    private static final Field splashesSessionField = ReflectionUtils.findObfuscatedField(SplashManager.class, "f_118863_", "user");

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
     * <li>Recreate {@link UserApiService}, using logic from {@link Minecraft#createUserApiService(YggdrasilAuthenticationService, GameConfig)}
     * <li>Recreate {@link PlayerSocialManager} with the new SocialInteractionsService
     * <li>Update {@link SplashManager#user}
     */
    private static void setSession(SessionData data, boolean online) {
        try {
            Minecraft minecraft = Minecraft.getInstance();

            User session = new User(data.username, data.uuid, data.accessToken, Optional.empty(), Optional.empty(), User.Type.byName(data.type));

            ReflectionUtils.setField(userField, minecraft, session);
            SessionChecker.invalidate();

            // Update things depending on the Session.
            // TODO keep updated across versions

            // Clear ProfileProperties and repopulate them
            minecraft.getProfileProperties().clear();
            minecraft.getProfileProperties();
            // UserProperties are unused

            // Recreate UserApiService
            UserApiService userApiService = null;
            if (online) {
                YggdrasilMinecraftSessionService sessionService = (YggdrasilMinecraftSessionService) minecraft.getMinecraftSessionService();
                YggdrasilAuthenticationService authService = sessionService.getAuthenticationService();
                try {
                    userApiService = authService.createUserApiService(session.getAccessToken());
                } catch (AuthenticationException authenticationexception) {
                    ReAuth.log.error("Failed to create UserApiService", authenticationexception);
                }
            }
            if (userApiService == null) {
                userApiService = UserApiService.OFFLINE;
            }
            ReflectionUtils.setField(userApiServiceField, minecraft, userApiService);

            // Recreate FilterManager
            PlayerSocialManager socialManager = new PlayerSocialManager(minecraft, userApiService);
            ReflectionUtils.setField(socialManagerField, minecraft, socialManager);

            // Update Splashes session
            ReflectionUtils.setField(splashesSessionField, minecraft.getSplashManager(), session);
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
