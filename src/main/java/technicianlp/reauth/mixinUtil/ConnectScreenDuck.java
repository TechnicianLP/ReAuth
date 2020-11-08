package technicianlp.reauth.mixinUtil;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.ClientConnection;

public interface ConnectScreenDuck {
    ClientConnection reauthGetConnection();

    Screen reauthGetParent();
}
