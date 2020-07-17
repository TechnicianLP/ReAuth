package technicianlp.reauth;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.ConfigHolder;
import me.sargunvohra.mcmods.autoconfig1u.ConfigManager;
import me.sargunvohra.mcmods.autoconfig1u.serializer.Toml4jConfigSerializer;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

public final class ConfigWrapper {

    public static final String CONFIG_NAME = "../reauth";

    static void registerConfig() {
        AutoConfig.register(Configuration.class, Toml4jConfigSerializer::new);
    }

    private Configuration config;
    private Crypto crypto;

    public ConfigWrapper() {
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

    public void onLoad(Configuration config0) {
        this.config = config0;
        if (config.version == 1) {
            convertConfigV1();
            return;
        }

        Configuration.Credentials credentials = config.credentials;
        byte[] salt = null;
        boolean saltLoaded = false;
        String saltRaw = credentials.salt;
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
            credentials.salt = Base64.getEncoder().encodeToString(salt);
        }
        if (crypto != null)
            crypto.setup(getPath(), salt);
    }

    /**
     * Migrate Version 1 > 2
     */
    private void convertConfigV1() {
        config.version = 2;
        byte[] salt = createSalt();
        Configuration.Credentials credentials = config.credentials;
        credentials.salt = Base64.getEncoder().encodeToString(salt);
        if (crypto != null)
            crypto.setup(getPath(), salt);
        setCredentials("", credentials.username, credentials.password);
    }

    /**
     * Path is used as the Key for encryption
     * Resolve symlinks so as to allow the resulting hash to be the same across symlinks
     * Fall back to "local" path if that lookup fails (somehow)
     */
    private String getPath() {
        Path configFile = FabricLoader.getInstance().getConfigDirectory().toPath().resolve(CONFIG_NAME + ".toml");
        try {
            if (Files.exists(configFile))
                return configFile.toRealPath().toString();
        } catch (IOException e) {
            ReAuth.log.error("Could not resolve real path", e);
        }
        return configFile.toString();
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
        Configuration.Credentials credentials = config.credentials;
        credentials.profile = profileName;
        if (!username.isEmpty())
            username = crypto.encryptString(username);
        credentials.username = username;
        if (!password.isEmpty())
            password = crypto.encryptString(password);
        credentials.password = password;

        ConfigHolder<?> cfg = AutoConfig.getConfigHolder(Configuration.class);
        if (cfg instanceof ConfigManager) {
            ((ConfigManager<?>) cfg).save();
        } else {
            ReAuth.log.warn("Unknown ConfigHolder: cannot save config");
        }
    }

    /**
     * Decrypt Username
     * Empty String means offline Username -> return that
     */
    public String getUsername() {
        Configuration.Credentials credentials = config.credentials;
        String username = credentials.username;
        if (username.isEmpty())
            return credentials.profile;
        if (crypto != null)
            return crypto.decryptString(username);
        return "";
    }

    /**
     * Decrypt Password
     * Empty String is not decrypted
     */
    public String getPassword() {
        String password = config.credentials.password;
        if (!password.isEmpty() && crypto != null)
            return crypto.decryptString(password);
        return "";
    }

    public String getProfile() {
        return config.credentials.profile;
    }

    /**
     * Is Cryptography available
     */
    public boolean hasCrypto() {
        return crypto != null;
    }

    public boolean hasCredentials() {
        return hasCrypto() &&
                !config.credentials.username.isEmpty() &&
                !config.credentials.password.isEmpty() &&
                !config.credentials.profile.isEmpty();
    }
}
