package technicianlp.reauth;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.text.TranslationTextComponent;
import technicianlp.reauth.authentication.flows.Flow;
import technicianlp.reauth.authentication.flows.Flows;
import technicianlp.reauth.configuration.Profile;
import technicianlp.reauth.gui.FlowScreen;
import technicianlp.reauth.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public final class ReconnectHelper {

    private static final Field managerField = ReflectionUtils.findObfuscatedField(ConnectingScreen.class, "field_146371_g", "networkManager");
    private static final Field previousField = ReflectionUtils.findObfuscatedField(ConnectingScreen.class, "field_146374_i", "previousGuiScreen");
    private static ConnectingScreen screen;

    public static String getTranslationKey(Object component) {
        if (component instanceof TranslationTextComponent) {
            return ((TranslationTextComponent) component).getKey();
        }
        return "";
    }

    public static void setConnectScreen(ConnectingScreen screen) {
        ReconnectHelper.screen = screen;
    }

    public static boolean hasConnectionInfo() {
        return screen != null;
    }

    public static void retryLogin(Profile profile) {
        Flow flow = FlowScreen.open(Flows::loginWithProfile, profile, true);
        flow.thenRunAsync(ReconnectHelper::connect, Minecraft.getInstance());
    }

    private static void connect() {
        if (screen != null) {
            SocketAddress add = ReflectionUtils.<NetworkManager>getField(managerField, screen).getRemoteAddress();
            if (add instanceof InetSocketAddress) {
                InetSocketAddress address = (InetSocketAddress) add;
                Minecraft client = Minecraft.getInstance();
                client.displayGuiScreen(new ConnectingScreen(ReflectionUtils.getField(previousField, screen), client, address.getHostString(), address.getPort()));
            }
        }
    }
}
