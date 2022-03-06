package technicianlp.reauth.authentication.http;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import technicianlp.reauth.authentication.dto.RequestObject;
import technicianlp.reauth.authentication.dto.ResponseObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public final class HttpUtil {

    public static <R extends ResponseObject> R performFormRequest(String url, RequestObject.Form<R> form) throws UnreachableServiceException, InvalidResponseException {
        return performWrappedFormRequest(url, form).get();
    }

    public static <R extends ResponseObject> Response<R> performWrappedFormRequest(String url, RequestObject.Form<R> form) throws UnreachableServiceException {
        HttpPost request = new HttpPost(url);
        List<NameValuePair> fields = form.getFields().entrySet().stream().map(e -> new BasicNameValuePair(e.getKey(), e.getValue())).collect(Collectors.toList());
        request.setEntity(new UrlEncodedFormEntity(fields, StandardCharsets.UTF_8));
        request.addHeader("Accept", "application/json");

        return performRequest(request, form.getResponseClass());
    }

    public static <R extends ResponseObject> R performJsonRequest(String url, RequestObject.JSON<R> payload) throws UnreachableServiceException, InvalidResponseException {
        return performWrappedJsonRequest(url, payload).get();
    }

    public static <R extends ResponseObject> Response<R> performWrappedJsonRequest(String url, RequestObject.JSON<R> payload) throws UnreachableServiceException {
        HttpPost request = new HttpPost(url);
        request.setEntity(new StringEntity(new Gson().toJson(payload), ContentType.APPLICATION_JSON));
        request.addHeader("Accept", "application/json");

        return performRequest(request, payload.getResponseClass());
    }

    public static <R extends ResponseObject> R performGetRequest(String url, String bearer, Class<R> responseType) throws UnreachableServiceException, InvalidResponseException {
        return performWrappedGetRequest(url, bearer, responseType).get();
    }

    public static <R extends ResponseObject> Response<R> performWrappedGetRequest(String url, String bearer, Class<R> responseType) throws UnreachableServiceException {
        HttpGet request = new HttpGet(url);
        request.addHeader("Authorization", "Bearer " + bearer);
        request.addHeader("Accept", "application/json");

        return performRequest(request, responseType);
    }

    /**
     * Executes the provided {@link HttpUriRequest} and parses the returned Response.
     * <p>
     * {@link IllegalStateException} can occur in GSON when type-mismatches are encountered
     */
    private static <R extends ResponseObject> Response<R> performRequest(HttpUriRequest request, Class<R> responseType) throws UnreachableServiceException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse httpResponse = client.execute(request);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            R response = null;

            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                Reader reader = new InputStreamReader(httpEntity.getContent(), StandardCharsets.UTF_8);
                response = new Gson().fromJson(reader, responseType);
            }

            return new Response<>(statusCode, response);
        } catch (IOException e) {
            throw new UnreachableServiceException("Cannot reach server", e);
        } catch (IllegalStateException | JsonParseException e) {
            throw new UnreachableServiceException("Server is talking nonsense", e);
        }
    }
}
