package technicianlp.reauth.authentication.flows;

import java.util.concurrent.Executor;

public interface FlowCallback {

    /**
     * transition displayed stage & log
     */
    void transitionStage(FlowStage newStage);

    Executor getExecutor();
}
