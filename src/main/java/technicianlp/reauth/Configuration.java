package technicianlp.reauth;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = ConfigWrapper.CONFIG_NAME)
public final class Configuration implements ConfigData {
    @ConfigEntry.Gui.Excluded
    @ConfigEntry.BoundedDiscrete(min = 1, max = 2)
    @Comment("Version Number of the Configuration File")
    public int version = 2;

//        @Comment("Credentials for login, encrypted with AES-CBC-PKCS5Padding and PBKDF2WithHmacSHA512 " +
//            "based on the Path of this file and the contained Salt.\n" +
//            "Manually editing one of the Encrypted Values or the Salt, is inadvisable - " +
//            "Reset them to \"\" if required\n" +
//            "Keep in mind that while the credentials are no longer humanly readable, " +
//            "they could still be decrypted by malicious third parties")
    @ConfigEntry.Gui.TransitiveObject
    Credentials credentials = new Credentials();

    static final class Credentials {
        @Comment("One of the values required to decrypt the credentials\n" +
                "See https://en.wikipedia.org/wiki/Salt_(cryptography) for details")
        @ConfigEntry.Gui.Tooltip(count = 2)
        public String salt = "";

        @Comment("The Name of the last used Profile")
        @ConfigEntry.Gui.Tooltip
        public String profile = "";

        @Comment("Your Username (encrypted)")
        @ConfigEntry.Gui.Tooltip
        public String username = "";

        @Comment("Your Password (encrypted)")
        @ConfigEntry.Gui.Tooltip
        public String password = "";
    }

    @Override
    public void validatePostLoad() throws ValidationException {
        if (1 <= version && version <= 2)
            ReAuth.config.onLoad(this);
        else
            throw new ValidationException("Unknown Version");
    }
}
