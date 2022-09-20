package technicianlp.reauth.configuration;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.fabricmc.loader.api.FabricLoader;
import technicianlp.reauth.ReAuth;
import technicianlp.reauth.crypto.Crypto;

import java.io.IOException;

public final class Config {
    public static final String CONFIG_NAME = "reauth";
    private final CommentedFileConfig config;
    private final ProfileList profileList;

    public Config() {
        this.config = CommentedFileConfig.builder(
                FabricLoader.getInstance().getConfigDir().resolve(CONFIG_NAME + ".toml"),
                TomlFormat.instance()).autosave().build();
        config.load();
        Crypto.updateConfigPath(this.getPath(config));
        this.profileList = new ProfileList(this, config);
    }

    public final void save() {
        this.config.save();
    }

    public final ProfileList getProfileList() {
        return this.profileList;
    }

    /**
     * Get the Absolute path of the Config with symlinks resolved.
     * Fall back to "local" path if that lookup fails (somehow)
     */
    private String getPath(CommentedConfig config) {
        try {
            return ((CommentedFileConfig) config).getNioPath().toRealPath().toString();
        } catch (IOException e) {
            ReAuth.log.error("Could not resolve real path", e);
            return ((CommentedFileConfig) config).getNioPath().toString();
        }
    }
}
