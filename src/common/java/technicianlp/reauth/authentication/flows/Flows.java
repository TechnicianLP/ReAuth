package technicianlp.reauth.authentication.flows;

import technicianlp.reauth.authentication.flows.impl.MicrosoftCodeFlow;
import technicianlp.reauth.authentication.flows.impl.MicrosoftDeviceFlow;
import technicianlp.reauth.authentication.flows.impl.MicrosoftProfileFlow;
import technicianlp.reauth.authentication.flows.impl.MojangAuthenticationFlow;
import technicianlp.reauth.authentication.flows.impl.UnknownProfileFlow;
import technicianlp.reauth.configuration.Profile;
import technicianlp.reauth.configuration.ProfileConstants;

public final class Flows {

    public static Flow loginWithProfile(Profile profile, FlowCallback callback) {
        switch (profile.getValue(ProfileConstants.PROFILE_TYPE)) {
            case ProfileConstants.PROFILE_TYPE_MICROSOFT:
                return new MicrosoftProfileFlow(profile, callback);
            case ProfileConstants.PROFILE_TYPE_MOJANG:
                return new MojangAuthenticationFlow(profile, callback);
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

    public static Flow loginWithMojang(String username, String password, boolean persist, FlowCallback callback) {
        return new MojangAuthenticationFlow(username, password, persist, callback);
    }
}
