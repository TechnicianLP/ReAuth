package technicianlp.reauth.authentication.dto.yggdrasil;

import org.jetbrains.annotations.Nullable;

import com.google.gson.annotations.SerializedName;

import technicianlp.reauth.authentication.dto.ResponseObject;

/**
 * Response Payload for the /session/minecraft/join of the Mojang Session API
 * Payload is only returned in case of error
 *
 * @see <a href=https://wiki.vg/Authentication>https://wiki.vg/Authentication</a>
 */
public final class JoinServerResponse implements ResponseObject {

    @SerializedName("error")
    public final @Nullable String error;

    private JoinServerResponse() {
        this.error = null;
    }

    @Override
    public final boolean isValid() {
        return this.error == null;
    }

    @Override
    public final @Nullable String getError() {
        return this.error;
    }
}
