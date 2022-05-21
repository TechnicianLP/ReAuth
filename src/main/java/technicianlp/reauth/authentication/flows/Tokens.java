package technicianlp.reauth.authentication.flows;

import technicianlp.reauth.authentication.dto.microsoft.MicrosoftAuthResponse;
import technicianlp.reauth.authentication.dto.xbox.XboxAuthResponse;

public final class Tokens {

    private final String xblToken;
    private final String refreshToken;

    public Tokens(MicrosoftAuthResponse microsoft, XboxAuthResponse xasu) {
        this.xblToken = xasu.getToken();
        this.refreshToken = microsoft.getRefreshToken();
    }

    public final String getXblToken() {
        return this.xblToken;
    }

    public final String getRefreshToken() {
        return this.refreshToken;
    }
}
