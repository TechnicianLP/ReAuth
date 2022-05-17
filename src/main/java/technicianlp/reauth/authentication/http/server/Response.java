package technicianlp.reauth.authentication.http.server;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

final class Response {

    private final HttpStatus httpStatus;
    private final Map<String, String> headers;
    private ByteBuffer pageContent;

    Response(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        this.headers = new HashMap<>();
    }

    final Response setContent(String contentType, ByteBuffer content) {
        this.setHeader("Content-Type", contentType);
        this.pageContent = content;
        return this;
    }

    final Response setHeader(String name, String value) {
        this.getHeaders().put(name, value);
        return this;
    }

    final HttpStatus getHttpStatus() {
        return this.httpStatus;
    }

    final boolean hasContent() {
        return this.pageContent != null;
    }

    final ByteBuffer getPageContent() {
        return this.pageContent;
    }

    final Map<String, String> getHeaders() {
        return this.headers;
    }
}
