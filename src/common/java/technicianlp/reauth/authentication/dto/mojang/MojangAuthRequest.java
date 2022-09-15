package technicianlp.reauth.authentication.dto.mojang;

import com.google.gson.annotations.SerializedName;
import technicianlp.reauth.authentication.dto.RequestObject;

/**
 * Request Payload for the /authentication/login_with_xbox Endpoint of the MinecraftServices API
 *
 * @see <a href=https://wiki.vg/Microsoft_Authentication_Scheme>https://wiki.vg/Microsoft_Authentication_Scheme</a>
 */
public final class MojangAuthRequest implements RequestObject.JSON<MojangAuthResponse> {

    @SerializedName("identityToken")
    private final String token;

    public MojangAuthRequest(String xToken, String userHash) {
        this.token = "XBL3.0 x=" + userHash + ";" + xToken;
    }

    @Override
    public final Class<MojangAuthResponse> getResponseClass() {
        return MojangAuthResponse.class;
    }
}
