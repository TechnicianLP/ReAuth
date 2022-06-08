package technicianlp.reauth.authentication.dto.xbox;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import technicianlp.reauth.authentication.dto.RequestObject;

import java.lang.reflect.Type;

/**
 * Request Payload for the /user/authenticate Endpoint of the Xbox Live user services<br>
 *
 * @see <a href=https://wiki.vg/Microsoft_Authentication_Scheme>https://wiki.vg/Microsoft_Authentication_Scheme</a>
 */
public final class XboxLiveAuthRequest implements RequestObject.JSON<XboxAuthResponse> {

    private final String token;

    public XboxLiveAuthRequest(String token) {
        this.token = token;
    }

    @Override
    public final Class<XboxAuthResponse> getResponseClass() {
        return XboxAuthResponse.class;
    }

    /**
     * Custom Serializer for {@link XboxLiveAuthRequest} for static data to improve readability
     */
    public static final class Serializer implements JsonSerializer<XboxLiveAuthRequest> {

        @Override
        public final JsonElement serialize(XboxLiveAuthRequest src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject properties = new JsonObject();
            properties.addProperty("AuthMethod", "RPS");
            properties.addProperty("SiteName", "user.auth.xboxlive.com");
            properties.addProperty("RpsTicket", "d=" + src.token);

            JsonObject root = new JsonObject();
            root.add("Properties", properties);
            //noinspection HttpUrlsUsage
            root.addProperty("RelyingParty", "http://auth.xboxlive.com");
            root.addProperty("TokenType", "JWT");
            return root;
        }
    }
}
