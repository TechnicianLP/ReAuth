package technicianlp.reauth.authentication.http.server;

import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.io.IOUtils;
import technicianlp.reauth.ReAuth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

final class CodeHandler extends Handler {

    private final CompletableFuture<String> codeFuture;

    CodeHandler(PageWriter writer, CompletableFuture<String> codeFuture) {
        super(writer);
        this.codeFuture = codeFuture;
    }

    @Override
    public final void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod().toUpperCase(Locale.ROOT);
            Response response;
            if ("POST".equals(method)) {
                response = this.handlePostRequest(exchange);
            } else if ("HEAD".equals(method) || "GET".equals(method)) {
                response = this.handleQueryRequest(exchange);
            } else {
                response = new Response(HttpStatus.Not_Implemented);
            }

            this.sendResponse(exchange, response);
        } catch (Exception exception) {
            ReAuth.log.error("Exception while answering request", exception);
            throw exception;
        }
    }

    private Response handleQueryRequest(HttpExchange exchange) throws IOException {
        return this.handleRequest(exchange.getRequestURI().getRawQuery());
    }

    private Response handlePostRequest(HttpExchange exchange) throws IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (!contentType.startsWith("application/x-www-form-urlencoded")) {
            return new Response(HttpStatus.Unsupported_Media_Type);
        }

        return this.handleRequest(IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8));
    }

    private Response handleRequest(String urlEncodedParameters) throws IOException {
        Map<String, String> parameters = this.parseUrlEncodedParameters(urlEncodedParameters);

        if (parameters.containsKey("code")) {
            ReAuth.log.info("Received Microsoft Authentication Code");
            this.codeFuture.complete(parameters.get("code"));
            return new Response(HttpStatus.OK).setContent(CONTENT_TYPE_HTML, this.pageWriter.createSuccessResponsePage());
        } else {
            String error = parameters.getOrDefault("error", "unknown");
            ReAuth.log.error("Received Error from Microsoft Authentication: " + error);
            return new Response(HttpStatus.Bad_Request).setContent(CONTENT_TYPE_HTML, this.pageWriter.createErrorResponsePage(error));
        }
    }

    /**
     * Decodes the Contents of an application/x-www-form-urlencoded request or a query String.
     * Duplicate field names are discarded.
     */
    private Map<String, String> parseUrlEncodedParameters(String urlEncodedParameters) {
        Map<String, String> parameters = new HashMap<>();
        if (urlEncodedParameters == null)
            return parameters;
        String[] fields = urlEncodedParameters.split("&");

        try {
            for (String field : fields) {
                if (field.isEmpty()) {
                    continue;
                }
                String key = field;
                String value = "";

                int delimiter = field.indexOf('=');
                if (delimiter != -1) {
                    key = field.substring(0, delimiter);
                    value = field.substring(delimiter + 1);
                }

                parameters.putIfAbsent(URLDecoder.decode(key, "UTF-8"), URLDecoder.decode(value, "UTF-8"));
            }
        } catch (UnsupportedEncodingException exception) {
            throw new RuntimeException("UTF-8 unsupported", exception);
        }
        return parameters;
    }
}
