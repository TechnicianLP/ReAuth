package technicianlp.reauth;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import technicianlp.reauth.authentication.flows.Flow;
import technicianlp.reauth.authentication.flows.Flows;
import technicianlp.reauth.configuration.Profile;
import technicianlp.reauth.gui.FlowScreen;
import technicianlp.reauth.util.ReflectionUtils;

public final class ReconnectHelper {

    private static final Field managerField = ReflectionUtils.findObfuscatedField(ConnectScreen.class, "f_95684_", "connection");
    private static final Field previousField = ReflectionUtils.findObfuscatedField(ConnectScreen.class, "f_95686_", "parent");
    private static ConnectScreen screen;

    /**
     * Extract the translationKey from the supplied {@link Component}
     *
     * @param nested whether to extract the key from the nested {@link Component} instead
     */
    public static String getTranslationKey(Object component, boolean nested) {
        if (component instanceof MutableComponent mutableComponent) {
            ComponentContents contents = mutableComponent.getContents();
            if (contents instanceof TranslatableContents translatableContents) {
                if (nested) {
                    Object[] args = translatableContents.getArgs();
                    if (args.length >= 1) {
                        return getTranslationKey(args[0], false);
                    }
                } else {
                    return translatableContents.getKey();
                }
            }
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
        Flow flow = FlowScreen.open(Flows::loginWithProfile, profile, true);
        flow.thenRunAsync(ReconnectHelper::connect, Minecraft.getInstance());
    }

    private static void connect() {
        if (screen != null) {
            SocketAddress add = ReflectionUtils.<Connection>getField(managerField, screen).getRemoteAddress();
            if (add instanceof InetSocketAddress address) {
                Minecraft minecraft = Minecraft.getInstance();
                ServerAddress server = new ServerAddress(address.getHostString(), address.getPort());
                ConnectScreen.startConnecting(ReflectionUtils.getField(previousField, screen), minecraft, server, minecraft.getCurrentServer());
            }
        }
    }
}
