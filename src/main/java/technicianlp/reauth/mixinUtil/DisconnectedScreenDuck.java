package technicianlp.reauth.mixinUtil;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;


public interface DisconnectedScreenDuck {
    Text reauthGetReason();

    Screen reauthGetParent();
}
