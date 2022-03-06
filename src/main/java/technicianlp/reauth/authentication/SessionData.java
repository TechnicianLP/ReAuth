package technicianlp.reauth.authentication;

public final class SessionData {

    public final String username;
    public final String uuid;
    public final String accessToken;
    public final String type;

    public SessionData(String username, String uuid, String accessToken, String type) {
        this.username = username;
        this.uuid = uuid;
        this.accessToken = accessToken;
        this.type = type;
    }

    @Override
    public final String toString() {
        return "SessionData{" +
                "username='" + this.username + '\'' +
                ", uuid='" + this.uuid + '\'' +
                ", accessToken='" + this.accessToken + '\'' +
                ", type='" + this.type + '\'' +
                '}';
    }
}
