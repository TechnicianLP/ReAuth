package technicianlp.reauth.configuration;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class Profile {

    private final Map<String, String> data;
    private final boolean loaded;

    Profile(Map<String, String> data, boolean loaded) {
        this.data = data;
        this.loaded = loaded;
    }

    public final String getValue(String key) {
        return this.data.get(key);
    }

    public final String getValue(String key, String defaultValue) {
        return this.data.getOrDefault(key, defaultValue);
    }

    public final CompletableFuture<String> get(String key) {
        return CompletableFuture.completedFuture(this.getValue(key));
    }

    final Map<String, String> getConfig() {
        return this.data;
    }

    final boolean isLoaded() {
        return this.loaded;
    }

    // profile type
    public static final String PROFILE_TYPE = "type";
    public static final String PROFILE_TYPE_NONE = "none";
    public static final String PROFILE_TYPE_MOJANG = "mojang";
    public static final String PROFILE_TYPE_MICROSOFT = "microsoft";

    // common field
    public static final String NAME = "name";
    public static final String UUID = "uuid";

    // mojang profiles
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    // microsoft profiles
    public static final String REFRESH_TOKEN = "refresh-token";
    public static final String XBL_TOKEN = "xbl-token";

    // encryption
    public static final String KEY = "key";
    public static final String KEY_AUTO = "auto";
    public static final String KEY_NONE = "none";
    public static final String SALT = "salt";
}
