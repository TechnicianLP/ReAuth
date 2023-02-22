package technicianlp.reauth.authentication.http;

import technicianlp.reauth.authentication.dto.ResponseObject;

import java.net.HttpURLConnection;

public final class Response<R extends ResponseObject> {

    private final int statusCode;
    private final R response;

    public Response(int statusCode, R response) {
        this.statusCode = statusCode;
        this.response = response;
    }

    public final boolean isValid() {
        if (!(200 <= this.statusCode && this.statusCode < 300)) {
            return false;
        }
        if (this.response != null) {
            return this.response.isValid();
        } else {
            return this.statusCode == HttpURLConnection.HTTP_NO_CONTENT;
        }
    }

    public final R get() throws InvalidResponseException {
        if (this.isValid()) {
            return this.response;
        } else if (this.response != null) {
            throw new InvalidResponseException("Error received with code " + this.statusCode + ": " + this.response.getError(), this);
        } else {
            throw new InvalidResponseException("Received error code " + this.statusCode, this);
        }
    }

    public final R getUnchecked() {
        return this.response;
    }
}
