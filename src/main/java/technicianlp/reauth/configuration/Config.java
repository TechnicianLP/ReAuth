package technicianlp.reauth.configuration;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import technicianlp.reauth.ReAuth;
import technicianlp.reauth.crypto.Crypto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Config {

    private final Configuration config;
    private final ProfileList profileList;

    public Config(Path configFile) {
        Crypto.updateConfigPath(this.getPath(configFile));
        this.config = new Configuration(configFile.toFile());
        this.profileList = new ProfileList(this);
    }

    public final void loadConfig() {
        ConfigCategory profiles = this.config.getCategory("profiles");
        profiles.setComment("Saved Profiles. Check Documentation for Info & Syntax");
        this.profileList.loadConfig(profiles);

        // remove old config entries
        if (this.config.hasCategory(Configuration.CATEGORY_GENERAL)) {
            ConfigCategory general = this.config.getCategory(Configuration.CATEGORY_GENERAL);
            general.remove("offlineModeEnabled");
            general.remove("password");
            general.remove("username");
            general.remove("validatorBold");
            general.remove("validatorEnabled");

            if (general.isEmpty()) {
                this.config.removeCategory(general);
            }
        }
        this.save();
    }

    public final void save() {
        this.config.save();
    }

    public final ProfileList getProfileList() {
        return this.profileList;
    }

    /**
     * Get the Absolute path of the Config with symlinks resolved.
     * Fall back to "local" path if that lookup fails (somehow) or config files is missing
     */
    private String getPath(Path config) {
        if (Files.exists(config)) {
            try {
                return config.toRealPath().toString();
            } catch (IOException e) {
                ReAuth.log.error("Could not resolve real path", e);
            }
        }
        return config.toString();
    }

    final Configuration getConfig() {
        return this.config;
    }
}
