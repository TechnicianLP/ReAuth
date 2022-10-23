package technicianlp.reauth.configuration;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.fabricmc.loader.api.FabricLoader;
import technicianlp.reauth.ReAuth;
import technicianlp.reauth.crypto.Crypto;

import java.io.IOException;

public final class Config {
    public static final String CONFIG_NAME = "reauth";
    public static final String VERSION_PATH = "version";

    private final CommentedFileConfig config;
    private final ProfileList profileList;

    public Config() {
        this.config = CommentedFileConfig.builder(
            FabricLoader.getInstance().getConfigDir().resolve(CONFIG_NAME + ".toml"),
            TomlFormat.instance()).autosave().build();
        this.config.load();
        Crypto.updateConfigPath(getPath(this.config));
        this.config.set(VERSION_PATH, 3);
        this.config.setComment(VERSION_PATH, "Version Number of the Configuration File");
        this.profileList = new ProfileList(this, this.config);
    }

    public void save() {
        this.config.save();
    }

    public ProfileList getProfileList() {
        return this.profileList;
    }

    /**
     * Get the Absolute path of the Config with symlinks resolved. Fall back to "local" path if that lookup fails
     * (somehow)
     */
    private static String getPath(FileConfig config) {
        try {
            return config.getNioPath().toRealPath().toString();
        } catch (IOException e) {
            ReAuth.log.error("Could not resolve real path", e);
            return config.getNioPath().toString();
        }
    }
}
