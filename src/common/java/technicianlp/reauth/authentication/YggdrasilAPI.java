package technicianlp.reauth.authentication;

import java.math.BigInteger;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

import technicianlp.reauth.authentication.dto.yggdrasil.AuthenticateRequest;
import technicianlp.reauth.authentication.dto.yggdrasil.AuthenticateResponse;
import technicianlp.reauth.authentication.dto.yggdrasil.JoinServerRequest;
import technicianlp.reauth.authentication.dto.yggdrasil.JoinServerResponse;
import technicianlp.reauth.authentication.http.HttpUtil;
import technicianlp.reauth.authentication.http.InvalidResponseException;
import technicianlp.reauth.authentication.http.Response;
import technicianlp.reauth.authentication.http.UnreachableServiceException;
import technicianlp.reauth.crypto.Crypto;

/**
 * cut down reimplementation version of {@link YggdrasilUserAuthentication}
 */
public final class YggdrasilAPI {

    private static final String urlAuthenticate = "https://authserver.mojang.com/authenticate";
    private static final String urlJoin = "https://sessionserver.mojang.com/session/minecraft/join";

    /**
     * reimplementation of {@link YggdrasilUserAuthentication#logInWithPassword()}
     */
    public static SessionData login(String username, String password) throws UnreachableServiceException, InvalidResponseException {
        AuthenticateRequest request = new AuthenticateRequest(username, password, UUID.randomUUID().toString());
        AuthenticateResponse response = HttpUtil.performJsonRequest(urlAuthenticate, request);
        return response != null ? response.getSession() : null;
    }

    /**
     * checks validity of accessToken by invoking the joinServer endpoint
     * <p>
     * reimplementation of {@link YggdrasilMinecraftSessionService#joinServer(GameProfile, String, String)}
     * Server hash is generated like during standard login sequence
     */
    public static boolean validate(String accessToken, String uuid) throws UnreachableServiceException {
        String hash = new BigInteger(Crypto.randomBytes(20)).toString(16);
        JoinServerRequest request = new JoinServerRequest(accessToken, uuid, hash);
        Response<JoinServerResponse> response = HttpUtil.performWrappedJsonRequest(urlJoin, request);
        return response.isValid();
    }
}
