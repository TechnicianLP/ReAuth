package technicianlp.reauth.configuration;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

final class ProfileImpl implements Profile {

    private final Map<String, String> data;
    private final boolean loaded;

    ProfileImpl(Map<String, String> data, boolean loaded) {
        this.data = data;
        this.loaded = loaded;
    }

    @Override
    public final String getValue(String key) {
        return this.data.get(key);
    }

    @Override
    public final String getValue(String key, String defaultValue) {
        return this.data.getOrDefault(key, defaultValue);
    }

    @Override
    public final CompletableFuture<String> get(String key) {
        return CompletableFuture.completedFuture(this.getValue(key));
    }

    final Map<String, String> getConfig() {
        return this.data;
    }

    final boolean isLoaded() {
        return this.loaded;
    }
}
