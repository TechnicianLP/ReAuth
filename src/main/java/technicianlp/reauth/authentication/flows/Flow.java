package technicianlp.reauth.authentication.flows;

import technicianlp.reauth.authentication.SessionData;
import technicianlp.reauth.configuration.Profile;

import java.util.concurrent.CompletableFuture;

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
}
