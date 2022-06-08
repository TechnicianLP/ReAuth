package technicianlp.reauth.authentication.flows;

public interface AuthorizationCodeFlow extends Flow {

    String getLoginUrl();
}
