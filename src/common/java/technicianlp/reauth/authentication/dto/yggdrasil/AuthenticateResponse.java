package technicianlp.reauth.authentication.dto.yggdrasil;

import org.jetbrains.annotations.Nullable;

import com.google.gson.annotations.SerializedName;

import technicianlp.reauth.authentication.SessionData;
import technicianlp.reauth.authentication.dto.ResponseObject;

/**
 * Response Payload for the /authenticate of the Mojang Yggdrasil API
 * <br>
 * Only relevant fields are deserialized
 *
 * @see <a href=https://wiki.vg/Authentication>https://wiki.vg/Authentication</a>
 */
public final class AuthenticateResponse implements ResponseObject {

    @SerializedName("accessToken")
    public final String accessToken;
    @SerializedName("selectedProfile")
    public final Profile profile;

    @SerializedName("error")
    public final @Nullable String error;

    private AuthenticateResponse() {
        this.accessToken = null;
        this.profile = null;
        this.error = null;
    }

    @Override
    public final boolean isValid() {
        return this.error == null && this.accessToken != null && this.profile != null && this.profile.name != null && this.profile.uuid != null;
    }

    @Override
    public final @Nullable String getError() {
        return this.error;
    }

    public final SessionData getSession() {
        if (this.profile == null) {
            return null;
        }
        return new SessionData(this.profile.name, this.profile.uuid, this.accessToken, "mojang");
    }

    public static final class Profile {

        @SerializedName("name")
        public final String name;
        @SerializedName("id")
        public final String uuid;

        private Profile() {
            this.name = null;
            this.uuid = null;
        }
    }
}
