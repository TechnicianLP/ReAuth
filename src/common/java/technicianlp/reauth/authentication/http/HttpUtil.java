package technicianlp.reauth.authentication.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import technicianlp.reauth.authentication.dto.RequestObject;
import technicianlp.reauth.authentication.dto.ResponseObject;
import technicianlp.reauth.authentication.dto.xbox.XboxAuthResponse;
import technicianlp.reauth.authentication.dto.xbox.XboxLiveAuthRequest;
import technicianlp.reauth.authentication.dto.xbox.XboxXstsAuthRequest;
import technicianlp.reauth.authentication.dto.yggdrasil.AuthenticateRequest;
import technicianlp.reauth.mojangfix.CertWorkaround;

public final class HttpUtil {

    private static final Gson GSON;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(AuthenticateRequest.class, new AuthenticateRequest.Serializer());
        builder.registerTypeAdapter(XboxLiveAuthRequest.class, new XboxLiveAuthRequest.Serializer());
        builder.registerTypeAdapter(XboxXstsAuthRequest.class, new XboxXstsAuthRequest.Serializer());
        builder.registerTypeAdapter(XboxAuthResponse.class, new XboxAuthResponse.Deserializer());
        GSON = builder.create();
    }

    public static <R extends ResponseObject> R performFormRequest(String url, RequestObject.Form<R> form) throws UnreachableServiceException, InvalidResponseException {
        return performWrappedFormRequest(url, form).get();
    }

    public static <R extends ResponseObject> Response<R> performWrappedFormRequest(String url, RequestObject.Form<R> form) throws UnreachableServiceException {
        String body = form.getFields().entrySet().stream().map(HttpUtil::urlEncode).collect(Collectors.joining("&"));
        return performRequest(url, body, "application/x-www-form-urlencoded", null, form.getResponseClass());
    }

    public static <R extends ResponseObject> R performJsonRequest(String url, RequestObject.JSON<R> payload) throws UnreachableServiceException, InvalidResponseException {
        return performWrappedJsonRequest(url, payload).get();
    }

    public static <R extends ResponseObject> Response<R> performWrappedJsonRequest(String url, RequestObject.JSON<R> payload) throws UnreachableServiceException {
        return performRequest(url, GSON.toJson(payload), "application/json", null, payload.getResponseClass());
    }

    public static <R extends ResponseObject> R performGetRequest(String url, String bearer, Class<R> responseType) throws UnreachableServiceException, InvalidResponseException {
        return performWrappedGetRequest(url, bearer, responseType).get();
    }

    public static <R extends ResponseObject> Response<R> performWrappedGetRequest(String url, String bearer, Class<R> responseType) throws UnreachableServiceException {
        return performRequest(url, null, null, bearer, responseType);
    }

    /**
     * Executes the Request and parses the returned Response.
     * <p>
     * {@link IllegalStateException} can occur in GSON when type-mismatches are encountered
     */
    private static <R extends ResponseObject> Response<R> performRequest(String url, String body, String contentType, String token, Class<R> responseType) throws UnreachableServiceException {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();

            SSLSocketFactory socketFactory = CertWorkaround.getSocketFactory();
            if (socketFactory != null) {
                connection.setSSLSocketFactory(socketFactory);
            }

            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(true);
            connection.setUseCaches(false);

            connection.setRequestProperty("Accept", "application/json");
            if (token != null) {
                connection.setRequestProperty("Authorization", "Bearer " + token);
            }
            if (body != null) {
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", contentType);
                connection.setDoOutput(true);
                try (OutputStream dataOut = connection.getOutputStream()) {
                    dataOut.write(body.getBytes(StandardCharsets.UTF_8));
                    dataOut.flush();
                }
            }

            connection.connect();

            int responseCode = connection.getResponseCode();
            InputStream inputStream = responseCode < 400 ? connection.getInputStream() : connection.getErrorStream();

            R response = null;
            if (inputStream.available() > 0) {
                try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                    response = GSON.fromJson(reader, responseType);
                }
            }

            return new Response<>(responseCode, response);
        } catch (ClassCastException | IOException e) {
            throw new UnreachableServiceException("Cannot reach server", e);
        } catch (IllegalStateException | JsonParseException e) {
            throw new UnreachableServiceException("Server is talking nonsense", e);
        }
    }

    private static String urlEncode(Map.Entry<String, String> entry) {
        try {
            return URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 unsupported", e);
        }
    }
}
