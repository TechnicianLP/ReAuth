package technicianlp.reauth.crypto;

import java.util.Map;

public interface ProfileEncryption {

    String decryptFieldOne(String encrypted) throws CryptoException;

    String decryptFieldTwo(String encrypted) throws CryptoException;

    String encryptFieldOne(String value) throws CryptoException;

    String encryptFieldTwo(String value) throws CryptoException;

    void saveToProfile(Map<String, String> profile);

    /**
     * creates a new Instance of this {@link ProfileEncryption} with new, random parameters
     */
    ProfileEncryption randomizedCopy();
}
