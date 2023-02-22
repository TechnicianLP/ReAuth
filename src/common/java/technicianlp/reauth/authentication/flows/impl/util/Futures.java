package technicianlp.reauth.authentication.flows.impl.util;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public final class Futures {

    public static <R> CompletableFuture<R> composeConditional(CompletableFuture<Boolean> condition, CompletableFuture<R> value, CompletableFuture<R> fallback) {
        return condition.thenCompose(cond -> cond ? value : fallback);
    }

    public static <R> CompletableFuture<R> composeConditional(CompletableFuture<Boolean> condition, Supplier<CompletableFuture<R>> value, CompletableFuture<R> fallback) {
        return condition.thenCompose(cond -> cond ? value.get() : fallback);
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
