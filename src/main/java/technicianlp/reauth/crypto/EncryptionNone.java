package technicianlp.reauth.crypto;

import technicianlp.reauth.configuration.Profile;

import java.util.Map;

final class EncryptionNone implements ProfileEncryption {

    @Override
    public String decryptFieldOne(String value) {
        return value;
    }

    @Override
    public String decryptFieldTwo(String value) {
        return value;
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
        profile.put(Profile.KEY, Profile.KEY_NONE);
    }

    @Override
    public ProfileEncryption randomizedCopy() {
        return new EncryptionNone();
    }
}
