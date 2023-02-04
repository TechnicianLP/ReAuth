package technicianlp.reauth.authentication.flows;

import java.util.concurrent.Executor;

import technicianlp.reauth.authentication.SessionData;
import technicianlp.reauth.configuration.Profile;

public interface FlowCallback {

    /**
     * transition displayed stage & log
     */
    void transitionStage(FlowStage newStage);

    void onSessionComplete(SessionData session, Throwable throwable);

    void onProfileComplete(Profile profile, Throwable throwable);

    Executor getExecutor();

}
