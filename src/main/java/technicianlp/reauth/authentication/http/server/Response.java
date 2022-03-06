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

    Response setContent(String contentType, ByteBuffer content) {
        this.setHeader("Content-Type", contentType);
        this.pageContent = content;
        return this;
    }

    Response setHeader(String name, String value) {
        this.getHeaders().put(name, value);
        return this;
    }

    HttpStatus getHttpStatus() {
        return this.httpStatus;
    }

    boolean hasContent() {
        return this.pageContent != null;
    }

    ByteBuffer getPageContent() {
        return this.pageContent;
    }

    Map<String, String> getHeaders() {
        return this.headers;
    }
}
