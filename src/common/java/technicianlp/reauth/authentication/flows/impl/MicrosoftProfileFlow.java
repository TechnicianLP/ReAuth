package technicianlp.reauth.authentication.flows.impl;

import technicianlp.reauth.authentication.MsAuthAPI;
import technicianlp.reauth.authentication.SessionData;
import technicianlp.reauth.authentication.dto.microsoft.MicrosoftAuthResponse;
import technicianlp.reauth.authentication.dto.xbox.XboxAuthResponse;
import technicianlp.reauth.authentication.flows.FlowCallback;
import technicianlp.reauth.authentication.flows.FlowStage;
import technicianlp.reauth.authentication.flows.Tokens;
import technicianlp.reauth.authentication.flows.impl.util.Futures;
import technicianlp.reauth.authentication.http.Response;
import technicianlp.reauth.configuration.Profile;
import technicianlp.reauth.configuration.ProfileBuilder;
import technicianlp.reauth.configuration.ProfileConstants;
import technicianlp.reauth.crypto.Crypto;
import technicianlp.reauth.crypto.ProfileEncryption;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class MicrosoftProfileFlow extends FlowBase {

    private final CompletableFuture<ProfileEncryption> encryption;
    private final CompletableFuture<SessionData> session;

    private final XboxAuthenticationFlow xboxFlow1;

    private final CompletableFuture<Boolean> refreshRequired;
    private final CompletableFuture<Profile> profileFuture;

    /**
     * tries to login using the stored accessToken.
     * When authentication fails due to an expired token, the stored refreshToken is used to acquire a new accessToken followed by a second login attempt
     *
     * @see XboxAuthenticationFlow#isExpiredToken(Response, Throwable)
     */
    public MicrosoftProfileFlow(Profile profile, FlowCallback callback) {
        super(callback);

        this.session = new CompletableFuture<>();
        this.session.whenComplete(this::onSessionComplete);
        CompletableFuture<Profile> profileFuture = CompletableFuture.completedFuture(profile);
        this.encryption = profileFuture.thenApplyAsync(this.wrapStep(FlowStage.CRYPTO_INIT, Crypto::getProfileEncryption), this.executor);
        this.refreshRequired = new CompletableFuture<>();

        CompletableFuture<String> xblTokenDec = this.encryption.thenCombineAsync(profile.get(ProfileConstants.XBL_TOKEN), ProfileEncryption::decryptFieldOne, this.executor);
        this.xboxFlow1 = new XboxAuthenticationFlow(xblTokenDec, callback);
        this.xboxFlow1.getSession().whenComplete(this::onComplete);
        this.registerDependantStages(this.encryption, xblTokenDec);
        this.registerDependantFlow(this.xboxFlow1);

        CompletableFuture<String> refreshTokenEnc = Futures.composeConditional(this.refreshRequired, profile.get(ProfileConstants.REFRESH_TOKEN), Futures.cancelled());
        CompletableFuture<String> refreshTokenDec = this.encryption.thenCombineAsync(refreshTokenEnc, ProfileEncryption::decryptFieldTwo, this.executor);
        CompletableFuture<MicrosoftAuthResponse> auth = refreshTokenDec.thenApplyAsync(this.wrapStep(FlowStage.MS_REDEEM_REFRESH_TOKEN, MsAuthAPI::redeemRefreshToken), this.executor);
        CompletableFuture<XboxAuthResponse> xasu = auth.thenApply(MicrosoftAuthResponse::getAccessToken)
                .thenApplyAsync(this.wrapStep(FlowStage.MS_AUTH_XASU, MsAuthAPI::authenticateXASU), this.executor);
        CompletableFuture<String> xblToken = xasu.thenApply(XboxAuthResponse::getToken);
        XboxAuthenticationFlow xboxFlow2 = new XboxAuthenticationFlow(xblToken, callback);
        xboxFlow2.getSession().whenComplete(this::onComplete);
        this.registerDependantStages(this.refreshRequired, refreshTokenDec, auth);
        this.registerDependantFlow(xboxFlow2);

        CompletableFuture<Tokens> tokens = auth.thenCombine(xasu, Tokens::new);
        this.profileFuture = Futures.composeConditional(this.refreshRequired, () -> this.constructProfile(tokens), CompletableFuture.completedFuture(profile));
        this.profileFuture.whenComplete(this::onProfileComplete);
    }

    /**
     * A reimagined version of {@link CompletableFuture#applyToEither(CompletionStage, Function)} that makes guarantees on exceptional behaviour and triggers the fallback computation if required.
     * <p>
     * completes {@link MicrosoftProfileFlow#session} with the provided session if completed normally (throwable = null)<br>
     * if the first {@link XboxAuthenticationFlow} completed exceptionally caused by an expired token the refresh-flow is triggered by completing {@link MicrosoftProfileFlow#refreshRequired}<br>
     * otherwise {@link MicrosoftProfileFlow#session} is completed exceptionally with the supplied throwable.
     *
     * @see CompletionStage#whenComplete(BiConsumer)
     * @see CompletableFuture#applyToEither(CompletionStage, Function)
     */
    private void onComplete(SessionData sessionData, Throwable throwable) {
        if (throwable == null) {
            this.session.complete(sessionData);
            this.refreshRequired.complete(false);
        } else {
            if (this.refreshRequired.isDone()) {
                this.session.completeExceptionally(throwable);
                this.profileFuture.cancel(true);
            }
            if (this.xboxFlow1.hasExpiredTokenError()) {
                this.refreshRequired.complete(true);
            } else {
                this.session.completeExceptionally(throwable);
                this.refreshRequired.cancel(true);
            }
        }
    }

    @Override
    public final CompletableFuture<SessionData> getSession() {
        return this.session;
    }

    @Override
    public final boolean hasProfile() {
        return true;
    }

    @Override
    public final CompletableFuture<Profile> getProfile() {
        return this.profileFuture;
    }

    private CompletableFuture<Profile> constructProfile(CompletableFuture<Tokens> tokens) {
        CompletableFuture<ProfileEncryption> encryption = this.encryption.thenApplyAsync(ProfileEncryption::randomizedCopy, this.executor);
        CompletableFuture<ProfileBuilder> builder = this.session.thenCombine(encryption, ProfileBuilder::new);
        return builder.thenCombineAsync(tokens, ProfileBuilder::buildMicrosoft, this.executor);
    }
}
