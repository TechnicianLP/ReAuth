package technicianlp.reauth.authentication.dto.mojang;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;
import technicianlp.reauth.authentication.dto.ResponseObject;

/**
 * Response Payload for the /authentication/login_with_xbox Endpoint of the MinecraftServices API
 * <br>
 * Only relevant fields are deserialized
 *
 * @see <a href=https://wiki.vg/Microsoft_Authentication_Scheme>https://wiki.vg/Microsoft_Authentication_Scheme</a>
 */
public final class MojangAuthResponse implements ResponseObject {

    // Normal Handling
    @SerializedName("access_token")
    public final String token;
    @SerializedName("expires_in")
    public final String expiry;

    // Error Handling
    @SerializedName("error")
    public final @Nullable String error;

    private MojangAuthResponse() {
        this.token = null;
        this.expiry = null;
        this.error = null;
    }

    @Override
    public final boolean isValid() {
        return this.error == null && this.token != null && this.expiry != null;
    }

    @Override
    public final @Nullable String getError() {
        return this.error;
    }

    public final String getToken() {
        return this.token;
    }
}
