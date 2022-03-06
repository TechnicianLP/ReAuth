package technicianlp.reauth.authentication.flows.impl;

import technicianlp.reauth.authentication.SessionData;
import technicianlp.reauth.authentication.flows.FlowCallback;
import technicianlp.reauth.configuration.Profile;

import java.util.concurrent.CompletableFuture;

public final class UnknownProfileFlow extends FlowBase {

    private final CompletableFuture<SessionData> future;

    public UnknownProfileFlow(FlowCallback callback) {
        super(callback);
        this.future = CompletableFuture.failedFuture(new IllegalArgumentException("Unknown Profile Type"));
        this.future.whenComplete(this::onFlowComplete);
    }

    @Override
    public CompletableFuture<SessionData> getSession() {
        return this.future;
    }

    @Override
    public boolean hasProfile() {
        return false;
    }

    @Override
    public CompletableFuture<Profile> getProfile() {
        throw new IllegalStateException("Profile creation not supported");
    }
}
