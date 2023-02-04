package technicianlp.reauth.session;

import java.util.concurrent.CompletableFuture;

import technicianlp.reauth.ReAuth;
import technicianlp.reauth.authentication.YggdrasilAPI;
import technicianlp.reauth.authentication.http.UnreachableServiceException;

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
    public static SessionStatus getSessionStatus(String token, String uuid) {
        if (lastCheck + cacheTime < System.currentTimeMillis())
            status = SessionStatus.UNKNOWN;

        if (status == SessionStatus.UNKNOWN) {
            status = SessionStatus.REFRESHING;
            lastCheck = System.currentTimeMillis();

            CompletableFuture<String> tokenFuture = CompletableFuture.completedFuture(token);
            CompletableFuture<String> uuidFuture = CompletableFuture.completedFuture(uuid);
            tokenFuture.thenCombineAsync(uuidFuture, SessionChecker::getSessionStatus0, ReAuth.executor)
                    .thenAccept(SessionChecker::setStatus);
        }
        return status;
    }

    public static void invalidate() {
        status = SessionStatus.UNKNOWN;
    }

    private static SessionStatus getSessionStatus0(String accessToken, String uuid) {
        try {
            return YggdrasilAPI.validate(accessToken, uuid) ? SessionStatus.VALID : SessionStatus.INVALID;
        } catch (UnreachableServiceException e) {
            ReAuth.log.error("Failed to check session validity", e);
            return SessionStatus.ERROR;
        }
    }

    private static void setStatus(SessionStatus newStatus) {
        SessionChecker.status = newStatus;
    }
}
