package technicianlp.reauth.authentication.flows;

import technicianlp.reauth.authentication.flows.impl.*;
import technicianlp.reauth.configuration.Profile;
import technicianlp.reauth.configuration.ProfileConstants;

public enum Flows {
    ;

    public static Flow loginWithProfile(Profile profile, FlowCallback callback) {
        return switch (profile.getValue(ProfileConstants.PROFILE_TYPE)) {
            case ProfileConstants.PROFILE_TYPE_MICROSOFT -> new MicrosoftProfileFlow(profile, callback);
            case ProfileConstants.PROFILE_TYPE_MOJANG -> new MojangAuthenticationFlow(profile, callback);
            default -> new UnknownProfileFlow(callback);
        };
    }

    public static AuthorizationCodeFlow loginWithAuthCode(boolean persist, FlowCallback callback) {
        return new MicrosoftCodeFlow(persist, callback);
    }

    public static DeviceCodeFlow loginWithDeviceCode(boolean persist, FlowCallback callback) {
        return new MicrosoftDeviceFlow(persist, callback);
    }

}
