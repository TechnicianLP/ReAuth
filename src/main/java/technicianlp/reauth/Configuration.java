package technicianlp.reauth;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class Configuration {

    private final ForgeConfigSpec spec;

    private final ForgeConfigSpec.IntValue versionSpec;
    private final ForgeConfigSpec.ConfigValue<String> profileNameSpec;
    private final ForgeConfigSpec.ConfigValue<String> saltSpec;
    private final ForgeConfigSpec.ConfigValue<String> usernameSpec;
    private final ForgeConfigSpec.ConfigValue<String> passwordSpec;

    private Crypto crypto;

    public Configuration() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        versionSpec = builder
                .comment("Version Number of the Configuration File")
                .defineInRange("version", 2, 1, 2);

        builder.comment("Credentials for login, encrypted with AES-CBC-PKCS5Padding and PBKDF2WithHmacSHA512 " +
                "based on the Path of this file and the contained Salt.\n" +
                "Manually editing one of the Encrypted Values or the Salt, is inadvisable - Reset them to \"\" if required\n" +
                "Keep in mind that while the credentials are no longer humanly readable, they could still be decrypted by malicious third parties").push("credentials");
        saltSpec = builder
                .comment("One of the values required to decrypt the credentials\n" +
                        "See https://en.wikipedia.org/wiki/Salt_(cryptography) for details")
                .define("salt", "");
        profileNameSpec = builder
                .comment("The Name of the last used Profile")
                .define("profile", "");
        usernameSpec = builder
                .comment("Your Username (encrypted)")
                .define("username", "");
        passwordSpec = builder
                .comment("Your Password (encrypted)")
                .define("password", "");
        builder.pop();

        spec = builder.build();

        if (!Boolean.getBoolean("mods.reauth.disableCrypto")) {
            try {
                this.crypto = new Crypto();
            } catch (GeneralSecurityException e) {
                ReAuth.log.error("Unable to locate cryptographic algorithms. Credentials cannot be saved", e);
            }
        } else {
            ReAuth.log.error("Crypto disabled by commandline");
        }
    }

    public ForgeConfigSpec getSpec() {
        return spec;
    }

    public void setConfig(ModConfig config) {
        if (versionSpec.get() == 1) {
            convertConfigV1(config);
            return;
        }

        byte[] salt = null;
        boolean saltLoaded = false;
        String saltRaw = saltSpec.get();
        if (!saltRaw.isEmpty()) {
            try {
                salt = Base64.getDecoder().decode(saltRaw);
                if (salt.length == 16)
                    saltLoaded = true;
                else
                    ReAuth.log.error("Salt corrupted, saved credentials cannot be recovered");
            } catch (IllegalArgumentException e) {
                ReAuth.log.error("Could not load salt, saved credentials cannot be recovered", e);
            }
        }
        if (!saltLoaded) {
            salt = createSalt();
            saltSpec.set(Base64.getEncoder().encodeToString(salt));
        }
        if (crypto != null)
            crypto.setup(getPath(config), salt);
    }

    /**
     * Migrate Version 1 > 2
     */
    private void convertConfigV1(ModConfig config) {
        versionSpec.set(2);
        byte[] salt = createSalt();
        saltSpec.set(Base64.getEncoder().encodeToString(salt));
        if (crypto != null)
            crypto.setup(getPath(config), salt);
        setCredentials("", usernameSpec.get(), passwordSpec.get());
    }

    /**
     * Path is used as the Key for encryption
     * Resolve symlinks so as to allow the resulting hash to be the same across symlinks
     * Fall back to "local" path if that lookup fails (somehow)
     */
    private String getPath(ModConfig config) {
        try {
            return config.getFullPath().toRealPath().toString();
        } catch (IOException e) {
            ReAuth.log.error("Could not resolve real path", e);
            return config.getFullPath().toString();
        }
    }

    private byte[] createSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    /**
     * Updates the credentials in Config
     * Credentials are encrypted; If encryption is unavailable they are not saved
     * Empty String is not encrypted
     */
    public void setCredentials(String profileName, String username, String password) {
        if (crypto == null)
            return;
        profileNameSpec.set(profileName);
        if (!username.isEmpty())
            username = crypto.encryptString(username);
        usernameSpec.set(username);
        if (!password.isEmpty())
            password = crypto.encryptString(password);
        passwordSpec.set(password);
        spec.save();
    }

    /**
     * Decrypt Username
     * Empty String means offline Username -> return that
     */
    public String getUsername() {
        String username = usernameSpec.get();
        if (username.isEmpty())
            return profileNameSpec.get();
        if (crypto != null)
            return crypto.decryptString(username);
        return "";
    }

    public String getProfile() {
        return profileNameSpec.get();
    }

    /**
     * Decrypt Password
     * Empty String is not decrypted
     */
    public String getPassword() {
        String password = passwordSpec.get();
        if (!password.isEmpty() && crypto != null)
            return crypto.decryptString(password);
        return "";
    }

    /**
     * Is Cryptography available
     */
    public boolean hasCrypto() {
        return crypto != null;
    }

    public boolean hasCredentials() {
        return hasCrypto() &&
                !usernameSpec.get().isEmpty() &&
                !passwordSpec.get().isEmpty() &&
                !profileNameSpec.get().isEmpty();
    }
}
