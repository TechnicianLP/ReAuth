package technicianlp.reauth.authentication.flows;

import technicianlp.reauth.authentication.SessionData;
import technicianlp.reauth.configuration.Profile;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface Flow {

    void cancel();

    CompletableFuture<SessionData> getSessionFuture();

    boolean hasProfile();

    /**
     * Returns a {@link Profile} containing all required profile information.
     *
     * @throws IllegalStateException if a profile cannot be created
     */
    CompletableFuture<Profile> getProfileFuture();

    default void thenRunAsync(Runnable action, Executor executor) {
        CompletableFuture<?> target;
        if (this.hasProfile()) {
            target = CompletableFuture.allOf(this.getSessionFuture(), this.getProfileFuture());
        } else {
            target = this.getSessionFuture();
        }
        target.thenRunAsync(action, executor);
    }
}
