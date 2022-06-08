package technicianlp.reauth.authentication.dto.microsoft.device;

import com.google.common.collect.ImmutableMap;
import technicianlp.reauth.authentication.MsAuthAPI;
import technicianlp.reauth.authentication.dto.RequestObject;
import technicianlp.reauth.authentication.dto.microsoft.MicrosoftAuthResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Request Payload for the /token Endpoint of the Microsoft Identity Platform.
 * Payload is used for polling the endpoint as described in the device code flow.
 *
 * @see <a href=https://wiki.vg/Microsoft_Authentication_Scheme>Microsoft Authentication Scheme on wiki.vg</a>
 * @see <a href=https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-device-code>Microsoft Device Code Flow</a>
 */
public final class MicrosoftAuthDeviceTokenRequest implements RequestObject.Form<MicrosoftAuthResponse> {

    private final Map<String, String> fields;

    public MicrosoftAuthDeviceTokenRequest(String deviceCode) {
        Map<String, String> map = new HashMap<>();

        map.put("client_id", MsAuthAPI.clientId);
        map.put("device_code", deviceCode);
        map.put("grant_type", "urn:ietf:params:oauth:grant-type:device_code");

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
