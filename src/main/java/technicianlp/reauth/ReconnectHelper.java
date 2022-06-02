package technicianlp.reauth;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import technicianlp.reauth.authentication.flows.Flow;
import technicianlp.reauth.authentication.flows.Flows;
import technicianlp.reauth.configuration.Profile;
import technicianlp.reauth.gui.FlowScreen;
import technicianlp.reauth.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public final class ReconnectHelper {

    private static final Field managerField = ReflectionUtils.findMcpField(GuiConnecting.class, "field_146371_g", "networkManager");
    private static final Field previousField = ReflectionUtils.findMcpField(GuiConnecting.class, "field_146374_i", "previousGuiScreen");
    private static GuiConnecting screen;

    public static String getTranslationKey(IChatComponent component) {
        if (component instanceof ChatComponentTranslation) {
            return ((ChatComponentTranslation) component).getKey();
        }
        return "";
    }

    public static void setConnectScreen(GuiConnecting screen) {
        ReconnectHelper.screen = screen;
    }

    public static boolean hasConnectionInfo() {
        return screen != null;
    }

    public static void retryLogin(Profile profile) {
        Flow flow = FlowScreen.open(Flows::loginWithProfile, profile, Minecraft.getMinecraft().currentScreen);
        flow.thenRunAsync(ReconnectHelper::connect, Minecraft.getMinecraft()::addScheduledTask);
    }

    private static void connect() {
        if (screen != null) {
            SocketAddress add = ReflectionUtils.<NetworkManager>getField(managerField, screen).getRemoteAddress();
            if (add instanceof InetSocketAddress) {
                InetSocketAddress address = (InetSocketAddress) add;
                Minecraft client = Minecraft.getMinecraft();
                client.displayGuiScreen(new GuiConnecting(ReflectionUtils.getField(previousField, screen), client, address.getHostString(), address.getPort()));
            }
        }
    }
}
