package technicianlp.reauth.mixinUtil;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.ClientConnection;
import net.minecraft.text.Text;

public interface ConnectScreenDuck {
    ClientConnection reauthGetConnection();

    Screen reauthGetParent();
}
