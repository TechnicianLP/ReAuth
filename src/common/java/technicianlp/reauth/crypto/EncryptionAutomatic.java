package technicianlp.reauth.crypto;

import technicianlp.reauth.configuration.ProfileConstants;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Map;

final class EncryptionAutomatic implements ProfileEncryption {

    private static final int PBE_ROUNDS = 250_000;
    private static final int IV1_OFFSET = 32;
    private static final int IV2_OFFSET = 48;

    private final byte[] keyData;

    private final String path;
    private final byte[] salt;

    public EncryptionAutomatic(String path) {
        this(path, Crypto.randomBytes(16));
    }

    EncryptionAutomatic(String path, String salt) throws CryptoException {
        this(path, Base64.getDecoder().decode(salt));
    }

    EncryptionAutomatic(String path, byte[] salt) throws CryptoException {
        try {
            SecretKeyFactory pbkdf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            SecretKey key = pbkdf.generateSecret(new PBEKeySpec(path.toCharArray(), salt, PBE_ROUNDS, 512));
            this.keyData = key.getEncoded();
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new CryptoException("Failed to derive encryption key", e);
        }
        this.path = path;
        this.salt = salt;
    }

    @Override
    public final String decryptFieldOne(String encrypted) throws CryptoException {
        return this.decrypt(encrypted, IV1_OFFSET);
    }

    @Override
    public final String decryptFieldTwo(String encrypted) throws CryptoException {
        return this.decrypt(encrypted, IV2_OFFSET);
    }

    private String decrypt(String encrypted, int ivOffset) throws CryptoException {
        try {
            byte[] raw = Base64.getDecoder().decode(encrypted);
            byte[] dec = this.crypt(raw, Cipher.DECRYPT_MODE, this.keyData, ivOffset);
            return new String(dec, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("Decryption failed", e);
        }
    }

    @Override
    public final String encryptFieldOne(String value) throws CryptoException {
        return this.encrypt(value, IV1_OFFSET);
    }

    @Override
    public final String encryptFieldTwo(String value) throws CryptoException {
        return this.encrypt(value, IV2_OFFSET);
    }

    private String encrypt(String value, int ivOffset) throws CryptoException {
        try {
            byte[] raw = value.getBytes(StandardCharsets.UTF_8);
            byte[] enc = this.crypt(raw, Cipher.ENCRYPT_MODE, this.keyData, ivOffset);
            return Base64.getEncoder().encodeToString(enc);
        } catch (GeneralSecurityException e) {
            throw new CryptoException("Encryption failed", e);
        }
    }

    /**
     * Encrypt or decrypt the supplied data with the given Key and IV
     */
    private byte[] crypt(byte[] data, int mode, byte[] keyData, int ivOffset) throws GeneralSecurityException {
        SecretKeySpec secretKey = new SecretKeySpec(keyData, 0, 32, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(keyData, ivOffset, 16);

        Cipher aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aes.init(mode, secretKey, ivParameterSpec);
        return aes.doFinal(data);
    }

    @Override
    public final void saveToProfile(Map<String, String> profile) {
        profile.put(ProfileConstants.KEY, ProfileConstants.KEY_AUTO);
        profile.put(ProfileConstants.SALT, Base64.getEncoder().encodeToString(this.salt));
    }

    @Override
    public final ProfileEncryption randomizedCopy() {
        return new EncryptionAutomatic(this.path);
    }
}
