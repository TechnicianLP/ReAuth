package technicianlp.reauth.crypto;

import technicianlp.reauth.ReAuth;
import technicianlp.reauth.authentication.flows.FlowCallback;
import technicianlp.reauth.configuration.Profile;

import javax.crypto.Cipher;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class Crypto {

    private static String configPath = "";

    public static CompletableFuture<ProfileEncryption> getProfileEncryption(Profile profile, FlowCallback callback) {
        return switch (profile.getValue(Profile.KEY)) {
            case Profile.KEY_AUTO -> CompletableFuture.supplyAsync(() -> new EncryptionAutomatic(configPath, profile.getValue(Profile.SALT)), callback.getExecutor());
            case Profile.KEY_NONE -> CompletableFuture.completedFuture(new EncryptionNone());
            default -> CompletableFuture.failedFuture(new IllegalArgumentException("Unknown Encryption Type"));
        };
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

    public static void init() {
        try {
            if (Cipher.getMaxAllowedKeyLength("AES") != Integer.MAX_VALUE) {
                ReAuth.log.warn("Cryptography is explicitly restricted in this Java installation");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("AES unavailable", e);
        }
    }

    public static void updateConfigPath(String configPath) {
        Crypto.configPath = configPath;
    }
}
