package technicianlp.reauth.authentication.flows.impl;

import technicianlp.reauth.authentication.MsAuthAPI;
import technicianlp.reauth.authentication.SessionData;
import technicianlp.reauth.authentication.dto.microsoft.MicrosoftAuthResponse;
import technicianlp.reauth.authentication.dto.xbox.XboxAuthResponse;
import technicianlp.reauth.authentication.flows.AuthorizationCodeFlow;
import technicianlp.reauth.authentication.flows.FlowCallback;
import technicianlp.reauth.authentication.flows.FlowStage;
import technicianlp.reauth.authentication.flows.Tokens;
import technicianlp.reauth.authentication.http.server.AuthenticationCodeServer;
import technicianlp.reauth.configuration.Profile;
import technicianlp.reauth.configuration.ProfileBuilder;
import technicianlp.reauth.crypto.Crypto;
import technicianlp.reauth.crypto.PkceChallenge;
import technicianlp.reauth.crypto.ProfileEncryption;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public final class MicrosoftCodeFlow extends FlowBase implements AuthorizationCodeFlow {

    private final String loginUrl;

    private final CompletableFuture<SessionData> session;
    private final CompletableFuture<Profile> profile;

    private AuthenticationCodeServer codeServer;

    public MicrosoftCodeFlow(boolean persist, FlowCallback callback) {
        super(callback);
        PkceChallenge pkceChallenge = Crypto.createPkceChallenge();
        this.loginUrl = MsAuthAPI.getLoginUrl(persist, pkceChallenge.getChallenge());

        CompletableFuture<String> codeStage = new CompletableFuture<>();
        CompletableFuture<String> pkceVerifier = CompletableFuture.completedFuture(pkceChallenge.getVerifier());
        CompletableFuture<MicrosoftAuthResponse> ms = codeStage.thenCombineAsync(pkceVerifier, this.wrapStep(FlowStage.MS_REDEEM_AUTH_CODE, MsAuthAPI::redeemAuthorizationCode), this.executor);
        CompletableFuture<XboxAuthResponse> xasu = ms.thenApply(MicrosoftAuthResponse::getAccessToken)
                .thenApplyAsync(this.wrapStep(FlowStage.MS_AUTH_XASU, MsAuthAPI::authenticateXASU), this.executor);
        XboxAuthenticationFlow flow = new XboxAuthenticationFlow(xasu.thenApply(XboxAuthResponse::getToken), callback);
        this.session = flow.getSession();
        this.session.whenComplete(this::onSessionComplete);
        this.registerDependantStages(codeStage, ms, xasu, this.session);
        this.registerDependantFlow(flow);

        if (persist) {
            CompletableFuture<Tokens> tokens = ms.thenCombine(xasu, Tokens::new);
            CompletableFuture<ProfileEncryption> encryption = CompletableFuture.supplyAsync(Crypto::newEncryption, this.executor);
            CompletableFuture<ProfileBuilder> builder = this.session.thenCombine(encryption, ProfileBuilder::new);
            this.profile = builder.thenCombine(tokens, ProfileBuilder::buildMicrosoft);
            this.profile.whenComplete(this::onProfileComplete);
        } else {
            this.profile = null;
        }

        this.executor.execute(() -> {
            try {
                this.codeServer = new AuthenticationCodeServer(MsAuthAPI.port, this.loginUrl, codeStage, this.executor);
                this.step(FlowStage.MS_AWAIT_AUTH_CODE);
            } catch (IOException | NoClassDefFoundError exception) {
                codeStage.completeExceptionally(exception);
            }
        });
    }

    @Override
    public final CompletableFuture<SessionData> getSession() {
        return this.session;
    }

    @Override
    public boolean hasProfile() {
        return this.profile != null;
    }

    @Override
    public final CompletableFuture<Profile> getProfile() {
        if (this.profile != null) {
            return this.profile;
        } else {
            throw new IllegalStateException("Persistence not requested");
        }
    }

    @Override
    public final String getLoginUrl() {
        return this.loginUrl;
    }

    @Override
    public final void cancel() {
        super.cancel();
        this.executor.execute(() -> this.codeServer.stop(true));
    }
}
