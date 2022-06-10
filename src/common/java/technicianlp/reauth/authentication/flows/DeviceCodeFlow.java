package technicianlp.reauth.authentication.flows;

import java.util.concurrent.CompletableFuture;

public interface DeviceCodeFlow extends Flow {

    CompletableFuture<String> getLoginUrl();

    CompletableFuture<String> getCode();
}
