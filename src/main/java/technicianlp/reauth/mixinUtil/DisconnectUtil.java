package technicianlp.reauth.mixinUtil;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.mojang.authlib.exceptions.AuthenticationException;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableText;
import technicianlp.reauth.ReAuth;
import technicianlp.reauth.gui.AuthScreen;

public class DisconnectUtil {
    private static ConnectScreenDuck screen;

    public static String getTranslationKey(Object component) {
        if (component instanceof TranslatableText) {
            return ((TranslatableText) component).getKey();
        }
        return "";
    }

    public static void setConnectScreen(ConnectScreenDuck screen) {
        DisconnectUtil.screen = screen;
    }

    public static boolean canRetryLogin() {
        return screen != null && ReAuth.config.hasCredentials();
    }

    public static void retryLogin() {
        try {
            ReAuth.auth.login(ReAuth.config.getUsername(), ReAuth.config.getPassword(), true);
            if (screen != null) {
                SocketAddress add = screen.reauthGetConnection().getAddress();
                if (add instanceof InetSocketAddress) {
                    InetSocketAddress address = (InetSocketAddress) add;
                    MinecraftClient client = MinecraftClient.getInstance();
                    ConnectScreen.connect(screen.reauthGetParent(), client, new ServerAddress(address.getHostString(), address.getPort()), null);
                }
            }
        } catch (AuthenticationException exception) {
            ReAuth.log.error("Login failed:", exception);
            Screen login = new AuthScreen(screen.reauthGetParent(), I18n.translate("reauth.login.fail", exception.getMessage()));
            MinecraftClient.getInstance().openScreen(login);
        }
    }
}
