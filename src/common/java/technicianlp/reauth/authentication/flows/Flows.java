package technicianlp.reauth.authentication.flows;

import technicianlp.reauth.authentication.flows.impl.MicrosoftCodeFlow;
import technicianlp.reauth.authentication.flows.impl.MicrosoftDeviceFlow;
import technicianlp.reauth.authentication.flows.impl.MicrosoftProfileFlow;
import technicianlp.reauth.authentication.flows.impl.UnknownProfileFlow;
import technicianlp.reauth.authentication.http.InvalidResponseException;
import technicianlp.reauth.authentication.http.Response;
import technicianlp.reauth.authentication.http.UnreachableServiceException;
import technicianlp.reauth.configuration.Profile;
import technicianlp.reauth.configuration.ProfileConstants;
import technicianlp.reauth.crypto.CryptoException;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;

public final class Flows {

    public static Flow loginWithProfile(Profile profile, FlowCallback callback) {
        switch (profile.getValue(ProfileConstants.PROFILE_TYPE)) {
            case ProfileConstants.PROFILE_TYPE_MICROSOFT:
                return new MicrosoftProfileFlow(profile, callback);
            default:
                return new UnknownProfileFlow(callback);
        }
    }

    public static AuthorizationCodeFlow loginWithAuthCode(boolean persist, FlowCallback callback) {
        return new MicrosoftCodeFlow(persist, callback);
    }

    public static DeviceCodeFlow loginWithDeviceCode(boolean persist, FlowCallback callback) {
        return new MicrosoftDeviceFlow(persist, callback);
    }

    public static String getFailureReason(Flow flow) {
        try {
            flow.getSession().getNow(null);
            return "reauth.error.none";
        } catch (CancellationException ce) {
            return "reauth.error.cancelled";
        } catch (CompletionException exception) {
            Throwable failure = exception.getCause();
            if (failure instanceof CryptoException) {
                return "reauth.error.crypto";
            } else if (failure instanceof UnreachableServiceException) {
                return "reauth.error.network";
            } else if (failure instanceof InvalidResponseException) {
                Response<?> response = ((InvalidResponseException) failure).response;
                String description = response.getUnchecked().getErrorDescription();
                if (description != null) {
                    return description;
                }
            }
        }
        return "reauth.error.unknown";
    }
}
