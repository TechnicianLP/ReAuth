package technicianlp.reauth;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import technicianlp.reauth.authentication.flows.Flow;
import technicianlp.reauth.authentication.flows.Flows;
import technicianlp.reauth.configuration.Profile;
import technicianlp.reauth.gui.FlowScreen;
import technicianlp.reauth.util.ReflectionHelper;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public final class ReconnectHelper {

    private static final Field managerField = ReflectionHelper.findMcpField(GuiConnecting.class, "field_146371_g");
    private static final Field previousField = ReflectionHelper.findMcpField(GuiConnecting.class, "field_146374_i");
    private static GuiConnecting screen;

    public static String getTranslationKey(ITextComponent component) {
        if (component instanceof TextComponentTranslation) {
            return ((TextComponentTranslation) component).getKey();
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
        Minecraft mc = Minecraft.getMinecraft();
        FlowScreen flowScreen = new FlowScreen(mc.currentScreen);
        Flow flow = Flows.loginWithProfile(profile, flowScreen);
        flowScreen.setFlow(flow);
        flow.getSession().thenRun(() -> Minecraft.getMinecraft().addScheduledTask(ReconnectHelper::connect));
        mc.displayGuiScreen(flowScreen);
    }

    private static void connect() {
        if (screen != null) {
            SocketAddress add = ReflectionHelper.<NetworkManager>getField(managerField, screen).getRemoteAddress();
            if (add instanceof InetSocketAddress) {
                InetSocketAddress address = (InetSocketAddress) add;
                Minecraft client = Minecraft.getMinecraft();
                client.displayGuiScreen(new GuiConnecting(ReflectionHelper.getField(previousField, screen), client, address.getHostString(), address.getPort()));
            }
        }
    }
}
