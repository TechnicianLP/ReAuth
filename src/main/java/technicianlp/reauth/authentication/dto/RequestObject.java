package technicianlp.reauth.authentication.dto;

import java.util.Map;

/**
 * Base Interface for request payloads
 */
public interface RequestObject<R extends ResponseObject> {

    Class<R> getResponseClass();

    /**
     * Interface for form request payloads
     */
    interface Form<R extends ResponseObject> extends RequestObject<R> {

        Map<String, String> getFields();
    }

    /**
     * Interface for json request payloads
     */
    interface JSON<R extends ResponseObject> extends RequestObject<R> {
    }
}
