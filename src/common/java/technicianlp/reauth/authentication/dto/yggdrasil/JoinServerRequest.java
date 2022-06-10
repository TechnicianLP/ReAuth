package technicianlp.reauth.authentication.dto.yggdrasil;

import com.google.gson.annotations.SerializedName;
import technicianlp.reauth.authentication.dto.RequestObject;

/**
 * Request Payload for the /session/minecraft/join of the Mojang Session API
 *
 * @see <a href=https://wiki.vg/Authentication>https://wiki.vg/Authentication</a>
 */
public final class JoinServerRequest implements RequestObject.JSON<JoinServerResponse> {

    @SerializedName("accessToken")
    private final String accessToken;
    @SerializedName("selectedProfile")
    private final String uuid;
    @SerializedName("serverId")
    private final String hash;

    public JoinServerRequest(String accessToken, String uuid, String hash) {
        this.accessToken = accessToken;
        this.uuid = uuid;
        this.hash = hash;
    }

    @Override
    public final Class<JoinServerResponse> getResponseClass() {
        return JoinServerResponse.class;
    }
}
