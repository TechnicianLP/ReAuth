package technicianlp.reauth.authentication.flows.impl;

import technicianlp.reauth.authentication.SessionData;
import technicianlp.reauth.authentication.flows.Flow;
import technicianlp.reauth.authentication.flows.FlowCallback;
import technicianlp.reauth.authentication.flows.FlowStage;
import technicianlp.reauth.authentication.flows.impl.util.AuthBiFunction;
import technicianlp.reauth.authentication.flows.impl.util.AuthFunction;
import technicianlp.reauth.configuration.Profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;

abstract class FlowBase implements Flow {

    private final List<CompletableFuture<?>> stages = new ArrayList<>();
    private final List<FlowBase> flows = new ArrayList<>();

    private final FlowCallback callback;
    final Executor executor;

    protected FlowBase(FlowCallback callback) {
        this.callback = callback;
        this.executor = callback.getExecutor();
    }

    /**
     * Register dependant {@link CompletionStage} for cancellation handling.
     * Every major stage should be registered for handling as cancelling a completed stage does not propagate to its dependants.
     */
    final void registerDependantStages(CompletableFuture<?>... stages) {
        Collections.addAll(this.stages, stages);
    }

    /**
     * Register a dependant {@link FlowBase} for cancellation handling.
     * Used to handle potential cleanup operations as the flows stages should already have been cancelled
     */
    final void registerDependantFlow(FlowBase flow) {
        this.stages.add(flow.getSession());
        this.flows.add(flow);
    }

    /**
     * Cancel the flow and its dependants.
     * Cancels registered dependant stages in order.
     * Requests cancellation of dependant flows.
     */
    @Override
    public void cancel() {
        this.stages.forEach(stage -> stage.cancel(true));
        this.flows.forEach(FlowBase::cancel);
        this.getSession().cancel(true);
    }

    final void onSessionComplete(SessionData data, Throwable throwable) {
        if (throwable == null) {
            if (this.hasProfile() && !this.getProfile().isDone()) {
                this.step(FlowStage.PROFILE);
            } else {
                this.step(FlowStage.FINISHED);
            }
        } else {
            this.step(FlowStage.FAILED);
        }
        this.callback.onSessionComplete(data, throwable);
    }

    final void onProfileComplete(Profile profile, Throwable throwable) {
        if (throwable == null) {
            if (this.getSession().isDone() && !this.getSession().isCompletedExceptionally()) {
                this.step(FlowStage.FINISHED);
            }
        }
        this.callback.onProfileComplete(profile, throwable);
    }

    final <T, U, R> BiFunction<T, U, R> wrapStep(FlowStage stage, AuthBiFunction<T, U, R> step) {
        return (t, u) -> {
            this.step(stage);
            return step.apply(t, u);
        };
    }

    final <T, R> Function<T, R> wrapStep(FlowStage stage, AuthFunction<T, R> step) {
        return (t) -> {
            this.step(stage);
            return step.apply(t);
        };
    }

    final <T, R> Function<T, R> wrap(AuthFunction<T, R> step) {
        return step;
    }

    final void step(FlowStage stage) {
        this.callback.transitionStage(stage);
    }
}
