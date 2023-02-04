package technicianlp.reauth.authentication.http.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpServer;

import technicianlp.reauth.ReAuth;

/**
 * Authentication with Microsoft requires a webserver on localhost to receive the issued authentication code.
 * <br>
 * Class provides encapsulation of the jdk-builtin {@link HttpServer}.
 * The API provided by the {@link com.sun.net.httpserver} package is an official and documented API since Java 6.
 * While the API should be available in every major java distribution, it is an optional API and may therefore be missing in rare cases.
 * This is denoted by both the {@link jdk.Exported} Annotation and <a href="https://docs.oracle.com/en/java/javase/17/docs/api/index.html">online documentation</a>.
 * <br>
 * The HttpServer is started on the supplied Executor.
 * After the code has been received the future is completed synchronously and the server automatically {@link #stop(boolean) stops} asynchronously.
 */
public final class AuthenticationCodeServer {

    private final Consumer<Boolean> stopServer;

    private boolean running = true;

    public AuthenticationCodeServer(int port, String loginUrl, CompletableFuture<String> codeFuture, Executor executor) throws IOException, NoClassDefFoundError {
        InetSocketAddress localAddress = new InetSocketAddress(InetAddress.getByAddress("localhost", new byte[]{127, 0, 0, 1}), port);
        HttpServer server = HttpServer.create(localAddress, 0);
        server.setExecutor(executor);

        PageWriter writer = new PageWriter(loginUrl);
        server.createContext("/", new CodeHandler(writer, codeFuture));
        server.createContext("/res/", new ResourcesHandler(writer));

        codeFuture.whenCompleteAsync((v, exception) -> this.stop(exception != null), executor);

        ReAuth.log.info("Starting local endpoint");
        server.start();
        ReAuth.log.info("Started local endpoint");

        this.stopServer = (immediate) -> server.stop(immediate ? 0 : 1);
    }

    public final synchronized void stop(boolean immediate) {
        if (this.running) {
            this.running = false;
            if (!immediate) {
                ReAuth.log.info("About to stop local endpoint");
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException exception) {
                    ReAuth.log.warn("Interrupted while waiting to stop local endpoint", exception);
                }
            }
            ReAuth.log.info("Stopping local endpoint");
            this.stopServer.accept(immediate);
            ReAuth.log.info("Stopped local endpoint");
        }
    }
}
