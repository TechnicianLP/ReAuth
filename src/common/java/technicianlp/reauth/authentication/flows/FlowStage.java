package technicianlp.reauth.authentication.flows;

public enum FlowStage {

    INITIAL("reauth.msauth.step.initial", "Starting Flow"),
    FINISHED("reauth.msauth.step.finished", "Login successful"),
    PROFILE("reauth.msauth.step.profile", "Saving Profile"),
    FAILED("reauth.msauth.step.failed", "Login failed"),
    CRYPTO_INIT("reauth.msauth.step.crypto", "Initializing Encryption"),

    YGG_AUTH("reauth.auth.step.yggdrasil", "Authenticating with Mojang Yggdrasil"),

    MS_AWAIT_AUTH_CODE("reauth.msauth.step.microsoft.code.await", "Waiting for Authentication"),
    MS_REDEEM_AUTH_CODE("reauth.msauth.step.microsoft.code.redeem", "Authenticating with Microsoft"),

    MS_REQUEST_DEVICE_CODE("reauth.msauth.step.microsoft.device.request", "Requesting Device Code from Microsoft"),
    MS_POLL_DEVICE_CODE("reauth.msauth.step.microsoft.device.poll", "Starting to poll Microsoft for token"),

    MS_REDEEM_REFRESH_TOKEN("reauth.msauth.step.microsoft.refresh", "Refreshing Authentication with Microsoft"),

    MS_AUTH_XASU("reauth.msauth.step.xbox", "Authenticating with Xbox Live"),
    MS_AUTH_XSTS("reauth.msauth.step.xsts", "Authenticating with XSTS"),
    MS_AUTH_MOJANG("reauth.msauth.step.mojang", "Authenticating with Mojang"),
    MS_FETCH_PROFILE("reauth.msauth.step.fetch", "Retrieving Profile");

    private final String rawName;
    private final String logLine;

    FlowStage(String rawName, String logLine) {
        this.rawName = rawName;
        this.logLine = logLine;
    }

    public final String getRawName() {
        return this.rawName;
    }

    public final String getLogLine() {
        return this.logLine;
    }
}
