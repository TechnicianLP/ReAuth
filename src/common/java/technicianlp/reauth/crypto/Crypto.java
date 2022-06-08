package technicianlp.reauth.crypto;

import technicianlp.reauth.authentication.flows.FlowCallback;
import technicianlp.reauth.authentication.flows.impl.util.Futures;
import technicianlp.reauth.configuration.Profile;

import java.security.SecureRandom;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class Crypto {

    private static String configPath = "";

    public static CompletableFuture<ProfileEncryption> getProfileEncryption(Profile profile, FlowCallback callback) {
        switch (profile.getValue(Profile.KEY)) {
            case Profile.KEY_AUTO:
                return CompletableFuture.supplyAsync(() -> new EncryptionAutomatic(configPath, profile.getValue(Profile.SALT)), callback.getExecutor());
            case Profile.KEY_NONE:
                return CompletableFuture.completedFuture(new EncryptionNone());
            default:
                return Futures.failed(new IllegalArgumentException("Unknown Encryption Type"));
        }
    }

    public static CompletableFuture<ProfileEncryption> newEncryption(Executor executor) {
        return CompletableFuture.supplyAsync(() -> new EncryptionAutomatic(configPath), executor);
    }

    public static byte[] randomBytes(int length) {
        byte[] salt = new byte[length];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    public static PkceChallenge createPkceChallenge() {
        return new PkceChallenge();
    }

    public static void updateConfigPath(String configPath) {
        Crypto.configPath = configPath;
    }
}
