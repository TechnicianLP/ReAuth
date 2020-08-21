package technicianlp.reauth;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class VersionChecker implements Runnable {

    private static final String MC_VERSION = "1.16.2";
    private static final String JSON_URL = "https://github.com/TechnicianLP/ReAuth/raw/master/update.json";

    private Status status = Status.UNKNOWN;
    private String changes = null;

    public void runVersionCheck() {
        status = Status.UNKNOWN;
        changes = null;
        new Thread(new VersionChecker(), "ReAuth Version Check").start();
    }

    @Override
    public void run() {
        try {
            ReAuth.log.info("Starting version check");

            InputStream inputstream = new URL(JSON_URL).openStream();
            String data = IOUtils.toString(inputstream, StandardCharsets.UTF_8);
            inputstream.close();

            VersionJson json = new Gson().fromJson(data, VersionJson.class);

            String latest = json.versions.get(MC_VERSION + "-latest");
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
        @SerializedName("promos-fabric")
        Map<String, String> versions = new HashMap<>();
        @SerializedName(MC_VERSION + "-fabric")
        Map<String, String> changelog = new HashMap<>();
    }

    public enum Status {
        FAILED, OK, OUTDATED, UNKNOWN
    }
}
