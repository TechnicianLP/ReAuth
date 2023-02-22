package technicianlp.reauth.authentication;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import technicianlp.reauth.authentication.dto.yggdrasil.JoinServerRequest;
import technicianlp.reauth.authentication.dto.yggdrasil.JoinServerResponse;
import technicianlp.reauth.authentication.http.HttpUtil;
import technicianlp.reauth.authentication.http.Response;
import technicianlp.reauth.authentication.http.UnreachableServiceException;
import technicianlp.reauth.crypto.Crypto;

import java.math.BigInteger;

/**
 * cut down reimplementation version of {@link YggdrasilUserAuthentication}
 */
public final class YggdrasilAPI {

    private static final String urlJoin = "https://sessionserver.mojang.com/session/minecraft/join";

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
