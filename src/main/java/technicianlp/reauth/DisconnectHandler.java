package technicianlp.reauth;

import com.mojang.authlib.exceptions.AuthenticationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import technicianlp.reauth.gui.AuthScreen;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public final class DisconnectHandler {

    private static final Field managerField = ObfuscationReflectionHelper.findField(ConnectingScreen.class, "field_146371_g");
    private static final Field previousField = ObfuscationReflectionHelper.findField(ConnectingScreen.class, "field_146374_i");
    private static ConnectingScreen screen;

    public static String getTranslationKey(Object component) {
        if (component instanceof TranslationTextComponent) {
            return ((TranslationTextComponent) component).getKey();
        }
        return "";
    }

    public static void setConnectScreen(ConnectingScreen screen) {
        DisconnectHandler.screen = screen;
    }

    public static boolean canRetryLogin() {
        return screen != null && ReAuth.config.hasCredentials();
    }

    public static void retryLogin() {
        try {
            ReAuth.auth.login(ReAuth.config.getUsername(), ReAuth.config.getPassword(), true);
            if (screen != null) {
                SocketAddress add = ReAuth.<NetworkManager>getField(managerField, screen).getRemoteAddress();
                if (add instanceof InetSocketAddress) {
                    InetSocketAddress address = (InetSocketAddress) add;
                    Minecraft client = Minecraft.getInstance();
                    client.displayGuiScreen(new ConnectingScreen(ReAuth.getField(previousField, screen), client, address.getHostString(), address.getPort()));
                }
            }
        } catch (AuthenticationException exception) {
            ReAuth.log.error("Login failed:", exception);
            Screen login = new AuthScreen(ReAuth.getField(previousField, screen), I18n.format("reauth.login.fail", exception.getMessage()));
            Minecraft.getInstance().displayGuiScreen(login);
        }
    }
}
