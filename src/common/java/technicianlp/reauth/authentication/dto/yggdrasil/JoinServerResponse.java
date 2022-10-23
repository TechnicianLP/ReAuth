package technicianlp.reauth.authentication.dto.yggdrasil;

import com.google.gson.annotations.SerializedName;
import technicianlp.reauth.authentication.dto.ResponseObject;

/**
 * Response Payload for the /session/minecraft/join of the Mojang Session API Payload is only returned in case of error
 *
 * @see <a href=https://wiki.vg/Authentication>https://wiki.vg/Authentication</a>
 */
public final class JoinServerResponse implements ResponseObject {

    @SerializedName("error")
    public final String error;

    private JoinServerResponse() {
        this.error = null;
    }

    @Override
    public boolean isValid() {
        return this.error == null;
    }

    @Override
    public String getError() {
        return this.error;
    }
}
