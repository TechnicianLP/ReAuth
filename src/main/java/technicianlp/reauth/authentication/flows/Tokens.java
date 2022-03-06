package technicianlp.reauth.authentication.flows;

import technicianlp.reauth.authentication.dto.microsoft.MicrosoftAuthResponse;
import technicianlp.reauth.authentication.dto.xbox.XboxAuthResponse;

public record Tokens(String xblToken, String refreshToken) {

    public Tokens(MicrosoftAuthResponse microsoft, XboxAuthResponse xasu) {
        this(xasu.getToken(), microsoft.getRefreshToken());
    }
}
