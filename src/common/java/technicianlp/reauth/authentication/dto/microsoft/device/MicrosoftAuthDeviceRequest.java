package technicianlp.reauth.authentication.dto.microsoft.device;

import com.google.common.collect.ImmutableMap;
import technicianlp.reauth.authentication.MsAuthAPI;
import technicianlp.reauth.authentication.dto.RequestObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Request Payload for the /devicecode Endpoint of the Microsoft Identity Platform.
 * Payload is used for requesting a devicecode for the device code flow.
 *
 * @see <a href=https://wiki.vg/Microsoft_Authentication_Scheme>Microsoft Authentication Scheme on wiki.vg</a>
 * @see <a href=https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-device-code>Microsoft Device Code Flow</a>
 */
public final class MicrosoftAuthDeviceRequest implements RequestObject.Form<MicrosoftAuthDeviceResponse> {

    private final Map<String, String> fields;

    public MicrosoftAuthDeviceRequest(String scope) {
        Map<String, String> map = new HashMap<>();

        map.put("client_id", MsAuthAPI.clientId);
        map.put("scope", scope);

        this.fields = ImmutableMap.copyOf(map);
    }

    @Override
    public final Class<MicrosoftAuthDeviceResponse> getResponseClass() {
        return MicrosoftAuthDeviceResponse.class;
    }

    @Override
    public final Map<String, String> getFields() {
        return this.fields;
    }
}
