package technicianlp.reauth.authentication.flows.impl;

import technicianlp.reauth.authentication.SessionData;
import technicianlp.reauth.authentication.flows.FlowCallback;
import technicianlp.reauth.authentication.flows.impl.util.Futures;
import technicianlp.reauth.configuration.Profile;

import java.util.concurrent.CompletableFuture;

public final class UnknownProfileFlow extends FlowBase {

    private final CompletableFuture<SessionData> future;

    public UnknownProfileFlow(FlowCallback callback) {
        super(callback);
        this.future = Futures.failed(new IllegalArgumentException("Unknown Profile Type"));
        this.future.whenComplete(this::onSessionComplete);
    }

    @Override
    public final CompletableFuture<SessionData> getSession() {
        return this.future;
    }

    @Override
    public final boolean hasProfile() {
        return false;
    }

    @Override
    public final CompletableFuture<Profile> getProfile() {
        throw new IllegalStateException("Profile creation not supported");
    }
}
