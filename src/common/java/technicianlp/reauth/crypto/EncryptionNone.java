package technicianlp.reauth.crypto;

import technicianlp.reauth.configuration.ProfileConstants;

import java.util.Map;

final class EncryptionNone implements ProfileEncryption {

    @Override
    public String decryptFieldOne(String encrypted) {
        return encrypted;
    }

    @Override
    public String decryptFieldTwo(String encrypted) {
        return encrypted;
    }

    @Override
    public String encryptFieldOne(String value) {
        return value;
    }

    @Override
    public String encryptFieldTwo(String value) {
        return value;
    }

    @Override
    public void saveToProfile(Map<String, String> profile) {
        profile.put(ProfileConstants.KEY, ProfileConstants.KEY_NONE);
    }

    @Override
    public ProfileEncryption randomizedCopy() {
        return new EncryptionNone();
    }
}
