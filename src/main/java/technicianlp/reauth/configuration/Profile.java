package technicianlp.reauth.configuration;

import com.electronwill.nightconfig.core.CommentedConfig;

import java.util.concurrent.CompletableFuture;

public final class Profile {

    private final CommentedConfig config;

    Profile(CommentedConfig config) {
        this.config = config;
    }

    public String getValue(String key) {
        return this.config.get(key);
    }

    public String getValue(String key, String defaultValue) {
        return this.config.getOrElse(key, defaultValue);
    }

    public CompletableFuture<String> get(String key) {
        return CompletableFuture.completedFuture(this.getValue(key));
    }

    CommentedConfig getConfig() {
        return this.config;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof Profile && this.config.equals(((Profile) obj).config);
    }

    @Override
    public int hashCode() {
        return this.config == null ? 0 : this.config.hashCode();
    }
}
