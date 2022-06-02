package technicianlp.reauth;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Simplified version of the Forge VersionCheck implementation
 */
public final class VersionChecker implements Runnable {

    private static final String MC_VERSION = "1.7.10";
    private static final String JSON_URL = "https://github.com/TechnicianLP/ReAuth/raw/master/update.json";

    private Status status = Status.UNKNOWN;
    private String changes = null;

    @Override
    public void run() {
        try {
            ReAuth.log.info("Starting version check");
            this.status = Status.UNKNOWN;
            this.changes = null;

            InputStream inputstream = new URL(JSON_URL).openStream();
            String data = IOUtils.toString(inputstream, StandardCharsets.UTF_8);
            inputstream.close();

            VersionJson json = new Gson().fromJson(data, VersionJson.class);

            String latestVersion = json.versions.get(MC_VERSION + "-recommended");
            ModContainer container = Loader.instance().getIndexedModList().get("reauth");
            String current = container.getVersion();
            int currentVersionId = versionToInt(current);

            if (latestVersion != null) {
                int latestVersionId = versionToInt(latestVersion);
                if (currentVersionId < latestVersionId) {
                    this.status = Status.OUTDATED;
                } else {
                    this.status = Status.OK;
                }
            }

            this.changes = json.changelog.get(latestVersion);
            ReAuth.log.info("Version check completed with status: " + this.status);
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
        String[] split = version.split("\\.", 3);
        int ver = 0;
        for (String s : split) {
            ver = (ver << 8) | Integer.parseInt(s);
        }
        return ver;
    }

    private static class VersionJson {
        @SerializedName("promos")
        Map<String, String> versions = new HashMap<>();
        @SerializedName(MC_VERSION)
        Map<String, String> changelog = new HashMap<>();
    }

    public enum Status {
        FAILED, OK, OUTDATED, UNKNOWN
    }
}
