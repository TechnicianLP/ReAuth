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
public final class XboxXstsAuthRequest implements RequestObject.JSON<XboxAuthResponse> {

    private final String token;

    public XboxXstsAuthRequest(String token) {
        this.token = token;
    }

    @Override
    public final Class<XboxAuthResponse> getResponseClass() {
        return XboxAuthResponse.class;
    }

    /**
     * Custom Serializer for {@link XboxXstsAuthRequest} for static data to improve readability
     */
    public static final class Serializer implements JsonSerializer<XboxXstsAuthRequest> {

        @Override
        public final JsonElement serialize(XboxXstsAuthRequest src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject properties = new JsonObject();
            properties.addProperty("SandboxId", "RETAIL");
            properties.add("UserTokens", context.serialize(new String[]{src.token}));

            JsonObject root = new JsonObject();
            root.add("Properties", properties);
            root.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
            root.addProperty("TokenType", "JWT");
            return root;
        }
    }
}
