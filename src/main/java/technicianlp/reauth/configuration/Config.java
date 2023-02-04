package technicianlp.reauth.configuration;

import java.io.IOException;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import technicianlp.reauth.ReAuth;
import technicianlp.reauth.crypto.Crypto;

public final class Config {

    private final ForgeConfigSpec spec;
    private ModConfig config;

    private final ProfileList profileList;

    public Config() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Version Number of the Configuration File")
                .defineInRange("version", 3, 3, 3);

        this.profileList = new ProfileList(this, builder);

        this.spec = builder.build();
    }

    public final ForgeConfigSpec getSpec() {
        return this.spec;
    }

    public final void updateConfig(ModConfig config) {
        this.config = config;
        Crypto.updateConfigPath(this.getPath(config));
        this.profileList.updateConfig(config);
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
    private String getPath(ModConfig config) {
        try {
            return config.getFullPath().toRealPath().toString();
        } catch (IOException e) {
            ReAuth.log.error("Could not resolve real path", e);
            return config.getFullPath().toString();
        }
    }
}
