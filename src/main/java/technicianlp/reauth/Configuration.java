package technicianlp.reauth;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public class Configuration {

    private final ForgeConfigSpec spec;

    private final ForgeConfigSpec.ConfigValue<Integer> versionSpec;
    private final ForgeConfigSpec.ConfigValue<String> usernameSpec;
    private final ForgeConfigSpec.ConfigValue<String> passwordSpec;

    private ModConfig configuration;
    private int version;
    private String username;
    private String password;

    public Configuration() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        versionSpec = builder
                .comment("Version Number of the Configuration File")
                .defineInRange("version", 1, 1, 1);
        builder.push("credentials");
        usernameSpec = builder
                .comment("Your Username")
                .define("username", "");
        passwordSpec = builder
                .comment("Your Password in plaintext if chosen to save to disk")
                .define("password", "");
        builder.pop();
        spec = builder.build();
    }

    public ForgeConfigSpec getSpec() {
        return spec;
    }

    public void setConfig(ModConfig config) {
        username = usernameSpec.get();
        password = passwordSpec.get();
    }

    public void setCredentials(String username, String password) {
        usernameSpec.set(username);
        usernameSpec.save();
        this.username = username;
        passwordSpec.set(password);
        passwordSpec.save();
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
