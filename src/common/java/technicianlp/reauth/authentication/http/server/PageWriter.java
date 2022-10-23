package technicianlp.reauth.authentication.http.server;

import org.apache.commons.io.IOUtils;
import technicianlp.reauth.ReAuth;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

final class PageWriter {

    private static final int BUFFER_SIZE = 8192;

    private final String loginUrl;

    PageWriter(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    static ByteBuffer createSuccessResponsePage() throws IOException {
        String successMessage = formatAndEscape("reauth.msauth.code.success");
        String closeMessage = formatAndEscape("reauth.msauth.code.success.close");
        return createPage(successMessage, closeMessage);
    }

    ByteBuffer createErrorResponsePage(String errorCode) throws IOException {
        String errorMessage = formatAndEscape(getErrorMessage(errorCode));
        String retryMessage =
            createLink(formatAndEscape("reauth.msauth.code.retry"), this.loginUrl);
        return createPage(errorMessage, retryMessage);
    }

    ByteBuffer createHttpErrorResponsePage(HttpStatus error) throws IOException {
        String errorMessage = formatAndEscape("reauth.msauth.code.error.http." + error.code);
        String retryMessage =
            createLink(formatAndEscape("reauth.msauth.code.retry"), this.loginUrl);
        return createPage(errorMessage, retryMessage);
    }

    private static ByteBuffer createPage(String text1, String text2) throws IOException {
        try (InputStream is = AuthenticationCodeServer.class.getResourceAsStream("/reauth/reauth.html")) {
            if (is != null) {
                String page = IOUtils.toString(is, StandardCharsets.UTF_8);
                page = page.replace("$text1", text1).replace("$text2", text2);
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                buffer.put(page.getBytes(StandardCharsets.UTF_8));
                return buffer;
            } else {
                throw new FileNotFoundException("Resource /reauth/reauth.html is unavailable");
            }
        }
    }

    private static String createLink(String content, String href) {
        return "<a href=" + href + ">" + content + "</a>";
    }

    private static String getErrorMessage(String authError) {
        String type = switch (authError) {
            case "access_denied" -> "cancelled";
            case "server_error", "temporarily_unavailable" -> "server";
            default -> "unknown";
        };
        return "reauth.msauth.code.fail." + type;
    }

    private static String formatAndEscape(String key, Object... arguments) {
        String text = ReAuth.i18n.apply(key, arguments);
        text = text.replaceAll("&", "&amp;");
        text = text.replaceAll("<", "&lt;");
        text = text.replaceAll(">", "&gt;");
        text = text.replaceAll("\"", "&qout;");
        text = text.replaceAll("'", "&apos;");
        return text;
    }
}
