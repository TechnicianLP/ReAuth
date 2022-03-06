package technicianlp.reauth.authentication.http.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;

abstract class Handler implements HttpHandler {

    static final int BUFFER_SIZE = 8192;
    static final String CONTENT_TYPE_HTML = "text/html; charset=UTF-8";

    final PageWriter pageWriter;

    Handler(PageWriter pageWriter) {
        this.pageWriter = pageWriter;
    }

    final void sendResponse(HttpExchange exchange, Response response) throws IOException {
        Headers responseHeaders = exchange.getResponseHeaders();
        response.getHeaders().forEach(responseHeaders::set);

        if (exchange.getRequestMethod().equals("HEAD")) {
            exchange.sendResponseHeaders(response.getHttpStatus().code, -1);
        } else {
            if (!response.hasContent()) {
                ByteBuffer buffer = this.pageWriter.createHttpErrorResponsePage(response.getHttpStatus());
                response.setContent(CONTENT_TYPE_HTML, buffer);
            }
            ByteBuffer pageContent = response.getPageContent();
            pageContent.flip();
            exchange.sendResponseHeaders(response.getHttpStatus().code, pageContent.limit());
            Channels.newChannel(exchange.getResponseBody()).write(pageContent);
        }
        exchange.close();
    }
}
