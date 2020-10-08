package technicianlp.reauth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import net.minecraft.SharedConstants;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class VersionChecker implements Runnable {

    private static final String MC_VERSION = SharedConstants.getGameVersion().getReleaseTarget();
    private static final String JSON_URL = "https://github.com/TechnicianLP/ReAuth/raw/master/update.json";
    private static final Type mapType = new TypeToken<Map<String, String>>() {}.getType();

    private Status status = Status.UNKNOWN;
    private String changes = null;

    private final Gson gson;

    public VersionChecker() {
        gson = new GsonBuilder()
                .registerTypeAdapter(VersionJson.class, new VersionJsonDeserializer())
                .create();
    }

    public void runVersionCheck() {
        status = Status.UNKNOWN;
        changes = null;
        new Thread(this, "ReAuth Version Check").start();
    }

    @Override
    public void run() {
        try {
            ReAuth.log.info("Starting version check");

            InputStream inputstream = new URL(JSON_URL).openStream();
            String data = IOUtils.toString(inputstream, StandardCharsets.UTF_8);
            inputstream.close();

            VersionJson json = gson.fromJson(data, VersionJson.class);

            String latest = json.versions.get(MC_VERSION + "-recommended");
            String current = ReAuth.container.getMetadata().getVersion().getFriendlyString();
            int currentId = versionToInt(current);

            if (latest != null) {
                int latestVer = versionToInt(latest);
                if (currentId < latestVer) {
                    status = Status.OUTDATED;
                } else {
                    status = Status.OK;
                }
            }

            int currentLatest = 0;
            for (Map.Entry<String, String> entry : json.changelog.entrySet()) {
                int versionId = versionToInt(entry.getKey());
                if (versionId > currentLatest) {
                    currentLatest = versionId;
                    changes = entry.getValue();
                }
            }
            ReAuth.log.info("Version check complete");
        } catch (Exception e) {
            ReAuth.log.warn("Failed to process update information", e);
            status = Status.FAILED;
        }
    }

    public Status getStatus() {
        return status;
    }

    public String getChanges() {
        return changes;
    }

    private static int versionToInt(String version) {
        String[] split = version.split("\\.", 3);
        int ver = 0;
        for (String s : split) {
            ver = (ver << 8) | Integer.parseInt(s);
        }
        return ver;
    }

    private static class VersionJson {
        Map<String, String> versions = new HashMap<>();
        Map<String, String> changelog = new HashMap<>();

        public VersionJson(Map<String, String> versions, Map<String, String> changelog) {
            if (versions != null)
                this.versions.putAll(versions);
            if (changelog != null)
                this.changelog.putAll(changelog);
        }
    }

    private static class VersionJsonDeserializer implements JsonDeserializer<VersionJson> {
        @Override
        public VersionJson deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonObject()) {
                JsonObject object = (JsonObject) json;
                Map<String, String> versions = context.deserialize(object.get("promos-fabric"), mapType);
                Map<String, String> changelog = context.deserialize(object.get(MC_VERSION + "-fabric"), mapType);
                return new VersionJson(versions, changelog);
            }
            return null;
        }
    }

    public enum Status {
        FAILED, OK, OUTDATED, UNKNOWN
    }
}
