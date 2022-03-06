package technicianlp.reauth.authentication.flows.impl;

import technicianlp.reauth.authentication.SessionData;
import technicianlp.reauth.authentication.YggdrasilAPI;
import technicianlp.reauth.authentication.flows.FlowCallback;
import technicianlp.reauth.authentication.flows.FlowStage;
import technicianlp.reauth.configuration.Profile;
import technicianlp.reauth.configuration.ProfileBuilder;
import technicianlp.reauth.crypto.Crypto;
import technicianlp.reauth.crypto.ProfileEncryption;

import java.util.concurrent.CompletableFuture;

public final class MojangAuthenticationFlow extends FlowBase {

    private final CompletableFuture<SessionData> session;

    private final CompletableFuture<Profile> profile;

    public MojangAuthenticationFlow(String username, String password, boolean save, FlowCallback callback) {
        super(callback);
        this.session = CompletableFuture.supplyAsync(this.wrapStep(FlowStage.YGG_AUTH, () -> YggdrasilAPI.login(username, password)), this.executor);
        this.session.whenComplete(this::onFlowComplete);
        this.registerDependantStages(this.session);

        if (save) {
            CompletableFuture<ProfileEncryption> encryption = Crypto.newEncryption(this.executor);
            CompletableFuture<ProfileBuilder> builder = this.session.thenCombine(encryption, ProfileBuilder::new);
            this.profile = builder.thenApply(b -> b.buildMojang(username, password));
        } else {
            this.profile = null;
        }
    }

    public MojangAuthenticationFlow(Profile profile, FlowCallback callback) {
        super(callback);
        this.step(FlowStage.CRYPTO_INIT);
        CompletableFuture<ProfileEncryption> encryption = Crypto.getProfileEncryption(profile, callback);
        CompletableFuture<String> usernameDec = encryption.thenCombineAsync(profile.get(Profile.USERNAME), ProfileEncryption::decryptFieldOne, this.executor);
        CompletableFuture<String> passwordDec = encryption.thenCombineAsync(profile.get(Profile.PASSWORD), ProfileEncryption::decryptFieldTwo, this.executor);

        this.session = usernameDec.thenCombineAsync(passwordDec, this.wrapStep(FlowStage.YGG_AUTH, YggdrasilAPI::login), this.executor);
        this.registerDependantStages(encryption, usernameDec, passwordDec, this.session);

        this.profile = CompletableFuture.completedFuture(profile);
        this.profile.thenRun(this::onProfileComplete);
    }

    @Override
    public final CompletableFuture<SessionData> getSession() {
        return this.session;
    }

    @Override
    public final boolean hasProfile() {
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
}
