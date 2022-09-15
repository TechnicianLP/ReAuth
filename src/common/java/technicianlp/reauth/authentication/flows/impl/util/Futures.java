package technicianlp.reauth.authentication.flows.impl.util;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Futures {

    public static <R> Function<Boolean, CompletableFuture<R>> conditional(CompletableFuture<R> value, CompletableFuture<R> fallback) {
        return condition -> condition ? value : fallback;
    }

    public static <R> Function<Boolean, CompletableFuture<R>> conditional(Supplier<CompletableFuture<R>> value, CompletableFuture<R> fallback) {
        return condition -> condition ? value.get() : fallback;
    }

    public static <R> CompletableFuture<R> failed(Throwable throwable) {
        CompletableFuture<R> future = new CompletableFuture<>();
        future.completeExceptionally(throwable);
        return future;
    }

    public static <R> CompletableFuture<R> cancelled() {
        CompletableFuture<R> future = new CompletableFuture<>();
        future.cancel(true);
        return future;
    }
}
