package technicianlp.reauth.authentication.http.server;

import com.sun.net.httpserver.HttpServer;
import technicianlp.reauth.ReAuth;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Authentication with Microsoft requires a webserver on localhost to receive the issued authentication code.
 * <br>
 * Class provides encapsulation of the jdk-builtin {@link HttpServer}.
 * The API provided by the {@link com.sun.net.httpserver} package is an official and documented API since Java 6.
 * While the API should be available in every major java distribution, it is an optional API and may therefore be missing in rare cases.
 * This is denoted by both the {@link jdk.Exported} Annotation and <a href="https://docs.oracle.com/en/java/javase/17/docs/api/index.html">online documentation</a>.
 * <br>
 * The HttpServer is started on the supplied Executor.
 * After the code has been received the future is completed synchronously and the server automatically {@link #stop() stops} asynchronously.
 */
public final class AuthenticationCodeServer {

    private final Runnable stopServer;
    private final Executor executor;

    private boolean running = true;

    public AuthenticationCodeServer(int port, String loginUrl, CompletableFuture<String> codeFuture, Executor executor) throws IOException, NoClassDefFoundError {
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 0);
        this.executor = executor;
        server.setExecutor(executor);

        PageWriter writer = new PageWriter(loginUrl);
        server.createContext("/", new CodeHandler(writer, codeFuture));
        server.createContext("/res/", new ResourcesHandler(writer));

        codeFuture.whenCompleteAsync((v, t) -> this.stop(), executor);

        ReAuth.log.info("Starting local endpoint");
        server.start();
        ReAuth.log.info("Started local endpoint");

        this.stopServer = () -> {
            ReAuth.log.info("Stopping local endpoint");
            server.stop(1);
            ReAuth.log.info("Stopped local endpoint");
        };
    }

    public final synchronized void stop() {
        if (this.running) {
            this.executor.execute(this.stopServer);
            this.running = false;
        }
    }
}
