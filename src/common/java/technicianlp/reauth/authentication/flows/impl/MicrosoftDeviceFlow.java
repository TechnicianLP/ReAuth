package technicianlp.reauth.authentication.flows.impl;

import technicianlp.reauth.ReAuth;
import technicianlp.reauth.authentication.MsAuthAPI;
import technicianlp.reauth.authentication.SessionData;
import technicianlp.reauth.authentication.dto.microsoft.MicrosoftAuthResponse;
import technicianlp.reauth.authentication.dto.microsoft.device.MicrosoftAuthDeviceResponse;
import technicianlp.reauth.authentication.dto.xbox.XboxAuthResponse;
import technicianlp.reauth.authentication.flows.DeviceCodeFlow;
import technicianlp.reauth.authentication.flows.FlowCallback;
import technicianlp.reauth.authentication.flows.FlowStage;
import technicianlp.reauth.authentication.flows.Tokens;
import technicianlp.reauth.authentication.http.InvalidResponseException;
import technicianlp.reauth.authentication.http.Response;
import technicianlp.reauth.authentication.http.UnreachableServiceException;
import technicianlp.reauth.configuration.Profile;
import technicianlp.reauth.configuration.ProfileBuilder;
import technicianlp.reauth.crypto.Crypto;
import technicianlp.reauth.crypto.ProfileEncryption;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

public final class MicrosoftDeviceFlow extends FlowBase implements DeviceCodeFlow {

    private final CompletableFuture<SessionData> sessionFuture;
    private final CompletableFuture<Profile> profile;

    private final CompletableFuture<String> loginUrl;
    private final CompletableFuture<String> code;

    private final CompletableFuture<MicrosoftAuthResponse> auth;

    public MicrosoftDeviceFlow(boolean persist, FlowCallback callback) {
        super(callback);

        CompletableFuture<MicrosoftAuthDeviceResponse> deviceResponse = CompletableFuture.completedFuture(persist)
            .thenApplyAsync(this.wrapStep(FlowStage.MS_REQUEST_DEVICE_CODE, MsAuthAPI::requestDeviceCode),
                this.executor);
        this.loginUrl = deviceResponse.thenApply(MicrosoftAuthDeviceResponse::getVerificationUri);
        this.code = deviceResponse.thenApply(MicrosoftAuthDeviceResponse::getUserCode);
        this.auth = deviceResponse.thenApplyAsync(this::pollForCode, this.executor);
        CompletableFuture<XboxAuthResponse> xasu = this.auth.thenApply(MicrosoftAuthResponse::getAccessToken)
            .thenApplyAsync(this.wrapStep(FlowStage.MS_AUTH_XASU, MsAuthAPI::authenticateXASU), this.executor);

        XboxAuthenticationFlow flow = new XboxAuthenticationFlow(xasu.thenApply(XboxAuthResponse::getToken), callback);
        this.sessionFuture = flow.getSessionFuture();
        this.sessionFuture.whenComplete(this::onSessionComplete);
        this.registerDependantStages(deviceResponse, this.auth, xasu, this.sessionFuture);
        this.registerDependantFlow(flow);

        if (persist) {
            CompletableFuture<Tokens> tokens = this.auth.thenCombine(xasu, Tokens::new);
            CompletableFuture<ProfileEncryption> encryption =
                CompletableFuture.supplyAsync(Crypto::newEncryption, this.executor);
            CompletableFuture<ProfileBuilder> builder = this.sessionFuture.thenCombine(encryption, ProfileBuilder::new);
            this.profile = builder.thenCombine(tokens, ProfileBuilder::buildMicrosoft);
            this.profile.whenComplete(this::onProfileComplete);
        } else {
            this.profile = null;
        }
    }

    /**
     * Poll the Microsoft Token Endpoint until the user has completed authentication. Interval between Polls is
     * specified by {@link MicrosoftAuthDeviceResponse#interval}.
     */
    private MicrosoftAuthResponse pollForCode(MicrosoftAuthDeviceResponse deviceResponse) {
        this.step(FlowStage.MS_POLL_DEVICE_CODE);
        while (!this.auth.isDone()) {
            ReAuth.log.debug("Polling Microsoft for token");
            try {
                Response<MicrosoftAuthResponse> response = MsAuthAPI.redeemDeviceCode(deviceResponse.deviceCode);
                if (response.isValid()) {
                    ReAuth.log.info("Authorization received");
                    return response.get();
                } else {
                    MicrosoftAuthResponse responseError = response.getResponse();
                    if ("authorization_pending".equals(responseError.getError())) {
                        ReAuth.log.debug("Authorization is still pending - continue polling");
                    } else {
                        ReAuth.log.info("Authorization failed: {}", responseError.getError());
                        // will throw InvalidResponseException
                        return response.get();
                    }
                }
            } catch (UnreachableServiceException | InvalidResponseException e) {
                throw new CompletionException(e);
            }
            try {
                TimeUnit.SECONDS.sleep(deviceResponse.interval);
            } catch (InterruptedException e) {
                throw new CompletionException(e);
            }
        }
        // Polling cancelled
        return null;
    }

    @Override
    public CompletableFuture<SessionData> getSessionFuture() {
        return this.sessionFuture;
    }

    @Override
    public boolean hasProfile() {
        return this.profile != null;
    }

    @Override
    public CompletableFuture<Profile> getProfileFuture() {
        if (this.profile != null) {
            return this.profile;
        } else {
            throw new IllegalStateException("Persistence not requested");
        }
    }

    @Override
    public CompletableFuture<String> getLoginUrl() {
        return this.loginUrl;
    }

    @Override
    public CompletableFuture<String> getCode() {
        return this.code;
    }
}
