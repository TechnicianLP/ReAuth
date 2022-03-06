package technicianlp.reauth.authentication.dto.microsoft.device;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;
import technicianlp.reauth.authentication.dto.ResponseObject;

/**
 * Response Payload for the /devicecode Endpoint of the Microsoft Identity Platform
 * <br>
 * Only relevant fields are deserialized
 *
 * @see <a href=https://wiki.vg/Microsoft_Authentication_Scheme>Microsoft Authentication Scheme on wiki.vg</a>
 * @see <a href=https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth2-device-code>Microsoft Device Code Flow</a>
 */
public final class MicrosoftAuthDeviceResponse implements ResponseObject {

    // Normal Handling
    @SerializedName("device_code")
    public final String deviceCode;
    @SerializedName("user_code")
    public final String userCode;
    @SerializedName("verification_uri")
    public final String verificationUri;

    @SerializedName("interval")
    public final int interval;

    // Error Handling
    @SerializedName("error")
    public final @Nullable String error;

    private MicrosoftAuthDeviceResponse() {
        this.deviceCode = null;
        this.userCode = null;
        this.verificationUri = null;

        this.interval = 5;

        this.error = null;
    }

    @Override
    public final boolean isValid() {
        return this.error == null && this.deviceCode != null && this.userCode != null && this.verificationUri != null;
    }

    @Override
    public final @Nullable String getError() {
        return this.error;
    }

    public final String getUserCode() {
        return this.userCode;
    }

    public final String getVerificationUri() {
        return this.verificationUri;
    }
}
