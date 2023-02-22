package technicianlp.reauth.authentication.http;

public final class InvalidResponseException extends Exception {

    public final Response<?> response;

    public InvalidResponseException(String message, Response<?> response) {
        super(message);
        this.response = response;
    }
}
