package technicianlp.reauth.authentication.flows;

import technicianlp.reauth.authentication.SessionData;
import technicianlp.reauth.configuration.Profile;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface Flow {

    void cancel();

    CompletableFuture<SessionData> getSession();

    boolean hasProfile();

    /**
     * Returns a {@link Profile} containing all required profile information.
     *
     * @throws IllegalStateException if a profile cannot be created
     */
    CompletableFuture<Profile> getProfile();

    default void thenRunAsync(Runnable runnable, Executor executor) {
        CompletableFuture<?> target;
        if (!this.hasProfile()) {
            target = this.getSession();
        } else {
            target = CompletableFuture.allOf(this.getSession(), this.getProfile());
        }
        target.thenRunAsync(runnable, executor);
    }
}
