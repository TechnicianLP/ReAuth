package technicianlp.reauth.authentication.dto;

/**
 * Interface for response payloads
 */
public interface ResponseObject {

    /**
     * checks whether the request was successful and all required fields have been sent
     */
    boolean isValid();

    /**
     * returns the errormessage returned by the service for a failed request
     */
    String getError();
}
