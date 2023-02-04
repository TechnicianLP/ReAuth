package technicianlp.reauth.authentication.http.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.IOUtils;

import com.sun.net.httpserver.HttpExchange;

import technicianlp.reauth.ReAuth;

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
                response = new Response(HttpStatus.Method_Not_Allowed).setHeader("Allow", "POST");
            } else {
                response = new Response(HttpStatus.Not_Implemented);
            }

            this.sendResponse(exchange, response);
        } catch (Exception exception) {
            ReAuth.log.error("Exception while answering request", exception);
            throw exception;
        }
    }

    private Response handlePostRequest(HttpExchange exchange) throws IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (!contentType.startsWith("application/x-www-form-urlencoded")) {
            return new Response(HttpStatus.Unsupported_Media_Type);
        }

        String body = IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8);
        Map<String, String> formFields = (this.parseFormFields(body));

        if (formFields.containsKey("code")) {
            ReAuth.log.info("Received Microsoft Authentication Code");
            this.codeFuture.complete(formFields.get("code"));
            return new Response(HttpStatus.OK).setContent(CONTENT_TYPE_HTML, this.pageWriter.createSuccessResponsePage());
        } else {
            String error = formFields.getOrDefault("error", "unknown");
            ReAuth.log.error("Received Error from Microsoft Authentication: " + error);
            return new Response(HttpStatus.Bad_Request).setContent(CONTENT_TYPE_HTML, this.pageWriter.createErrorResponsePage(error));
        }
    }

    /**
     * Decodes the Contents of a application/x-www-form-urlencoded request.
     * Duplicate field names are discarded.
     */
    private Map<String, String> parseFormFields(String formUrlEncoded) {
        Map<String, String> formFields = new HashMap<>();
        String[] fields = formUrlEncoded.split("&");

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

                formFields.putIfAbsent(URLDecoder.decode(key, "UTF-8"), URLDecoder.decode(value, "UTF-8"));
            }
        } catch (UnsupportedEncodingException exception) {
            throw new RuntimeException("UTF-8 unsupported", exception);
        }
        return formFields;
    }
}
