package technicianlp.reauth.crypto;

import technicianlp.reauth.configuration.ProfileConstants;

import java.util.Map;

final class EncryptionNone implements ProfileEncryption {

    @Override
    public final String decryptFieldOne(String value) {
        return value;
    }

    @Override
    public final String decryptFieldTwo(String value) {
        return value;
    }

    @Override
    public final String encryptFieldOne(String value) {
        return value;
    }

    @Override
    public final String encryptFieldTwo(String value) {
        return value;
    }

    @Override
    public final void saveToProfile(Map<String, String> profile) {
        profile.put(ProfileConstants.KEY, ProfileConstants.KEY_NONE);
    }

    @Override
    public final ProfileEncryption randomizedCopy() {
        return new EncryptionNone();
    }
}
