package technicianlp.reauth.authentication.flows.impl.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

import technicianlp.reauth.authentication.http.InvalidResponseException;
import technicianlp.reauth.authentication.http.UnreachableServiceException;

/**
 * Functional interface that allows a method reference to throw {@link UnreachableServiceException} or {@link InvalidResponseException}.
 * Thrown Exceptions will be wrapped with a {@link CompletionException} for use with {@link CompletableFuture}.
 */
@FunctionalInterface
public interface AuthSupplier<T> extends Supplier<T> {

    T getAuthStep() throws UnreachableServiceException, InvalidResponseException;

    @Override
    default T get() {
        try {
            return this.getAuthStep();
        } catch (UnreachableServiceException | InvalidResponseException e) {
            throw new CompletionException(e);
        }
    }
}
