package technicianlp.reauth.authentication.flows;

import technicianlp.reauth.authentication.SessionData;
import technicianlp.reauth.configuration.Profile;

import java.util.concurrent.Executor;

public interface FlowCallback {

    /**
     * transition displayed stage & log
     */
    void transitionStage(FlowStage newStage);

    void onSessionComplete(SessionData session, Throwable throwable);

    void onProfileComplete(Profile profile, Throwable throwable);

    Executor getExecutor();

}
