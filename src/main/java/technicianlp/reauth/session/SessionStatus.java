package technicianlp.reauth.session;

public enum SessionStatus {
    VALID("valid"),
    INVALID("invalid"),
    UNKNOWN("unknown"),
    REFRESHING("refreshing"),
    ERROR("error");

    private final String translationKey;

    SessionStatus(String translationKey) {
        this.translationKey = "reauth.status." + translationKey;
    }

    public final String getTranslationKey() {
        return this.translationKey;
    }
}
