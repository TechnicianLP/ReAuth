package technicianlp.reauth.crypto;

import technicianlp.reauth.configuration.Profile;
import technicianlp.reauth.configuration.ProfileConstants;

import java.security.SecureRandom;

public final class Crypto {

    private static String configPath = "";

    public static ProfileEncryption getProfileEncryption(Profile profile) {
        switch (profile.getValue(ProfileConstants.KEY)) {
            case ProfileConstants.KEY_AUTO:
                return new EncryptionAutomatic(configPath, profile.getValue(ProfileConstants.SALT));
            case ProfileConstants.KEY_NONE:
                return new EncryptionNone();
            default:
                throw new IllegalArgumentException("Unknown Encryption Type");
        }
    }

    public static ProfileEncryption newEncryption() {
        return new EncryptionAutomatic(configPath);
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
