package technicianlp.reauth;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.network.ClientConnection;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import technicianlp.reauth.authentication.flows.Flow;
import technicianlp.reauth.authentication.flows.Flows;
import technicianlp.reauth.configuration.Profile;
import technicianlp.reauth.gui.FlowScreen;
import technicianlp.reauth.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public enum ReconnectHelper {
    ;

    private static final Field managerField = ReflectionUtils.findObfuscatedField(ConnectScreen.class, "f_95684_",
        "connection");
    private static final Field previousField = ReflectionUtils.findObfuscatedField(ConnectScreen.class, "f_95686_",
        "parent");
    private static ConnectScreen connectScreen;

    /**
     * Extract the translationKey from the supplied {@link Text}
     *
     * @param nested whether to extract the key from the nested {@link Text} instead
     */
    public static String getTranslationKey(Object component, boolean nested) {
        if (component instanceof MutableText mutableText) {
            TextContent contents = mutableText.getContent();
            if (contents instanceof TranslatableTextContent translatable) {
                if (nested) {
                    Object[] args = translatable.getArgs();
                    if (args.length >= 1) {
                        return getTranslationKey(args[0], false);
                    }
                } else {
                    return translatable.getKey();
                }
            }
        }
        return "";
    }

    public static void setConnectScreen(ConnectScreen connectScreen) {
        ReconnectHelper.connectScreen = connectScreen;
    }

    public static boolean hasConnectionInfo() {
        return connectScreen != null;
    }

    public static void retryLogin(Profile profile) {
        Flow flow = FlowScreen.open(Flows::loginWithProfile, profile);
        flow.thenRunAsync(ReconnectHelper::connect, MinecraftClient.getInstance());
    }

    private static void connect() {
        if (connectScreen != null) {
            SocketAddress add = ReflectionUtils.<ClientConnection>getField(managerField, connectScreen).getAddress();
            if (add instanceof InetSocketAddress address) {
                MinecraftClient minecraft = MinecraftClient.getInstance();
                ServerAddress server = new ServerAddress(address.getHostString(), address.getPort());
                ConnectScreen.connect(ReflectionUtils.getField(previousField, connectScreen), minecraft, server,
                    minecraft.getCurrentServerEntry());
            }
        }
    }
}
