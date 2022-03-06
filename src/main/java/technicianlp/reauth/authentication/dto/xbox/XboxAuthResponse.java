package technicianlp.reauth.authentication.dto.xbox;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
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

    // Normal Handling
    @SerializedName("NotAfter")
    public final String validUntil;
    @SerializedName("Token")
    public final String token;
    @SerializedName("DisplayClaims")
    @JsonAdapter(UserhashDeserializer.class)
    public final String userhash;

    // Error Handling
    @SerializedName("XErr")
    public final @Nullable String error;

    private XboxAuthResponse() {
        this.validUntil = null;
        this.token = null;
        this.userhash = null;
        this.error = null;
    }

    @Override
    public boolean isValid() {
        return this.error == null && this.validUntil != null && this.token != null && this.userhash != null;
    }

    @Override
    public @Nullable String getError() {
        return this.error;
    }

    public String getToken() {
        return this.token;
    }

    /**
     * Custom deserializer that searches the DisplayClaims object for the uhs field.
     */
    public static final class UserhashDeserializer implements JsonDeserializer<String> {

        @Override
        public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                JsonArray xui = json.getAsJsonObject().getAsJsonArray("xui");
                for (JsonElement claim : xui) {
                    JsonPrimitive uhs = claim.getAsJsonObject().getAsJsonPrimitive("uhs");
                    if (uhs != null) {
                        return uhs.getAsString();
                    }
                }
                return null;
            } catch (IllegalStateException | ClassCastException e) {
                throw new JsonParseException("invalid DisplayClaims format", e);
            }
        }
    }
}
