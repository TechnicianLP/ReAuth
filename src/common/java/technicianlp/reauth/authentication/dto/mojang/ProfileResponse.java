package technicianlp.reauth.authentication.dto.mojang;

import com.google.gson.annotations.SerializedName;
import technicianlp.reauth.authentication.dto.ResponseObject;

/**
 * Response Payload for the /minecraft/profile Endpoint of the MinecraftServices API.
 * <br>
 * Only relevant fields are deserialized
 *
 * @see <a href=https://wiki.vg/Microsoft_Authentication_Scheme>https://wiki.vg/Microsoft_Authentication_Scheme</a>
 */
public final class ProfileResponse implements ResponseObject {

    // Normal Handling
    @SerializedName("id")
    public final String uuid;
    @SerializedName("name")
    public final String name;

    // Error Handling
    @SerializedName("error")
    public final String error;

    private ProfileResponse() {
        this.uuid = null;
        this.name = null;
        this.error = null;
    }

    @Override
    public boolean isValid() {
        return this.error == null && this.uuid != null && this.name != null;
    }

    @Override
    public String getError() {
        return this.error;
    }
}
