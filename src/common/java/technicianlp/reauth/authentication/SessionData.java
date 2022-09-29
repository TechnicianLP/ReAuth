package technicianlp.reauth.authentication;

public record SessionData(String username, String uuid, String accessToken, String type) {
}
