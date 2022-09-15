package technicianlp.reauth.authentication.http;

/**
 * Wrapper Exception for use when services cannot be reached or answers cannot be decoded
 */
public final class UnreachableServiceException extends Exception {

    UnreachableServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
