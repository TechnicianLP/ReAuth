package technicianlp.reauth.authentication.flows.impl;

import technicianlp.reauth.authentication.SessionData;
import technicianlp.reauth.authentication.flows.FlowCallback;
import technicianlp.reauth.authentication.flows.impl.util.Futures;
import technicianlp.reauth.configuration.Profile;

import java.util.concurrent.CompletableFuture;

public final class UnknownProfileFlow extends FlowBase {

    private final CompletableFuture<SessionData> sessionFuture;

    public UnknownProfileFlow(FlowCallback callback) {
        super(callback);
        this.sessionFuture = Futures.failed(new IllegalArgumentException("Unknown Profile Type"));
        this.sessionFuture.whenComplete(this::onSessionComplete);
    }

    @Override
    public CompletableFuture<SessionData> getSessionFuture() {
        return this.sessionFuture;
    }

    @Override
    public boolean hasProfile() {
        return false;
    }

    @Override
    public CompletableFuture<Profile> getProfileFuture() {
        throw new IllegalStateException("Profile creation not supported");
    }
}
