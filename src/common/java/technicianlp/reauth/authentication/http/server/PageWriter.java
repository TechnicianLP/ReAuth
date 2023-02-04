package technicianlp.reauth.authentication.http.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import technicianlp.reauth.ReAuth;

final class PageWriter {

    private static final int BUFFER_SIZE = 8192;

    private final String loginUrl;

    PageWriter(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    final ByteBuffer createSuccessResponsePage() throws IOException {
        String successMessage = this.formatAndEscape("reauth.msauth.code.success");
        String closeMessage = this.formatAndEscape("reauth.msauth.code.success.close");
        return this.createPage(successMessage, closeMessage);
    }

    final ByteBuffer createErrorResponsePage(String errorCode) throws IOException {
        String errorMessage = this.formatAndEscape(this.getErrorMessage(errorCode));
        String retryMessage = this.createLink(this.formatAndEscape("reauth.msauth.code.retry"), this.loginUrl);
        return this.createPage(errorMessage, retryMessage);
    }

    final ByteBuffer createHttpErrorResponsePage(HttpStatus error) throws IOException {
        String errorMessage = this.formatAndEscape("reauth.msauth.code.error.http" + error.code);
        String retryMessage = this.createLink(this.formatAndEscape("reauth.msauth.code.retry"), this.loginUrl);
        return this.createPage(errorMessage, retryMessage);
    }

    private ByteBuffer createPage(String text1, String text2) throws IOException {
        try (InputStream is = AuthenticationCodeServer.class.getResourceAsStream("/resources/reauth/reauth.html")) {
            if (is != null) {
                String page = IOUtils.toString(is, StandardCharsets.UTF_8);
                page = page.replace("$text1", text1).replace("$text2", text2);
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                buffer.put(page.getBytes(StandardCharsets.UTF_8));
                return buffer;
            } else {
                throw new FileNotFoundException("Resource /resources/reauth/reauth.html is unavailable");
            }
        }
    }

    private String createLink(String content, String href) {
        return "<a href=" + href + ">" + content + "</a>";
    }

    private String getErrorMessage(String authError) {
        String type = "unknown";
        switch (authError) {
            case "access_denied":
                type = "cancelled";
                break;
            case "server_error":
            case "temporarily_unavailable":
                type = "server";
                break;
        }
        return "reauth.msauth.code.fail." + type;
    }

    private String formatAndEscape(String key, Object... arguments) {
        String text = ReAuth.i18n.apply(key, arguments);
        text = text.replaceAll("&", "&amp;");
        text = text.replaceAll("<", "&lt;");
        text = text.replaceAll(">", "&gt;");
        text = text.replaceAll("\"", "&qout;");
        text = text.replaceAll("'", "&apos;");
        return text;
    }
}
