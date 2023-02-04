package technicianlp.reauth.configuration;

import java.util.concurrent.CompletableFuture;

import com.electronwill.nightconfig.core.CommentedConfig;

public final class Profile {

    private final CommentedConfig config;

    Profile(CommentedConfig config) {
        this.config = config;
    }

    public final String getValue(String key) {
        return this.config.get(key);
    }

    public final String getValue(String key, String defaultValue) {
        return this.config.getOrElse(key, defaultValue);
    }

    public final CompletableFuture<String> get(String key) {
        return CompletableFuture.completedFuture(this.getValue(key));
    }

    final CommentedConfig getConfig() {
        return this.config;
    }
}
