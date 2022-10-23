package technicianlp.reauth.authentication.http.server;

import com.sun.net.httpserver.HttpExchange;
import technicianlp.reauth.ReAuth;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.Locale;

final class ResourcesHandler extends Handler {

    ResourcesHandler(PageWriter pageWriter) {
        super(pageWriter);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod().toUpperCase(Locale.ROOT);

            Response response;
            if ("GET".equals(method) || "HEAD".equals(method)) {
                response = handleResourceGet(exchange.getRequestURI().getPath());
            } else if ("POST".equals(method)) {
                response = new Response(HttpStatus.Method_Not_Allowed).setHeader("Allow", "GET, HEAD");
            } else {
                response = new Response(HttpStatus.Not_Implemented);
            }

            this.sendResponse(exchange, response);
        } catch (Exception exception) {
            ReAuth.log.error("Exception while answering request", exception);
            throw exception;
        }
    }

    private static Response handleResourceGet(String path) throws IOException {
        String contentType = null;
        String resource = null;
        if ("/res/icon.png".equals(path)) {
            contentType = "image/png";
            resource = "/reauth/icon.png";
        } else if ("/res/logo.png".equals(path)) {
            contentType = "image/png";
            resource = "/reauth/logo.png";
        }

        if (resource != null) {
            try (InputStream is = AuthenticationCodeServer.class.getResourceAsStream(resource)) {
                if (is != null) {
                    ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE * 4);
                    Channels.newChannel(is).read(buffer);
                    return new Response(HttpStatus.OK).setContent(contentType, buffer);
                } else {
                    throw new FileNotFoundException("Resource " + resource + " is unavailable");
                }
            }
        } else {
            return new Response(HttpStatus.Not_Found);
        }
    }
}
