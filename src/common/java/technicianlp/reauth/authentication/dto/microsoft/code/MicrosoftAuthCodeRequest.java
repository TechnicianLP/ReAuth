package technicianlp.reauth.authentication.dto.microsoft.code;

import com.google.common.collect.ImmutableMap;
import technicianlp.reauth.authentication.MsAuthAPI;
import technicianlp.reauth.authentication.dto.RequestObject;
import technicianlp.reauth.authentication.dto.microsoft.MicrosoftAuthResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Request Payload for the /token Endpoint of the Microsoft Identity Platform.
 * Payload is used for redeeming the code received in the auth code grant flow.
 *
 * @see <a href=https://wiki.vg/Microsoft_Authentication_Scheme>Microsoft Authentication Scheme on wiki.vg</a>
 * @see <a href=https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-auth-code-flow>Microsoft Auth Code Flow</a>
 */
public final class MicrosoftAuthCodeRequest implements RequestObject.Form<MicrosoftAuthResponse> {

    private final Map<String, String> fields;

    public MicrosoftAuthCodeRequest(String authCode, String pkceVerifier) {
        Map<String, String> map = new HashMap<>();

        map.put("client_id", MsAuthAPI.clientId);
        map.put("redirect_uri", MsAuthAPI.redirectUri);
        map.put("grant_type", "authorization_code");
        map.put("code", authCode);
        map.put("code_verifier", pkceVerifier);

        this.fields = ImmutableMap.copyOf(map);
    }

    @Override
    public final Class<MicrosoftAuthResponse> getResponseClass() {
        return MicrosoftAuthResponse.class;
    }

    @Override
    public final Map<String, String> getFields() {
        return this.fields;
    }
}
