package technicianlp.reauth.authentication.http;

import org.apache.http.HttpStatus;
import technicianlp.reauth.authentication.dto.ResponseObject;

public final class Response<R extends ResponseObject> {

    private final int statusCode;
    private final R response;

    public Response(int statusCode, R response) {
        this.statusCode = statusCode;
        this.response = response;
    }

    public final boolean isValid() {
        if (this.response != null) {
            return this.response.isValid();
        } else {
            return this.statusCode == HttpStatus.SC_NO_CONTENT;
        }
    }

    public final R get() throws InvalidResponseException {
        if (this.isValid()) {
            return this.response;
        } else if (this.response != null) {
            throw new InvalidResponseException("Error received with code " + this.statusCode + ": "+this.response.getError());
        } else {
            throw new InvalidResponseException("Received error code " + this.statusCode);
        }
    }

    public final R getUnchecked() {
        return this.response;
    }
}
