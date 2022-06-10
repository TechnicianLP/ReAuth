package technicianlp.reauth.authentication.flows.impl.util;

import technicianlp.reauth.authentication.http.InvalidResponseException;
import technicianlp.reauth.authentication.http.UnreachableServiceException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

/**
 * Functional interface that allows a method reference to throw {@link UnreachableServiceException} or {@link InvalidResponseException}.
 * Thrown Exceptions will be wrapped with a {@link CompletionException} for use with {@link CompletableFuture}.
 */
@FunctionalInterface
public interface AuthFunction<T, R> extends Function<T, R> {

    R applyAuthStep(T t) throws UnreachableServiceException, InvalidResponseException;

    @Override
    default R apply(T t) {
        try {
            return this.applyAuthStep(t);
        } catch (UnreachableServiceException | InvalidResponseException e) {
            throw new CompletionException(e);
        }
    }
}
