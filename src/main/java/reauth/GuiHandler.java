package reauth;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.UUID;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.lwjgl.opengl.Display;

import reauth.Secure.Sessionutil;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import com.mojang.util.UUIDTypeAdapter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Session;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class GuiHandler {

	private String validText;
	private int validColor;
	private Thread validator;

	@SubscribeEvent
	public void ongui(InitGuiEvent.Post e) {
		if (e.gui instanceof GuiMultiplayer) {
			e.buttonList.add(new GuiButton(17325, 5, 5, 100, 20, "Re-Login"));

			validText = "?";
			validColor = Color.GRAY.getRGB();

			if (this.validator != null)
				validator.interrupt();
			validator = new Thread(new Runnable() {
				public void run() {
					if (Secure.SessionValid()) {
						validText = "\u2714";
						validColor = Color.GREEN.getRGB();
					} else {
						validText = "\u2718";
						validColor = Color.RED.getRGB();
					}
				}
			});
			validator.start();
		}
	}

	@SubscribeEvent
	public void ongui(DrawScreenEvent.Post e) {
		if (e.gui instanceof GuiMultiplayer) {
			e.gui.drawString(e.gui.mc.fontRenderer, "Online:", 110, 10, Color.WHITE.getRGB());
			e.gui.drawString(e.gui.mc.fontRenderer, EnumChatFormatting.BOLD + validText, 145, 10, validColor);
		}
	}

	@SubscribeEvent
	public void ongui(ActionPerformedEvent.Post e) {
		if (e.gui instanceof GuiMultiplayer && e.button.id == 17325) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiLogin(Minecraft.getMinecraft().currentScreen));
		}
	}
}
