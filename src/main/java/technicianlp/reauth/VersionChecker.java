package technicianlp.reauth;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.SharedConstants;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class VersionChecker implements Runnable {

    private static final String MC_VERSION = SharedConstants.getGameVersion().getReleaseTarget();
    private static final String JSON_URL = "https://github.com/NgoKimPhu/ReAuth/raw/master/update.json";
    private static final Type mapType = new TypeToken<Map<String, String>>() {
    }.getType();
    private static final Pattern PATTERN_VERSION_DELIMS = Pattern.compile("[.-]");

    private Status status = Status.UNKNOWN;
    private String changes;

    private final Gson gson;

    public VersionChecker() {
        this.gson = new GsonBuilder()
            .registerTypeAdapter(VersionJson.class, new VersionJsonDeserializer())
            .create();
    }

    public void runVersionCheck() {
        this.status = Status.UNKNOWN;
        this.changes = null;
        new Thread(this, "ReAuth Version Check").start();
    }

    @Override
    public void run() {
        try {
            ReAuth.log.info("Starting version check");

            InputStream inputstream = new URL(JSON_URL).openStream();
            String data = IOUtils.toString(inputstream, StandardCharsets.UTF_8);
            inputstream.close();

            VersionJson json = this.gson.fromJson(data, VersionJson.class);

            String latest = json.versions.get(MC_VERSION + "-recommended");
            String current = ReAuth.container.getMetadata().getVersion().getFriendlyString();
            int currentId = versionToInt(current);

            if (latest != null) {
                int latestVer = versionToInt(latest);
                if (currentId < latestVer) {
                    this.status = Status.OUTDATED;
                } else {
                    this.status = Status.OK;
                }
            }

            int currentLatest = 0;
            for (Map.Entry<String, String> entry : json.changelog.entrySet()) {
                int versionId = versionToInt(entry.getKey());
                if (versionId > currentLatest) {
                    currentLatest = versionId;
                    this.changes = entry.getValue();
                }
            }
            ReAuth.log.info("Version check complete");
        } catch (Exception e) {
            ReAuth.log.warn("Failed to process update information", e);
            this.status = Status.FAILED;
        }
    }

    public Status getStatus() {
        return this.status;
    }

    public String getChanges() {
        return this.changes;
    }

    private static int versionToInt(String version) {
        return Arrays.stream(PATTERN_VERSION_DELIMS.split(version)).limit(3)
            .mapToInt(Integer::parseInt).reduce(0, (ver, i) -> (ver << 8) | i);
    }

    private static final class VersionJson {
        final Map<String, String> versions = new HashMap<>();
        final Map<String, String> changelog = new HashMap<>();

        private VersionJson(Map<String, String> versions, Map<String, String> changelog) {
            if (versions != null) {
                this.versions.putAll(versions);
            }
            if (changelog != null) {
                this.changelog.putAll(changelog);
            }
        }
    }

    private static class VersionJsonDeserializer implements JsonDeserializer<VersionJson> {
        @Override
        public VersionJson deserialize(JsonElement jsonElement, Type type,
                                       @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
                                       JsonDeserializationContext context) throws
            JsonParseException {
            if (jsonElement.isJsonObject()) {
                JsonObject object = (JsonObject) jsonElement;
                Map<String, String> versions =
                    context.deserialize(object.get("promos-fabric"), mapType);
                Map<String, String> changelog =
                    context.deserialize(object.get(MC_VERSION + "-fabric"), mapType);
                return new VersionJson(versions, changelog);
            }
            return null;
        }
    }

    public enum Status {
        FAILED, OK, OUTDATED, UNKNOWN
    }
}
