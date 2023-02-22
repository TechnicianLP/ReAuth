package technicianlp.reauth.authentication;

import technicianlp.reauth.authentication.dto.microsoft.MicrosoftAuthRefreshRequest;
import technicianlp.reauth.authentication.dto.microsoft.MicrosoftAuthResponse;
import technicianlp.reauth.authentication.dto.microsoft.code.MicrosoftAuthCodeRequest;
import technicianlp.reauth.authentication.dto.microsoft.device.MicrosoftAuthDeviceRequest;
import technicianlp.reauth.authentication.dto.microsoft.device.MicrosoftAuthDeviceResponse;
import technicianlp.reauth.authentication.dto.microsoft.device.MicrosoftAuthDeviceTokenRequest;
import technicianlp.reauth.authentication.dto.mojang.MojangAuthRequest;
import technicianlp.reauth.authentication.dto.mojang.MojangAuthResponse;
import technicianlp.reauth.authentication.dto.mojang.ProfileResponse;
import technicianlp.reauth.authentication.dto.xbox.XboxAuthResponse;
import technicianlp.reauth.authentication.dto.xbox.XboxLiveAuthRequest;
import technicianlp.reauth.authentication.dto.xbox.XboxXstsAuthRequest;
import technicianlp.reauth.authentication.http.HttpUtil;
import technicianlp.reauth.authentication.http.InvalidResponseException;
import technicianlp.reauth.authentication.http.Response;
import technicianlp.reauth.authentication.http.UnreachableServiceException;


public final class MsAuthAPI {

    public static final String clientId = "fa861065-c46c-4ac9-a4da-59a7d40b8a72";

    public static final int port = 52371;
    public static final String redirectUri = "http://127.0.0.1:" + port;
    private static final String redirectUriEncoded = "http%3A%2F%2F127%2E0%2E0%2E1%3A" + port;

    private static final String scopeBasic = "XboxLive.signin";
    private static final String scopePersist = "XboxLive.signin XboxLive.offline_access";
    private static final String scopePersistUrl = "XboxLive.signin+XboxLive.offline_access";

    private static final String urlMicrosoftAuthorize = "https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize";
    private static final String urlMicrosoftToken = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";
    private static final String urlMicrosoftDevice = "https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode";
    private static final String urlXboxLive = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String urlXsts = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private static final String urlMojangLogin = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String urlMojangProfile = "https://api.minecraftservices.com/minecraft/profile";

    public static String getLoginUrl(boolean persist, String pkceChallenge) {
        return urlMicrosoftAuthorize +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUriEncoded +
                "&scope=" + (persist ? scopePersistUrl : scopeBasic) +
                "&response_type=code" +
                "&response_mode=query" +
                "&prompt=select_account" +
                "&code_challenge=" + pkceChallenge +
                "&code_challenge_method=S256";
    }

    public static MicrosoftAuthResponse redeemAuthorizationCode(String code, String pkceVerifier) throws UnreachableServiceException, InvalidResponseException {
        MicrosoftAuthCodeRequest request = new MicrosoftAuthCodeRequest(code, pkceVerifier);
        return HttpUtil.performFormRequest(urlMicrosoftToken, request);
    }

    public static MicrosoftAuthDeviceResponse requestDeviceCode(boolean persist) throws UnreachableServiceException, InvalidResponseException {
        MicrosoftAuthDeviceRequest request = new MicrosoftAuthDeviceRequest(persist ? scopePersist : scopeBasic);
        return HttpUtil.performFormRequest(urlMicrosoftDevice, request);
    }

    public static Response<MicrosoftAuthResponse> redeemDeviceCode(String deviceCode) throws UnreachableServiceException {
        MicrosoftAuthDeviceTokenRequest request = new MicrosoftAuthDeviceTokenRequest(deviceCode);
        return HttpUtil.performWrappedFormRequest(urlMicrosoftToken, request);
    }

    public static MicrosoftAuthResponse redeemRefreshToken(String refreshToken) throws UnreachableServiceException, InvalidResponseException {
        MicrosoftAuthRefreshRequest request = new MicrosoftAuthRefreshRequest(refreshToken);
        return HttpUtil.performFormRequest(urlMicrosoftToken, request);
    }

    public static XboxAuthResponse authenticateXASU(String token) throws UnreachableServiceException, InvalidResponseException {
        XboxLiveAuthRequest request = new XboxLiveAuthRequest(token);
        return HttpUtil.performJsonRequest(urlXboxLive, request);
    }

    public static Response<XboxAuthResponse> authenticateXSTS(String xblToken) throws UnreachableServiceException {
        XboxXstsAuthRequest request = new XboxXstsAuthRequest(xblToken);
        return HttpUtil.performWrappedJsonRequest(urlXsts, request);
    }

    public static MojangAuthResponse authenticateMojang(String xstsToken, String uhs) throws UnreachableServiceException, InvalidResponseException {
        MojangAuthRequest request = new MojangAuthRequest(xstsToken, uhs);
        return HttpUtil.performJsonRequest(urlMojangLogin, request);
    }

    public static ProfileResponse fetchProfile(String accessToken) throws UnreachableServiceException, InvalidResponseException {
        return HttpUtil.performGetRequest(urlMojangProfile, accessToken, ProfileResponse.class);
    }
}
