package technicianlp.reauth;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.awt.Color;

@Mod.EventBusSubscriber(modid = "reauth", value = Side.CLIENT)
public final class GuiHandler {

    /**
     * Cache the Status for 5 Minutes
     */
    private static final CachedProperty<ValidationStatus> status = new CachedProperty<>(1000 * 60 * 5, ValidationStatus.Unknown);
    private static Thread validator;

    static boolean enabled = true;
    static boolean bold = true;

    @SubscribeEvent
    public static void open(InitGuiEvent.Post e) {
        boolean run = false;
        if (e.getGui() instanceof GuiMultiplayer) {
            e.getButtonList().add(new GuiButton(17325, 5, 5, 100, 20, "Re-Login"));
            run = true;

            if (enabled && !status.check()) {
                if (validator != null)
                    validator.interrupt();
                validator = new Thread(() -> status.set(Secure.SessionValid() ? ValidationStatus.Valid : ValidationStatus.Invalid), "Session-Validator");
                validator.setDaemon(true);
                validator.start();
            }
        } else if (e.getGui() instanceof GuiMainMenu) {
            run = true;
            // Support for Custom Main Menu (add button outside of viewport)
            e.getButtonList().add(new GuiButton(17325, -50, -50, 20, 20, "ReAuth"));
        }

        if (run && VersionChecker.shouldRun())
            VersionChecker.update();
    }

    @SubscribeEvent
    public static void draw(DrawScreenEvent.Post e) {
        if (enabled && e.getGui() instanceof GuiMultiplayer) {
            e.getGui().drawString(e.getGui().mc.fontRenderer, "Online:", 110, 10, 0xFFFFFFFF);
            ValidationStatus state = status.get();
            e.getGui().drawString(e.getGui().mc.fontRenderer, (bold ? ChatFormatting.BOLD : "") + state.text, 145, 10, state.color);
        }
    }

    @SubscribeEvent
    public static void action(ActionPerformedEvent.Post e) {
        if ((e.getGui() instanceof GuiMainMenu || e.getGui() instanceof GuiMultiplayer) && e.getButton().id == 17325) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiLogin(Minecraft.getMinecraft().currentScreen));
        }
    }

    @SubscribeEvent
    public static void action(ActionPerformedEvent.Pre e) {
        if (enabled && e.getGui() instanceof GuiMultiplayer && e.getButton().id == 8 && GuiScreen.isShiftKeyDown()) {
            status.invalidate();
        }
    }

    static void invalidateStatus() {
        status.invalidate();
    }

    private enum ValidationStatus {
        Unknown("?", Color.GRAY.getRGB()), Valid("\u2714", Color.GREEN.getRGB()), Invalid("\u2718", Color.RED.getRGB());

        private final String text;
        private final int color;

        ValidationStatus(String text, int color) {
            this.text = text;
            this.color = color;
        }
    }

}
