package technicianlp.reauth;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.TranslatableComponent;
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

    private static final Field managerField = ReflectionHelper.findMcpField(ConnectScreen.class, "f_95684_");
    private static final Field previousField = ReflectionHelper.findMcpField(ConnectScreen.class, "f_95686_");
    private static ConnectScreen screen;

    public static String getTranslationKey(Object component) {
        if (component instanceof TranslatableComponent) {
            return ((TranslatableComponent) component).getKey();
        }
        return "";
    }

    public static void setConnectScreen(ConnectScreen screen) {
        ReconnectHelper.screen = screen;
    }

    public static boolean hasConnectionInfo() {
        return screen != null;
    }

    public static void retryLogin(Profile profile) {
        FlowScreen flowScreen = new FlowScreen();
        Flow flow = Flows.loginWithProfile(profile, flowScreen);
        flowScreen.setFlow(flow);
        flowScreen.disableAutoClose();
        CompletableFuture.allOf(flow.getSession(), flow.getProfile()).thenRunAsync(ReconnectHelper::connect, Minecraft.getInstance());
        Minecraft.getInstance().pushGuiLayer(flowScreen);
    }

    private static void connect() {
        if (screen != null) {
            SocketAddress add = ReflectionHelper.<Connection>getField(managerField, screen).getRemoteAddress();
            if (add instanceof InetSocketAddress address) {
                Minecraft minecraft = Minecraft.getInstance();
                ServerAddress server = new ServerAddress(address.getHostString(), address.getPort());
                ConnectScreen.startConnecting(ReflectionHelper.getField(previousField, screen), minecraft, server, minecraft.getCurrentServer());
            }
        }
    }
}
