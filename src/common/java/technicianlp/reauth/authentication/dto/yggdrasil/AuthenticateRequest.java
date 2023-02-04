package technicianlp.reauth.authentication.dto.yggdrasil;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import technicianlp.reauth.authentication.dto.RequestObject;

/**
 * Request Payload for the /authenticate Endpoint of the Mojang Yggdrasil API
 *
 * @see <a href=https://wiki.vg/Authentication>https://wiki.vg/Authentication</a>
 */
public final class AuthenticateRequest implements RequestObject.JSON<AuthenticateResponse> {

    private final String username;
    private final String password;
    private final String clientToken;

    public AuthenticateRequest(String username, String password, String clientToken) {
        this.username = username;
        this.password = password;
        this.clientToken = clientToken;
    }

    @Override
    public final Class<AuthenticateResponse> getResponseClass() {
        return AuthenticateResponse.class;
    }

    /**
     * Custom Serializer for {@link AuthenticateRequest} for static data to improve readability
     */
    public static final class Serializer implements JsonSerializer<AuthenticateRequest> {

        @Override
        public final JsonElement serialize(AuthenticateRequest src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject agent = new JsonObject();
            agent.addProperty("name", "Minecraft");
            agent.addProperty("version", 1);

            JsonObject root = new JsonObject();
            root.add("agent", agent);
            root.addProperty("username", src.username);
            root.addProperty("password", src.password);
            root.addProperty("clientToken", src.clientToken);
            return root;
        }
    }
}
