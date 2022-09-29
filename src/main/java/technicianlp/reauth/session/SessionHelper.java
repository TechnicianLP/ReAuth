package technicianlp.reauth.session;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.client.util.Session;
import technicianlp.reauth.ReAuth;
import technicianlp.reauth.authentication.SessionData;
import technicianlp.reauth.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public enum SessionHelper {
    ;

    private static final Field sessionField = ReflectionUtils.findObfuscatedField(MinecraftClient.class, "f_90998_",
        "session");
    private static final Field userApiServiceField = ReflectionUtils.findObfuscatedField(MinecraftClient.class,
        "f_193584_", "userApiService");
    private static final Field socialManagerField = ReflectionUtils.findObfuscatedField(MinecraftClient.class,
        "f_91006_", "socialInteractionsManager");
    private static final Field splashesSessionField =
        ReflectionUtils.findObfuscatedField(SplashTextResourceSupplier.class, "f_118863_", "session");

    private static final Pattern usernamePattern = Pattern.compile("\\w{2,16}");

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
     * <ul>
     * <li>Clear ProfileProperties and repopulate them
     * <li>Recreate {@link UserApiService}, using logic from
     * {@link MinecraftClient#createUserApiService(YggdrasilAuthenticationService, RunArgs)}
     * <li>Recreate {@link SocialInteractionsManager} with the new SocialInteractionsService
     * <li>Update {@link SplashTextResourceSupplier#session}
     * </ul>
     */
    private static void setSession(SessionData data, boolean online) {
        try {
            MinecraftClient minecraft = MinecraftClient.getInstance();

            Session session = new Session(data.username(), data.uuid(), data.accessToken(), Optional.empty(),
                Optional.empty(), Session.AccountType.byName(data.type()));

            ReflectionUtils.setField(sessionField, minecraft, session);
            SessionChecker.invalidate();

            // Update things depending on the Session.
            // TODO keep updated across versions

            // Clear ProfileProperties and repopulate them
            minecraft.getSessionProperties().clear();
            minecraft.getSessionProperties();
            // UserProperties are unused

            // Recreate UserApiService
            UserApiService userApiService = null;
            if (online) {
                YggdrasilMinecraftSessionService sessionService =
                    (YggdrasilMinecraftSessionService) minecraft.getSessionService();
                YggdrasilAuthenticationService authService = sessionService.getAuthenticationService();
                try {
                    userApiService = authService.createUserApiService(session.getAccessToken());
                } catch (AuthenticationException authException) {
                    ReAuth.log.error("Failed to create UserApiService", authException);
                }
            }
            if (userApiService == null) {
                userApiService = UserApiService.OFFLINE;
            }
            ReflectionUtils.setField(userApiServiceField, minecraft, userApiService);

            // Recreate FilterManager
            SocialInteractionsManager socialManager = new SocialInteractionsManager(minecraft, userApiService);
            ReflectionUtils.setField(socialManagerField, minecraft, socialManager);

            // Update Splashes session
            ReflectionUtils.setField(splashesSessionField, minecraft.getSplashTextLoader(), session);
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
