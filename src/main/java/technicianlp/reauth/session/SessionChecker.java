package technicianlp.reauth.session;

import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import technicianlp.reauth.ReAuth;
import technicianlp.reauth.authentication.YggdrasilAPI;
import technicianlp.reauth.authentication.http.UnreachableServiceException;

import java.util.concurrent.CompletableFuture;

public final class SessionChecker {

    /**
     * Time for which the Validity gets cached (5 Minutes)
     */
    private static final long cacheTime = 5 * 1000 * 60L;

    /**
     * Current cached Session Validity
     */
    private static SessionStatus status = SessionStatus.UNKNOWN;
    private static long lastCheck = 0;

    /**
     * Get the cached Validity Status of the accessToken
     * Re-Validation is done if the cache expires
     */
    public static SessionStatus getSessionStatus() {
        if (lastCheck + cacheTime < System.currentTimeMillis())
            status = SessionStatus.UNKNOWN;

        if (status == SessionStatus.UNKNOWN) {
            status = SessionStatus.REFRESHING;
            lastCheck = System.currentTimeMillis();

            CompletableFuture<User> session = CompletableFuture.completedFuture(Minecraft.getInstance().getUser());
            CompletableFuture<String> token = session.thenApply(User::getAccessToken);
            CompletableFuture<String> uuid = session.thenApply(User::getUuid);
            token.thenCombineAsync(uuid, SessionChecker::getSessionStatus, ReAuth.executor)
                    .thenAccept(status -> SessionChecker.status = status);
        }
        return status;
    }

    public static void invalidate() {
        status = SessionStatus.UNKNOWN;
    }

    /**
     * Uses the Validate Endpoint to check the Tokens validity
     */
    private static SessionStatus getSessionStatus(String accessToken, String uuid) {
        try {
            return YggdrasilAPI.validate(accessToken, uuid) ? SessionStatus.VALID : SessionStatus.INVALID;
        } catch (UnreachableServiceException e) {
            ReAuth.log.error("Failed to check session validity", e);
            return SessionStatus.ERROR;
        }
    }
}
