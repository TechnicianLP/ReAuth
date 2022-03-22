package technicianlp.reauth;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.text.TranslationTextComponent;
import technicianlp.reauth.authentication.flows.Flow;
import technicianlp.reauth.authentication.flows.Flows;
import technicianlp.reauth.configuration.Profile;
import technicianlp.reauth.gui.FlowScreen;
import technicianlp.reauth.util.ReflectionHelper;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

public final class ReconnectHelper {

    private static final Field managerField = ReflectionHelper.findMcpField(ConnectingScreen.class, "field_146371_g");
    private static final Field previousField = ReflectionHelper.findMcpField(ConnectingScreen.class, "field_146374_i");
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
        Minecraft mc = Minecraft.getInstance();
        FlowScreen flowScreen = new FlowScreen(mc.currentScreen);
        Flow flow = Flows.loginWithProfile(profile, flowScreen);
        flowScreen.setFlow(flow);
        flowScreen.disableAutoClose();
        CompletableFuture.allOf(flow.getSession(), flow.getProfile()).thenRunAsync(ReconnectHelper::connect, Minecraft.getInstance());
        mc.displayGuiScreen(flowScreen);
    }

    private static void connect() {
        if (screen != null) {
            SocketAddress add = ReflectionHelper.<NetworkManager>getField(managerField, screen).getRemoteAddress();
            if (add instanceof InetSocketAddress) {
                InetSocketAddress address = (InetSocketAddress) add;
                Minecraft client = Minecraft.getInstance();
                client.displayGuiScreen(new ConnectingScreen(ReflectionHelper.getField(previousField, screen), client, address.getHostString(), address.getPort()));
            }
        }
    }
}
