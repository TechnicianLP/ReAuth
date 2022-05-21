package technicianlp.reauth.authentication.dto.microsoft;

import com.google.common.collect.ImmutableMap;
import technicianlp.reauth.authentication.MsAuthAPI;
import technicianlp.reauth.authentication.dto.RequestObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Request Payload for the /token Endpoint of the Microsoft Identity Platform
 * Payload is used for refreshing the oauth tokens.
 *
 * @see <a href=https://wiki.vg/Microsoft_Authentication_Scheme>Microsoft Authentication Scheme on wiki.vg</a>
 * @see <a href=https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-auth-code-flow>Microsoft Auth Code Flow</a>
 * @see <a href=https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-device-code>Microsoft Device Code Flow</a>
 */
public final class MicrosoftAuthRefreshRequest implements RequestObject.Form<MicrosoftAuthResponse> {

    private final Map<String, String> fields;

    public MicrosoftAuthRefreshRequest(String refreshToken) {
        Map<String, String> map = new HashMap<>();

        map.put("client_id", MsAuthAPI.clientId);
        map.put("redirect_uri", MsAuthAPI.redirectUri);
        map.put("grant_type", "refresh_token");
        map.put("refresh_token", refreshToken);

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
