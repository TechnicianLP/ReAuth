package technicianlp.reauth.authentication.dto.microsoft;

import org.jetbrains.annotations.Nullable;

import com.google.gson.annotations.SerializedName;

import technicianlp.reauth.authentication.dto.ResponseObject;

/**
 * Response Payload for the /token Endpoint of the Microsoft Identity Platform
 * <br>
 * Only relevant fields are deserialized
 *
 * @see <a href=https://wiki.vg/Microsoft_Authentication_Scheme>Microsoft Authentication Scheme on wiki.vg</a>
 * @see <a href=https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-auth-code-flow>Microsoft Auth Code Flow</a>
 * @see <a href=https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-device-code>Microsoft Device Code Flow</a>
 */
public final class MicrosoftAuthResponse implements ResponseObject {

    // Normal Handling
    @SerializedName("expires_in")
    public final String expires_in;
    @SerializedName("access_token")
    public final String accessToken;
    @SerializedName("refresh_token")
    public final @Nullable String refreshToken;

    // Error Handling
    @SerializedName("error")
    public final @Nullable String error;

    private MicrosoftAuthResponse() {
        this.expires_in = null;
        this.accessToken = null;
        this.refreshToken = null;

        this.error = null;
    }

    @Override
    public final boolean isValid() {
        return this.error == null && this.expires_in != null && this.accessToken != null;
    }

    @Override
    public final @Nullable String getError() {
        return this.error;
    }

    public final String getAccessToken() {
        return this.accessToken;
    }

    public final String getRefreshToken() {
        return this.refreshToken;
    }
}
