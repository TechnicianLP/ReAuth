package technicianlp.reauth.authentication.dto.xbox;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.Nullable;
import technicianlp.reauth.authentication.dto.ResponseObject;

import java.lang.reflect.Type;

/**
 * Response Payload for:
 * the /user/authenticate Endpoint of the Xbox Live user service<br>
 * the /xsts/authorize Endpoint of the Xbox Live xsts service<br>
 * <br>
 * Only relevant fields are deserialized
 *
 * @see <a href=https://wiki.vg/Microsoft_Authentication_Scheme>https://wiki.vg/Microsoft_Authentication_Scheme</a>
 */
public final class XboxAuthResponse implements ResponseObject {

    private static final String XSTS_ERR_MISSING_ACCOUNT = "8015dc09";
    private static final String XSTS_ERR_TOKEN_EXPIRED = "8015dc22";
    private static final String XSTS_ERR_TOKEN_INVALID = "8015dc26";

    // Normal Handling
    public final String validUntil;
    public final String token;
    public final String userHash;

    // Error Handling
    public final @Nullable String error;

    private XboxAuthResponse(String validUntil, String token, String userHash, String error) {
        this.validUntil = validUntil;
        this.token = token;
        this.userHash = userHash;
        this.error = error;
    }

    @Override
    public final boolean isValid() {
        return this.error == null && this.validUntil != null && this.token != null && this.userHash != null;
    }

    @Override
    public final @Nullable String getError() {
        return this.error;
    }

    @Override
    public final @Nullable String getErrorDescription() {
        if (XSTS_ERR_MISSING_ACCOUNT.equals(this.error)) {
            return "reauth.error.xbox";
        }
        return null;
    }

    public final String getToken() {
        return this.token;
    }

    public final boolean isExpiredTokenError() {
        return XSTS_ERR_TOKEN_EXPIRED.equals(this.error) || XSTS_ERR_TOKEN_INVALID.equals(this.error);
    }

    /**
     * Custom deserializer that searches the DisplayClaims object for the uhs field.
     */
    public static final class Deserializer implements JsonDeserializer<XboxAuthResponse> {

        @Override
        public final XboxAuthResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                JsonObject root = json.getAsJsonObject();

                String validUntil = this.getString(root, "NotAfter");
                String token = this.getString(root, "Token");
                String userHash = this.extractUserHash(root.getAsJsonObject("DisplayClaims"));

                String error = this.getString(root, "XErr");
                if(error != null) {
                    try {
                        error = Integer.toHexString(Integer.parseUnsignedInt(error));
                    } catch (NumberFormatException ignored) {
                    }
                }

                return new XboxAuthResponse(validUntil, token, userHash, error);
            } catch (IllegalStateException | ClassCastException e) {
                throw new JsonParseException("invalid format", e);
            }
        }

        private String getString(JsonObject root, String name) {
            JsonPrimitive primitive = root.getAsJsonPrimitive(name);
            if (primitive != null) {
                return primitive.getAsString();
            } else {
                return null;
            }
        }

        private String extractUserHash(JsonObject displayClaims) {
            if (displayClaims != null) {
                JsonArray xui = displayClaims.getAsJsonArray("xui");
                if (xui != null) {
                    for (JsonElement claim : xui) {
                        JsonPrimitive uhs = claim.getAsJsonObject().getAsJsonPrimitive("uhs");
                        if (uhs != null) {
                            return uhs.getAsString();
                        }
                    }
                }
            }
            return null;
        }
    }
}
