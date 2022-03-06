package technicianlp.reauth.authentication.flows.impl;

import technicianlp.reauth.authentication.MsAuthAPI;
import technicianlp.reauth.authentication.SessionData;
import technicianlp.reauth.authentication.dto.mojang.MojangAuthResponse;
import technicianlp.reauth.authentication.dto.mojang.ProfileResponse;
import technicianlp.reauth.authentication.dto.xbox.XboxAuthResponse;
import technicianlp.reauth.authentication.flows.FlowCallback;
import technicianlp.reauth.authentication.flows.FlowStage;
import technicianlp.reauth.authentication.http.InvalidResponseException;
import technicianlp.reauth.authentication.http.Response;
import technicianlp.reauth.authentication.http.UnreachableServiceException;
import technicianlp.reauth.configuration.Profile;

import java.util.concurrent.CompletableFuture;

final class XboxAuthenticationFlow extends FlowBase {

    private static final String XSTS_ERR_TOKEN_EXPIRED = "" + 0x8015DC22;
    private static final String XSTS_ERR_TOKEN_INVALID = "" + 0x8015DC26;

    private final CompletableFuture<SessionData> session;
    private final CompletableFuture<Boolean> expiredToken;

    public XboxAuthenticationFlow(CompletableFuture<String> xblToken, FlowCallback callback) {
        super(callback);

        CompletableFuture<Response<XboxAuthResponse>> xstsRaw = xblToken.thenApplyAsync(this.wrapStep(FlowStage.MS_AUTH_XSTS, MsAuthAPI::authenticateXSTS), this.executor);
        this.expiredToken = xstsRaw.handle(this::isExpiredToken);
        CompletableFuture<XboxAuthResponse> xsts = xstsRaw.thenApply(this.wrap(Response::get));
        CompletableFuture<MojangAuthResponse> mojang = xsts.thenApplyAsync(this.wrapStep(FlowStage.MS_AUTH_MOJANG, this::authenticateMojang), this.executor);
        CompletableFuture<ProfileResponse> profile = mojang.thenApply(MojangAuthResponse::getToken)
                .thenApplyAsync(this.wrapStep(FlowStage.MS_FETCH_PROFILE, MsAuthAPI::fetchProfile), this.executor);
        this.session = mojang.thenCombine(profile, this::makeSession);

        this.registerDependantStages(xsts, mojang, profile, this.session);
    }

    private MojangAuthResponse authenticateMojang(XboxAuthResponse xsts) throws UnreachableServiceException, InvalidResponseException {
        return MsAuthAPI.authenticateMojang(xsts.token, xsts.userhash);
    }

    private SessionData makeSession(MojangAuthResponse auth, ProfileResponse profile) {
        return new SessionData(profile.name, profile.uuid, auth.token);
    }

    /**
     * checks whether the passed response contains an error caused expired/invalid XASU-Token.
     * If a valid response is passed or the request failed with an exception false is returned.
     */
    private boolean isExpiredToken(Response<XboxAuthResponse> response, Throwable throwable) {
        if (response.isValid() || throwable != null) {
            return false;
        }
        XboxAuthResponse rawResponse = response.getUnchecked();
        if (rawResponse != null) {
            return XSTS_ERR_TOKEN_EXPIRED.equals(rawResponse.error) || XSTS_ERR_TOKEN_INVALID.equals(rawResponse.error);
        }
        return false;
    }

    @Override
    public final CompletableFuture<SessionData> getSession() {
        return this.session;
    }

    final CompletableFuture<Boolean> hasTokenExpiredError() {
        return this.expiredToken;
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
